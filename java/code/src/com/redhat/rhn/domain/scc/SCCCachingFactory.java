/**
 * Copyright (c) 2014 SUSE LLC
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
package com.redhat.rhn.domain.scc;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Factory class for populating and reading from SCC caching tables.
 */
public class SCCCachingFactory extends HibernateFactory {

    private static Logger log = Logger.getLogger(SCCCachingFactory.class);
    private static SCCCachingFactory singleton = new SCCCachingFactory();

    private SCCCachingFactory() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Store {@link SCCRepository} to the database.
     * @param repo repository
     */
    public static void saveRepository(SCCRepository repo) {
        repo.setModified(new Date());
        singleton.saveObject(repo);
    }

    /**
     * Lookup all repositories.
     * @return list of repositories
     */
    @SuppressWarnings("unchecked")
    public static List<SCCRepository> lookupRepositories() {
        log.debug("Retrieving repositories from cache");
        Session session = getSession();
        Criteria c = session.createCriteria(SCCRepository.class);
        return c.list();
    }

    /**
     * Clear all repositories from the database.
     */
    public static void clearRepositories() {
        getSession().getNamedQuery("SCCRepository.deleteAll").executeUpdate();
    }

    /**
     * Store {@link SCCSubscription} to the database.
     * @param subscription the subscription
     */
    public static void saveSubscription(SCCSubscription subscription) {
        subscription.setModified(new Date());
        singleton.saveObject(subscription);
    }

    /**
     * Store {@link SCCSubscription} to the database.
     * @param jsonSub the json subscription
     * @param creds the credentials
     */
    public static void saveJsonSubscription(com.suse.scc.model.SCCSubscription jsonSub,
            Credentials creds) {
        Set<SUSEProduct> products = new HashSet<>();
        for (Long pid : jsonSub.getProductIds()) {
            SUSEProduct prd = SUSEProductFactory.lookupByProductId(pid);
            if (prd != null) {
                products.add(prd);
            }
            else {
                log.error("unable to find product for scc product id: " + pid);
            }
        }

        SCCSubscription sub = lookupSubscriptionBySccId((long) jsonSub.getId());
        if (sub == null) {
            sub = new SCCSubscription();
        }

        sub.setCredentials(creds);
        sub.setSccId(jsonSub.getId());
        sub.setName(jsonSub.getName());
        sub.setStartsAt(jsonSub.getStartsAt());
        sub.setExpiresAt(jsonSub.getExpiresAt());
        sub.setStatus(jsonSub.getStatus());
        sub.setRegcode(jsonSub.getRegcode());
        sub.setType(jsonSub.getType());
        sub.setSystemLimit(jsonSub.getSystemLimit().longValue());

        sub.setProducts(products);
        sub.setModified(new Date());
        singleton.saveObject(sub);
    }

    /**
     * Lookup all Subscriptions
     * @return list of subscriptions
     */
    @SuppressWarnings("unchecked")
    public static List<SCCSubscription> lookupSubscriptions() {
        log.debug("Retrieving subscriptions from cache");
        Session session = getSession();
        Criteria c = session.createCriteria(SCCSubscription.class);
        return c.list();
    }

    /**
     * Lookup a {@link SCCSubscription} object for given sccId.
     * @param id the scc id
     * @return SCC Subscription or null
     */
    public static SCCSubscription lookupSubscriptionBySccId(Long id) {
        if (id == null) {
            return null;
        }
        Session session = getSession();
        Criteria c = session.createCriteria(SCCSubscription.class);
        c.add(Restrictions.eq("sccId", id));
        return (SCCSubscription) c.uniqueResult();
    }
    /**
     * Clear all subscriptions from the database.
     */
    public static void clearSubscriptions() {
        getSession().getNamedQuery("SCCSubscription.deleteAll").executeUpdate();
    }

    /**
     * Clear all subscriptions from the database assigned to the
     * credential.
     * @param c the credentials
     */
    public static void clearSubscriptions(Credentials c) {
        if (c == null) {
            clearSubscriptions();
            return;
        }
        getSession()
                .getNamedQuery("SCCSubscription.deleteByCredential")
                .setParameter("creds", c)
                .executeUpdate();
    }

    /**
     * Lookup all Order Items
     * @return list of Order Items
     */
    @SuppressWarnings("unchecked")
    public static List<SCCOrderItem> lookupOrderItems() {
        log.debug("Retrieving orderItems from cache");
        Session session = getSession();
        Criteria c = session.createCriteria(SCCOrderItem.class);
        return c.list();
    }

    /**
     * Store {@link SCCOrderItem} to the database.
     * @param item order item
     */
    public static void saveOrderItem(SCCOrderItem item) {
        item.setModified(new Date());
        singleton.saveObject(item);
    }

    /**
     * Clear all order items from the database assigne to the given
     * credentials
     * @param c the credentials
     */
    public static void clearOrderItems(Credentials c) {
        if (c == null) {
            clearOrderItems();
            return;
        }
        getSession()
        .getNamedQuery("SCCOrderItem.deleteByCredential")
        .setParameter("creds", c)
        .executeUpdate();
    }

    /**
     * Clear all order items from the database
     */
    public static void clearOrderItems() {
        getSession()
        .getNamedQuery("SCCOrderItem.deleteAll")
        .executeUpdate();
    }

    /**
     * Check if the cache needs a refresh.
     * @return true if refresh is needed, false otherwise
     */
    public static boolean refreshNeeded() {
        Session session = getSession();
        Criteria c = session.createCriteria(Credentials.class);
        c.add(Restrictions.eq("type", CredentialsFactory
                .findCredentialsTypeByLabel(Credentials.TYPE_SCC)));
        c = c.setProjection(Projections.max("modified"));
        Date modifiedCreds = (Date) c.uniqueResult();
        if (modifiedCreds == null) {
            return true;
        }

        // When was the cache last modified?
        Criteria c2 = session.createCriteria(SCCRepository.class);
        c2 = c2.setProjection(Projections.max("modified"));
        Date modifiedCache = (Date) c2.uniqueResult();
        if (modifiedCache == null) {
            return true;
        }

        return modifiedCache.compareTo(modifiedCreds) < 0;
    }

    /**
     * List Subscriptions SCC IDs by Credential
     * @param c the credential to query
     * @return list of scc subscription ids
     */
    public static List<Long> listSubscriptionsIdsByCredentials(Credentials c) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<SCCSubscription> query = builder.createQuery(SCCSubscription.class);
        Root<SCCSubscription> root = query.from(SCCSubscription.class);
        query.where(builder.equal(root.get("credentials"), c));
        List<Long> result = new ArrayList<>();
        for (SCCSubscription sub : getSession().createQuery(query).getResultList()) {
            result.add(sub.getSccId());
        }
        return result;
    }

    /**
     * Delete Subscriptions from Cache
     * @param sccSubId id to delete
     */
    public static void deleteSubscriptionBySccId(Long sccSubId) {
        SCCSubscription sub = SCCCachingFactory.lookupSubscriptionBySccId(sccSubId.longValue());
        if (sub != null) {
            singleton.removeObject(sub);
        }
    }
}
