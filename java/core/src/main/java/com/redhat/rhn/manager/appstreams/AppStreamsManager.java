/*
 * Copyright (c) 2024 SUSE LLC
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
package com.redhat.rhn.manager.appstreams;

import static java.util.Collections.singleton;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.appstream.AppStreamActionDetails;
import com.redhat.rhn.domain.channel.AppStream;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.ssm.SsmManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.webui.controllers.appstreams.response.SsmAppStreamModuleResponse;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class AppStreamsManager {

    private static final String SSM_CHANNEL_APPSTREAMS_SQL = """
        SELECT
            a.name AS name,
            a.stream AS stream,
            a.arch AS arch,
            COUNT(DISTINCT ssm.server_id) AS systemCount
        FROM suseAppstream a
        LEFT JOIN suseServerAppstream sa
        ON a.name = sa.name
            AND a.stream = sa.stream
            AND a.version = sa.version
            AND a.context = sa.context
            AND a.arch = sa.arch
        LEFT JOIN (
            SELECT ST.element AS server_id
            FROM rhnSet ST
            JOIN rhnServerChannel rsc ON ST.element = rsc.server_id
            WHERE ST.user_id = :user_id
            AND ST.label = :set_label
            AND rsc.channel_id = :channel_id
        ) ssm ON sa.server_id = ssm.server_id
        WHERE a.channel_id = :channel_id
        GROUP BY a.name, a.stream, a.arch
        ORDER BY a.name, a.stream
    """;

    private AppStreamsManager() {
        // hidden constructor
    }

    /**
     * List AppStreams in a Channel
     *
     * @param channelId the id of the channel
     * @return the List of AppStreams
     */
    public static List<AppStream> listChannelAppStreams(Long channelId) {
        CriteriaBuilder criteriaBuilder = HibernateFactory.getSession().getCriteriaBuilder();
        CriteriaQuery<AppStream> criteriaQuery = criteriaBuilder.createQuery(AppStream.class);

        Root<AppStream> root = criteriaQuery.from(AppStream.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.equal(root.get("channel").get("id"), channelId));

        return HibernateFactory.getSession().createQuery(criteriaQuery).getResultList();

    }

    /**
     * Schedules changes (enable/disable) to AppStreams in a Server.
     *
     * @param serverId            the id of the server
     * @param streamsToEnable     the set of streams to enable
     * @param streamsToDisable    the set of streams to disable
     * @param user                the user performing the action
     * @param actionChainLabel    the label for the action chain
     * @param earliestOccurrence  the earliest occurrence for the action
     * @return the id of the scheduled action
     * @throws TaskomaticApiException if an error occurs in Taskomatic
     */
    public static Long scheduleAppStreamsChanges(
            Long serverId,
            Set<String> streamsToEnable,
            Set<String> streamsToDisable,
            User user, Optional<String> actionChainLabel,
            Date earliestOccurrence) throws TaskomaticApiException {
        return scheduleAppStreamsChanges(
            singleton(serverId), streamsToEnable, streamsToDisable, user, actionChainLabel, earliestOccurrence
        );
    }

    /**
     * Schedules AppStream changes (enable/disable) for all the user's SSM systems
     * that are subscribed to the specified channel.
     *
     * @param channelId           the ID of the SSM channel
     * @param streamsToEnable     the set of streams to enable
     * @param streamsToDisable    the set of streams to disable
     * @param user                the user performing the action
     * @param actionChainLabel    the label for the action chain
     * @param earliestOccurrence  the earliest occurrence date for the action
     * @return the id of the scheduled action
     * @throws TaskomaticApiException if an error occurs in Taskomatic
     */
    public static Long scheduleSsmAppStreamsChanges(
            Long channelId,
            Set<String> streamsToEnable,
            Set<String> streamsToDisable,
            User user,
            Optional<String> actionChainLabel,
            Date earliestOccurrence) throws TaskomaticApiException {

        var serverIds = SsmManager.listSsmServerIdsInChannel(user, channelId);
        return scheduleAppStreamsChanges(
            serverIds, streamsToEnable, streamsToDisable, user, actionChainLabel, earliestOccurrence
        );
    }

    /**
     * Retrieves a list of AppStream modules for a specific channel, and counts
     * how many of the user's SSM systems in that channel are associated with each module.
     *
     * @param channelId the ID of a specific channel to list AppStreams from
     * @param user      the user
     * @return a {@code List} of {@link SsmAppStreamModuleResponse} objects, each
     * containing module detail and the calculated system counts.
     */
    public static List<SsmAppStreamModuleResponse> listSsmChannelAppStreams(Long channelId, User user) {
        return HibernateFactory
                .getSession()
                .createNativeQuery(SSM_CHANNEL_APPSTREAMS_SQL, Tuple.class)
                .setParameter("channel_id", channelId)
                .setParameter("user_id", user.getId())
                .setParameter("set_label", RhnSetDecl.SYSTEMS.getLabel())
                .getResultList()
                .stream()
                .map(SsmAppStreamModuleResponse::new)
                .toList();
    }

    private static Stream<AppStreamActionDetails> getAppStreamsEnableDetails(Set<String> appStreamsToEnable) {
        return appStreamsToEnable.stream().map(AppStreamActionDetails::enableAction);
    }

    private static String actionName(Set<String> streamsToEnable, Set<String> streamsToDisable) {
        StringBuilder sb = new StringBuilder("Change AppStreams (");
        if (!streamsToDisable.isEmpty()) {
            sb.append("disable: ");
            sb.append(streamsToDisable);
            sb.append(" ");
        }
        if (!streamsToEnable.isEmpty()) {
            sb.append("enable: ");
            sb.append(streamsToEnable);
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Return a set of appstreams available on the given system
     *
     * @param serverId the id of the system
     * @param user     the user
     * @return set of appstreams
     * @throws LookupException
     */
    public static Set<String> getSystemAppStreams(Long serverId, User user) throws LookupException {
        Set<String> appStreams = new HashSet<>();
        SystemManager.lookupByIdAndUser(serverId, user)
                .getChannels()
                .stream()
                .filter(Channel::isModular)
                .forEach(channel -> {
                    AppStreamsManager.listChannelAppStreams(channel.getId()).forEach(appStream ->
                        appStreams.add(appStream.getName() + ":" + appStream.getStream()));
                });
        return appStreams;
    }

    /**
     * Find a specific appstream in a channel based on module name and stream.
     *
     * @param channelId the id of the channel
     * @param name      the name of the module
     * @param stream    the stream
     * @return the found module or null if not found
     */
    public static AppStream findAppStream(Long channelId, String name, String stream) {
        Session session = HibernateFactory.getSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<AppStream> criteriaQuery = criteriaBuilder.createQuery(AppStream.class);
        Root<AppStream> root = criteriaQuery.from(AppStream.class);

        Predicate channelIdPredicate = criteriaBuilder.equal(root.get("channel").get("id"), channelId);
        Predicate namePredicate = criteriaBuilder.equal(root.get("name"), name);
        Predicate streamPredicate = criteriaBuilder.equal(root.get("stream"), stream);

        Predicate finalPredicate = criteriaBuilder.and(
            channelIdPredicate,
            namePredicate,
            streamPredicate
        );

        criteriaQuery.select(root).where(finalPredicate);
        return session.createQuery(criteriaQuery).stream().findFirst().orElse(null);
    }

    /**
     * Clone appstreams from one channel to another
     *
     * @param to channel to clone to
     * @param from channel to clone from
     */
    public static void cloneAppStreams(Channel to, Channel from) {
        if (from.isModular()) {
            HibernateFactory.getSession()
                .createNativeQuery("call clone_channel_appstreams(:fromChannelId, :toChannelId)")
                .setParameter("fromChannelId", from.getId())
                .setParameter("toChannelId", to.getId())
                .executeUpdate();
        }
    }

    /**
     * Schedules changes (enable/disable) to AppStreams in a Server.
     *
     * @param serverIds           the ids of the servers
     * @param streamsToEnable     the set of streams to enable
     * @param streamsToDisable    the set of streams to disable
     * @param user                the user performing the action
     * @param actionChainLabel    the label for the action chain
     * @param earliestOccurrence  the earliest occurrence for the action
     * @return the id of the scheduled action
     * @throws TaskomaticApiException if an error occurs in Taskomatic
     */
    private static Long scheduleAppStreamsChanges(
            Set<Long> serverIds,
            Set<String> streamsToEnable,
            Set<String> streamsToDisable,
            User user, Optional<String> actionChainLabel,
            Date earliestOccurrence) throws TaskomaticApiException {
        ActionChain actionChain = actionChainLabel
                .filter(StringUtils::isNotEmpty)
                .map(label -> ActionChainFactory.getOrCreateActionChain(label, user))
                .orElse(null);

        var details = Stream.concat(
                getAppStreamsEnableDetails(streamsToEnable),
                streamsToDisable.stream().map(AppStreamActionDetails::disableAction)
        ).collect(Collectors.toSet());

        return ActionChainManager.scheduleAppStreamAction(
                user,
                actionName(streamsToEnable, streamsToDisable),
                details,
                earliestOccurrence,
                actionChain,
                serverIds
        );
    }
}
