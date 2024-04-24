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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.appstream.AppStreamActionDetails;
import com.redhat.rhn.domain.channel.AppStream;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class AppStreamsManager {

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
        ActionChain actionChain = actionChainLabel
                .filter(StringUtils::isNotEmpty)
                .map(label -> ActionChainFactory.getOrCreateActionChain(label, user))
                .orElse(null);

        Set<AppStreamActionDetails> details = Stream.concat(
                getAppStreamsEnableDetails(streamsToEnable),
                streamsToDisable.stream().map(AppStreamActionDetails::disableAction)
        ).collect(Collectors.toSet());

        return ActionChainManager.scheduleAppStreamAction(
            user,
            actionName(streamsToEnable, streamsToDisable),
            details,
            earliestOccurrence,
            actionChain,
            serverId
        );
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
        return session.createQuery(criteriaQuery).uniqueResult();
    }
}
