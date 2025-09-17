/*
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
package com.redhat.rhn.domain.server;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import com.suse.manager.matcher.MatcherJsonIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.type.StandardBasicTypes;

import java.util.List;

import javax.persistence.TypedQuery;

/**
 * A factory for creating PinnedSubscription objects.
 */
public class PinnedSubscriptionFactory extends HibernateFactory {

    /** The log. */
    private static final Logger LOG = LogManager.getLogger(PinnedSubscriptionFactory.class);

    /** The instance. */
    private static PinnedSubscriptionFactory instance;

    /**
     * Instantiates a new pinned subscription factory.
     */
    private PinnedSubscriptionFactory() {
    }

    /**
     * Gets the single instance of PinnedSubscriptionFactory.
     *
     * @return single instance of PinnedSubscriptionFactory
     */
    public static synchronized PinnedSubscriptionFactory getInstance() {
        if (instance == null) {
            instance = new PinnedSubscriptionFactory();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * List the pinned subscriptions.
     *
     * @return the list
     */
    @SuppressWarnings("unchecked")
    public List<PinnedSubscription> listPinnedSubscriptions() {
        String sql = "SELECT * FROM susePinnedSubscription";

        TypedQuery<PinnedSubscription> query
                = getSession().createNativeQuery(sql, PinnedSubscription.class);
        return query.getResultList();
    }

    /**
     * Save a pinned subscription.
     *
     * @param subscription the subscription
     */
    public void save(PinnedSubscription subscription) {
        saveObject(subscription);
    }

    /**
     * Removes a pinned subscription.
     *
     * @param subscription the subscription
     */
    public void remove(PinnedSubscription subscription) {
        removeObject(subscription);
    }

    /**
     * Clean stale pins.
     */
    public void cleanStalePins() {
        getSession()
            .getNamedQuery("PinnedSubscription.cleanStalePins")
            .setParameter("selfSystemId", MatcherJsonIO.SELF_SYSTEM_ID, StandardBasicTypes.LONG)
            .executeUpdate();
    }

    /**
     * Looks up PinnedSubscription by id
     * @param id the id
     * @return PinnedSubscription object
     */
    public PinnedSubscription lookupById(Long id) {
        String sql = "SELECT * FROM susePinnedSubscription WHERE id = :id";
        return getSession()
                .createNativeQuery(sql, PinnedSubscription.class).setParameter("id", id)
                .getSingleResult();
    }

    /**
     * Looks up PinnedSubscription by systemId and subscriptionId
     * @param systemId the system id
     * @param subscriptionId the subscription id
     * @return PinnedSubscription object
     */
    public PinnedSubscription lookupBySystemIdAndSubscriptionId(Long systemId,
            Long subscriptionId) {

        String sql = "SELECT * FROM susePinnedSubscription " +
                "WHERE system_id = :systemId AND subscription_id = :subscriptionId";
        return getSession()
                .createNativeQuery(sql, PinnedSubscription.class)
                .setParameter("systemId", systemId, StandardBasicTypes.LONG)
                .setParameter("subscriptionId", subscriptionId, StandardBasicTypes.LONG)
                .getSingleResult();
    }
}
