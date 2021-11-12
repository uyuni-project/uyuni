/**
 * Copyright (c) 2016 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.channel;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.MinionServer;

import com.suse.manager.webui.utils.DownloadTokenBuilder;
import com.suse.utils.Opt;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.jose4j.lang.JoseException;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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
    private static Logger log = Logger.getLogger(AccessToken.class);

    /**
     * Queries an AccessToken by id.
     * @param id id of the AccessToken
     * @return optional of AccessToken
     */
    public static Optional<AccessToken> lookupById(long id) {
        return Optional.ofNullable(
                (AccessToken)HibernateFactory.getSession()
                .createCriteria(AccessToken.class)
                .add(Restrictions.eq("id", id))
                .uniqueResult()
        );
    }

    /**
     * Queries all AccessTokens
     * @return list of AccessTokens
     */
    public static List<AccessToken> all() {
        return (List<AccessToken>) HibernateFactory.getSession()
                .createCriteria(AccessToken.class)
                .list();
    }

    /**
     * Queries an AccessToken by token.
     * @param token token of the AccessToken
     * @return optional of AccessToken
     */
    public static Optional<AccessToken> lookupByToken(String token) {
        return Optional.ofNullable(
            (AccessToken)HibernateFactory.getSession()
            .createCriteria(AccessToken.class)
            .add(Restrictions.eq("token", token))
            .uniqueResult()
        );
    }

    /**
     * Saves the AccessToken to the database.
     * @param accessToken the AccessToken to save
     * @return the saved AccessToken
     */
    public static AccessToken save(AccessToken accessToken) {
        singleton.saveObject(accessToken);
        return accessToken;
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
                        .collect(Collectors.toList());

        ArrayList<Channel> withoutToken = new ArrayList<>(minion.getChannels());
        withoutToken.removeAll(allTokenChannels);

        List<AccessToken> newTokens = withoutToken.stream().flatMap(channel ->
                Opt.stream(generate(minion, Collections.singleton(channel)))
        ).collect(Collectors.toList());
        all.addAll(newTokens);

        List<AccessToken> maybeRefreshed = update.stream().map(token -> {
            try {
                return regenerate(token);
            }
            catch (JoseException e) {
                log.error("Could not regenerate token with id: " + token.getId(), e);
                e.printStackTrace();
                return token;
            }
        }).collect(Collectors.toList());

        List<AccessToken> tokens = Stream.concat(
                maybeRefreshed.stream(),
                Stream.concat(newTokens.stream(), noUpdate.stream())
        ).collect(Collectors.toList());
        minion.getAccessTokens().clear();
        minion.getAccessTokens().addAll(tokens);
        tokensToActivate.forEach(toActivate -> {
            toActivate.setValid(true);
            minion.getAccessTokens().add(toActivate);
        });

        return !unneededTokens.isEmpty() || !update.isEmpty() || !newTokens.isEmpty();
    }

    /**
     * Deletes unassigned expired AccessTokens.
     */
    public static void cleanupUnusedExpired() {
        Instant now = Instant.now();
        all().forEach(token -> {
            if (token.getMinion() == null &&
                    now.isAfter(token.getExpiration().toInstant())) {
                delete(token);
            }
        });
   }

    /**
     * Deletes the given AccessToken.
     * @param token AccessToken to delete.
     */
    public static void delete(AccessToken token) {
        HibernateFactory.getSession().delete(token);
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
            DownloadTokenBuilder tokenBuilder = new DownloadTokenBuilder(minion.getOrg().getId());
            tokenBuilder.useServerSecret();
            tokenBuilder.onlyChannels(channels.stream().map(Channel::getLabel)
                    .collect(Collectors.toSet()));
            String tokenString = tokenBuilder.getToken();

            AccessToken newToken = new AccessToken();
            newToken.setStart(Date.from(tokenBuilder.getIssuedAt()));
            newToken.setToken(tokenString);
            newToken.setMinion(minion);
            Instant expiration = tokenBuilder.getIssuedAt()
                    .plus(tokenBuilder.getExpirationTimeMinutesInTheFuture(),
                            ChronoUnit.MINUTES);
            newToken.setExpiration(Date.from(expiration));
            newToken.setChannels(channels);
            save(newToken);
            return Optional.of(newToken);
        }
        catch (JoseException e) {
            log.error("Could not generate token for minion: " + minion.getId(), e);
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Regenerated an access token by creating a new one with the same information.
     * If the token is linked to a minion it will be unlinked and linked to the new token.
     *
     * @param token access token to regenerate
     * @return the new access token
     * @throws JoseException if token generation fails in this case
     * the old token will not be unlinked.
     */
    public static AccessToken regenerate(AccessToken token) throws JoseException {
        DownloadTokenBuilder tokenBuilder = new DownloadTokenBuilder(token.getMinion().getOrg().getId());
        tokenBuilder.useServerSecret();
        tokenBuilder.onlyChannels(token.getChannels().stream().map(Channel::getLabel)
                .collect(Collectors.toSet()));
        String tokenString = tokenBuilder.getToken();

        //Link new token
        AccessToken newToken = new AccessToken();
        newToken.setStart(Date.from(tokenBuilder.getIssuedAt()));
        newToken.setToken(tokenString);
        newToken.setMinion(token.getMinion());
        Instant expiration = tokenBuilder.getIssuedAt()
                .plus(tokenBuilder.getExpirationTimeMinutesInTheFuture(),
                        ChronoUnit.MINUTES);
        newToken.setExpiration(Date.from(expiration));
        // We need to copy the collection here because hibernate does not like to share.
        newToken.setChannels(new HashSet<>(token.getChannels()));

        AccessTokenFactory.save(newToken);

        // Unlink the old token
        token.setMinion(null);
        AccessTokenFactory.save(token);

        return newToken;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
