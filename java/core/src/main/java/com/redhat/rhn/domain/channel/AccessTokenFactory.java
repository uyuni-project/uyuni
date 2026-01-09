/*
 * Copyright (c) 2016--2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.channel;

import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.taskomatic.task.TaskConstants;

import com.suse.manager.webui.utils.token.DownloadTokenBuilder;
import com.suse.manager.webui.utils.token.Token;
import com.suse.manager.webui.utils.token.TokenException;
import com.suse.utils.Opt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jose4j.lang.JoseException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Methods for working with AccessTokens
 */
public class AccessTokenFactory extends HibernateFactory {

    private static AccessTokenFactory singleton = new AccessTokenFactory();
    private static final Logger LOG = LogManager.getLogger(AccessTokenFactory.class);

    /**
     * Queries an AccessToken by id.
     * @param id id of the AccessToken
     * @return optional of AccessToken
     */
    public static Optional<AccessToken> lookupById(long id) {
        return getSession()
                .createQuery("FROM AccessToken WHERE id = :id", AccessToken.class)
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    /**
     * Queries all AccessTokens
     * @return list of AccessTokens
     */
    public static List<AccessToken> all() {
        return getSession()
                .createQuery("FROM AccessToken", AccessToken.class)
                .list();
    }


    /**
     * Queries all AccessTokens for a specific minion
     * @param minion the minion
     * @return list of AccessTokens
     */
    public static List<AccessToken> listByMinion(MinionServer minion) {
        return getSession()
                .createQuery("FROM AccessToken WHERE minion = :minion", AccessToken.class)
                .setParameter("minion", minion)
                .list();
    }

    /**
     * Queries an AccessToken by token.
     * @param token token of the AccessToken
     * @return optional of AccessToken
     */
    public static Optional<AccessToken> lookupByToken(String token) {
        return getSession()
                .createQuery("FROM AccessToken WHERE token = :token", AccessToken.class)
                .setParameter("token", token)
                .uniqueResultOptional();
    }

    /**
     * Saves the AccessToken to the database.
     * @param accessToken the AccessToken to save
     * @return the managed {@link AccessToken}
     */
    public static AccessToken save(AccessToken accessToken) {
        return singleton.saveObject(accessToken);
    }

    /**
     * Returns the list of tokens for a minion that give access to channels
     * the minion does not need.
     *
     * @param minion minion to check
     * @param tokensToActivate the new tokens to activate
     * @return list of tokens
     */
    public static List<AccessToken> unneededTokens(MinionServer minion, Collection<AccessToken> tokensToActivate) {
        return minion.getAccessTokens().stream()
                .filter(token -> !tokensToActivate.contains(token))
                .filter(token -> {
            // we only keep the token linked to the minion
            // if all channels it provides access to are needed
            // or if the tokens to activate don't have the same channels
            return !minion.getChannels().containsAll(token.getChannels()) ||
                    tokensToActivate.stream()
                            .anyMatch(newToken ->
                                    newToken.getChannels().containsAll(token.getChannels()));
        }).collect(Collectors.toList());
    }

    /**
     * Refreshes the AccessTokens of the given minion.
     * A token will be refreshed if either
     *  - its close to or already expired.
     *  - it gives access to more channels then the minion should have access to.
     * @param minion the minion to refresh the tokens for
     * @return boolean indicating if something change
     */
    public static boolean refreshTokens(MinionServer minion) {
        return refreshTokens(minion, Collections.emptySet());
    }

    /**
     * Refreshes the AccessTokens of the given minion.
     * A token will be refreshed if either
     *  - its close to or already expired.
     *  - it gives access to more channels then the minion should have access to.
     * If the parameter tokensToActivate is not empty, the channels from the supplied
     * tokens will be taken into consideration and the tokens will be activated and
     * added to the minion.
     *
     * @param minion  the minion to refresh the tokens for
     * @param tokensToActivate the tokens to activate and to add to the minion
     * @return boolean indicating if something change
     */
    public static boolean refreshTokens(MinionServer minion, Collection<AccessToken> tokensToActivate) {
        List<AccessToken> unneededTokens = unneededTokens(minion, tokensToActivate);
        Set<AccessToken> all = minion.getAccessTokens();
        all.removeAll(unneededTokens);

        unneededTokens.forEach(token -> {
            token.setMinion(null);
            token.setValid(false);
            AccessTokenFactory.save(token);
        });

        Map<Boolean, List<AccessToken>> collect = all.stream()
                .collect(Collectors.partitioningBy(token -> {
            Instant expiration = token.getExpiration().toInstant();

            Instant now = Instant.now();

            // using 10% of the tokens lifetime as buffer to
            // regenerate tokens before they expire
            Duration buffer = Duration.ofMillis(
                    (long) ((token.getExpiration().getTime() -
                            token.getStart().getTime()) * 0.1)
            );

            return now.plus(buffer).isAfter(expiration);
        }));
        List<AccessToken> update = collect.get(true);
        List<AccessToken> noUpdate = collect.get(false);

        List<Channel> allTokenChannels =
                Stream.concat(all.stream(), tokensToActivate.stream())
                        .flatMap(s -> s.getChannels().stream())
                        .toList();

        ArrayList<Channel> withoutToken = new ArrayList<>(minion.getChannels());
        withoutToken.removeAll(allTokenChannels);

        List<AccessToken> newTokens = withoutToken.stream().flatMap(channel ->
                Opt.stream(generate(minion, Collections.singleton(channel)))
        ).toList();
        all.addAll(newTokens);

        List<AccessToken> maybeRefreshed = update.stream().map(token -> {
            try {
                return regenerate(token);
            }
            catch (TokenException e) {
                LOG.error("Could not regenerate token with id: {}", token.getId(), e);
                return token;
            }
        }).toList();

        List<AccessToken> tokens = Stream.concat(
                maybeRefreshed.stream(),
                Stream.concat(newTokens.stream(), noUpdate.stream())
        ).toList();
        minion.getAccessTokens().clear();
        minion.getAccessTokens().addAll(tokens);
        tokensToActivate.forEach(toActivate -> {
            toActivate.setValid(true);
            minion.getAccessTokens().add(toActivate);
        });

        LOG.debug("Token refresh finished. Got Unneeded {} Got Updated {} Got New {}",
                !unneededTokens.isEmpty(), !update.isEmpty(), !newTokens.isEmpty());
        return !unneededTokens.isEmpty() || !update.isEmpty() || !newTokens.isEmpty();
    }

    /**
     * Deletes unassigned expired AccessTokens.
     */
    public static void cleanupUnusedExpired() {
        WriteMode m = ModeFactory.getWriteMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_TOKEN_CLEANUP);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing WriteMode " + TaskConstants.MODE_NAME + "::" +
                    TaskConstants.TASK_QUERY_TOKEN_CLEANUP);
        }
        int tokensDeleted = m.executeUpdate(new HashMap<>());
        if (LOG.isDebugEnabled()) {
            LOG.debug("WriteMode " + TaskConstants.MODE_NAME + "::" +
                    TaskConstants.TASK_QUERY_TOKEN_CLEANUP + " returned");
        }
        //logs number of tokens deleted
        if (tokensDeleted > 0) {
            LOG.info("{} channel access tokens deleted", tokensDeleted);
        }
        else {
            LOG.debug("No tokens to be deleted");
        }
   }

    /**
     * Deletes the given AccessToken.
     * @param token AccessToken to delete.
     */
    public static void delete(AccessToken token) {
        HibernateFactory.getSession().remove(token);
    }

    /**
     * Generates an AccessToken for the given MinionServer and set of channels.
     * @param minion minion
     * @param channels set of channels
     * @return AccessToken if it could be generated
     */
    public static Optional<AccessToken> generate(MinionServer minion,
            Set<Channel> channels) {
        try {
            Token token = new DownloadTokenBuilder(minion.getOrg().getId())
                .usingServerSecret()
                .allowingOnlyChannels(channels.stream().map(Channel::getLabel).collect(Collectors.toSet()))
                .build();

            AccessToken newToken = new AccessToken();
            newToken.setStart(Date.from(token.getIssuingTime()));
            newToken.setToken(token.getSerializedForm());
            newToken.setMinion(minion);
            newToken.setExpiration(Date.from(token.getExpirationTime()));
            newToken.setChannels(channels);
            save(newToken);
            return Optional.of(newToken);
        }
        catch (TokenException e) {
            LOG.error("Could not generate token for minion: {}", minion.getId(), e);
            return Optional.empty();
        }
    }

    /**
     * Regenerated an access token by creating a new one with the same information.
     * If the token is linked to a minion it will be unlinked and linked to the new token.
     *
     * @param accessToken access token to regenerate
     * @return the new access token
     * @throws JoseException if token generation fails in this case
     * the old token will not be unlinked.
     */
    public static AccessToken regenerate(AccessToken accessToken) throws TokenException {
        Token token = new DownloadTokenBuilder(accessToken.getMinion().getOrg().getId())
            .usingServerSecret()
            .allowingOnlyChannels(accessToken.getChannels().stream().map(Channel::getLabel).collect(Collectors.toSet()))
            .build();

        // Link new token
        AccessToken newToken = new AccessToken();
        newToken.setStart(Date.from(token.getIssuingTime()));
        newToken.setToken(token.getSerializedForm());
        newToken.setMinion(accessToken.getMinion());
        newToken.setExpiration(Date.from(token.getExpirationTime()));
        // We need to copy the collection here because hibernate does not like to share.
        newToken.setChannels(new HashSet<>(accessToken.getChannels()));

        AccessTokenFactory.save(newToken);

        // Unlink the old token
        accessToken.setMinion(null);
        accessToken.setValid(false);
        AccessTokenFactory.save(accessToken);

        return newToken;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
