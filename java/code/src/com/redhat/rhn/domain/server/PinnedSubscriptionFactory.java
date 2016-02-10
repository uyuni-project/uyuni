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
package com.redhat.rhn.domain.server;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.scc.SCCOrderItem;

import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;

import java.util.List;


/**
 * A factory for creating PinnedSubscription objects.
 */
public class PinnedSubscriptionFactory extends HibernateFactory {

    /** The log. */
    private static Logger log;

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
        if (log == null) {
            log = Logger.getLogger(PinnedSubscriptionFactory.class);
        }
        return log;
    }

    /**
     * List the pinned subscriptions.
     *
     * @return the list
     */
    @SuppressWarnings("unchecked")
    public List<PinnedSubscription> listPinnedSubscriptions() {
        return getSession().createCriteria(PinnedSubscription.class).list();
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
    @SuppressWarnings("unchecked")
    public void cleanStalePins() {
        DetachedCriteria systemIds = DetachedCriteria.forClass(Server.class)
                .setProjection(Projections.id());

        DetachedCriteria subscriptionIds = DetachedCriteria.forClass(SCCOrderItem.class)
                .setProjection(Projections.id());

        getSession()
            .createCriteria(PinnedSubscription.class)
            .add(Restrictions.disjunction()
                .add(Subqueries.propertyNotIn("systemId", systemIds))
                .add(Subqueries.propertyNotIn("subscriptionId", subscriptionIds))
            )
            .list()
            .forEach(stalePin -> remove((PinnedSubscription)stalePin));
    }
}
