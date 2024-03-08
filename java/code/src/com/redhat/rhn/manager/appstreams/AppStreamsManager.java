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
import com.redhat.rhn.domain.channel.AppStreamModule;

import org.hibernate.Session;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class AppStreamsManager {

    private AppStreamsManager() {
        // hidden constructor
    }

    /**
     * List App Streams modules in a Channel
     *
     * @param channelId the id of the channel
     * @return the List of enabled modules
     */
    public static List<AppStreamModule> listChannelModules(Long channelId) {
        CriteriaBuilder criteriaBuilder = HibernateFactory.getSession().getCriteriaBuilder();
        CriteriaQuery<AppStreamModule> criteriaQuery = criteriaBuilder.createQuery(AppStreamModule.class);

        Root<AppStreamModule> root = criteriaQuery.from(AppStreamModule.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.equal(root.get("channel").get("id"), channelId));

        return HibernateFactory.getSession().createQuery(criteriaQuery).getResultList();

    }

    /**
     * Find a specific module in a channel based on name, stream, version, context, and arch.
     *
     * @param channelId the id of the channel
     * @param name      the name of the module
     * @param stream    the stream of the module
     * @param version   the version of the module
     * @param context   the context of the module
     * @param arch      the architecture of the module
     * @return the found module or null if not found
     */
    public static AppStreamModule findModule(
            Long channelId, String name, String stream, String version, String context, String arch) {
        Session session = HibernateFactory.getSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<AppStreamModule> criteriaQuery = criteriaBuilder.createQuery(AppStreamModule.class);
        Root<AppStreamModule> root = criteriaQuery.from(AppStreamModule.class);

        Predicate channelIdPredicate = criteriaBuilder.equal(root.get("channel").get("id"), channelId);
        Predicate namePredicate = criteriaBuilder.equal(root.get("name"), name);
        Predicate streamPredicate = criteriaBuilder.equal(root.get("stream"), stream);
        Predicate versionPredicate = criteriaBuilder.equal(root.get("version"), version);
        Predicate contextPredicate = criteriaBuilder.equal(root.get("context"), context);
        Predicate archPredicate = criteriaBuilder.equal(root.get("arch"), arch);

        Predicate finalPredicate = criteriaBuilder.and(
            channelIdPredicate,
            namePredicate,
            streamPredicate,
            versionPredicate,
            contextPredicate,
            archPredicate
        );

        criteriaQuery.select(root).where(finalPredicate);
        return session.createQuery(criteriaQuery).uniqueResult();
    }
}
