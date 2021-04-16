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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.product.SUSEProduct;

import com.suse.scc.model.SCCRepositoryJson;
import com.suse.scc.model.SCCSubscriptionJson;
import com.suse.utils.Opt;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
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
        singleton.saveObject(repo);
    }

    /**
     * Store {@link SCCRepositoryAuth} to the database.
     * @param auth repo authentication
     */
    public static void saveRepositoryAuth(SCCRepositoryAuth auth) {
        singleton.saveObject(auth);
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
        //getSession().getNamedQuery("SCCRepository.deleteAll").executeUpdate();
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaDelete<SCCRepository> delete = builder.createCriteriaDelete(SCCRepository.class);
        CriteriaDelete<SCCRepositoryAuth> deleteAuth = builder.createCriteriaDelete(SCCRepositoryAuth.class);
        delete.from(SCCRepository.class);
        deleteAuth.from(SCCRepositoryAuth.class);
        getSession().createQuery(deleteAuth).executeUpdate();
        getSession().createQuery(delete).executeUpdate();
    }

    /**
     * Store {@link SCCSubscriptionJson} to the database.
     * @param subscription the subscription
     */
    public static void saveSubscription(SCCSubscription subscription) {
        subscription.setModified(new Date());
        singleton.saveObject(subscription);
    }

    /**
     * Store {@link SCCSubscriptionJson} to the database.
     * @param jsonSub the json subscription
     * @param creds the credentials
     * @param productsBySccId lookup map of products by scc id
     * @param subscriptionBySccId lookup map of subscriptions by scc id
     * @return generated SCC Subscription
     */
    public static SCCSubscription saveJsonSubscription(SCCSubscriptionJson jsonSub, Credentials creds,
            Map<Long, SUSEProduct> productsBySccId, Map<Long, SCCSubscription> subscriptionBySccId) {

        SCCSubscription sub = Optional.ofNullable(subscriptionBySccId.get(jsonSub.getId()))
                .orElse(new SCCSubscription());

        sub.setCredentials(creds);
        sub.setSccId(jsonSub.getId());
        sub.setName(jsonSub.getName());
        sub.setStartsAt(jsonSub.getStartsAt());
        sub.setExpiresAt(jsonSub.getExpiresAt());
        sub.setStatus(jsonSub.getStatus());
        sub.setRegcode(jsonSub.getRegcode());
        sub.setType(jsonSub.getType());
        sub.setSystemLimit(jsonSub.getSystemLimit().longValue());

        Set<SUSEProduct> products = sub.getProducts();
        List<Long> currentProductIds = jsonSub.getProductIds();
        for (Long pid : currentProductIds) {
            if (productsBySccId.containsKey(pid)) {
                if (!products.contains(productsBySccId.get(pid))) {
                    products.add(productsBySccId.get(pid));
                }
            }
        }
        Set<SUSEProduct> toRemove = new HashSet<>();
        for (SUSEProduct p : products) {
            if (!currentProductIds.contains(p.getProductId())) {
                toRemove.add(p);
            }
        }
        products.removeAll(toRemove);
        sub.setProducts(products);
        sub.setModified(new Date());
        singleton.saveObject(sub);
        return sub;
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
     * Lookup a {@link SCCSubscriptionJson} object for given sccId.
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
        //getSession().getNamedQuery("SCCSubscription.deleteAll").executeUpdate();
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaDelete<SCCSubscription> delete = builder.createCriteriaDelete(SCCSubscription.class);
        Root d = delete.from(SCCSubscription.class);
        getSession().createQuery(delete).executeUpdate();

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
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaDelete<SCCSubscription> delete = builder.createCriteriaDelete(SCCSubscription.class);
        Root<SCCSubscription> e = delete.from(SCCSubscription.class);
        delete.where(builder.equal(e.get("credentials"), c));
        getSession().createQuery(delete).executeUpdate();
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
     * Return a list of OrderItems fetched with the provided Credentials
     * @param c the Credentials
     * @return the list of OrderItems
     */
    public static List<SCCOrderItem> listOrderItemsByCredentials(Credentials c) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<SCCOrderItem> query = builder.createQuery(SCCOrderItem.class);
        Root<SCCOrderItem> root = query.from(SCCOrderItem.class);
        query.where(builder.equal(root.get("credentials"), c));
        return getSession().createQuery(query).getResultList();
    }

    /**
     * Lookup an OrderItem by its SCC Id
     * @param sccId the SCC Id
     * @return the OrderItem
     */
    public static Optional<SCCOrderItem> lookupOrderItemBySccId(Long sccId) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<SCCOrderItem> query = builder.createQuery(SCCOrderItem.class);
        Root<SCCOrderItem> root = query.from(SCCOrderItem.class);
        query.where(
                builder.equal(root.get("sccId"), sccId));
        return getSession().createQuery(query).uniqueResultOptional();
    }

    /**
     * Delete an order Item from DB
     * @param oi the item to delete
     */
    public static void deleteOrderItem(SCCOrderItem oi) {
        singleton.removeObject(oi);
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
     * Store {@link SCCRegCacheItem} to the database.
     * @param item regcache item
     */
    public static void saveRegCacheItem(SCCRegCacheItem item) {
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
     *
     * @param lastRefreshDateIn the last refresh cache date, if any
     * @return true if refresh is needed, false otherwise
     */
    public static boolean refreshNeeded(Optional<Date> lastRefreshDateIn) {
        Session session = getSession();
        Criteria c = session.createCriteria(Credentials.class);
        c.add(Restrictions.eq("type", CredentialsFactory
                .findCredentialsTypeByLabel(Credentials.TYPE_SCC)));
        c = c.setProjection(Projections.max("modified"));
        Date modifiedCreds = (Date) c.uniqueResult();
        if (modifiedCreds == null) {
            log.debug("REFRESH NEEDED - no credentials found");
            return true;
        }

        // When was the cache last modified?
        return Opt.fold(
                lastRefreshDateIn,
                () -> {
                    log.debug("REFRESH NEEDED - never refreshed");
                    return true;
                },
                modifiedCache -> {
                    log.debug("COMPARE: " + modifiedCache.toString() + " and " + modifiedCreds.toString() +
                            " : " + modifiedCache.compareTo(modifiedCreds));
                    return modifiedCache.compareTo(modifiedCreds) < 0;
                }
        );
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

    /**
     * Return a Repository
     * @param sccId the scc id
     * @return a SCCRepository
     */
    public static Optional<SCCRepository> lookupRepositoryBySccId(Long sccId) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<SCCRepository> select = builder.createQuery(SCCRepository.class);
        Root<SCCRepository> root = select.from(SCCRepository.class);
        select.where(builder.equal(root.get("sccId"), sccId));
        return getSession().createQuery(select).uniqueResultOptional();
    }

    /**
     * Lookup a {@link SCCRepository} by its name
     * @param name the name
     * @return the repository if found
     */
    public static Optional<SCCRepository> lookupRepositoryByName(String name) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<SCCRepository> select = builder.createQuery(SCCRepository.class);
        Root<SCCRepository> root = select.from(SCCRepository.class);
        select.where(builder.equal(root.get("name"), name));
        return getSession().createQuery(select).uniqueResultOptional();
    }

    /**
     * Get all repositories auth for a given Credential
     * @param c the Credential (null is supported)
     * @return a list of SCCRepositoriesAuth
     */
    public static List<SCCRepositoryAuth> lookupRepositoryAuthByCredential(Credentials c) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<SCCRepositoryAuth> select = builder.createQuery(SCCRepositoryAuth.class);
        Root<SCCRepositoryAuth> root = select.from(SCCRepositoryAuth.class);
        if (c != null) {
            select.where(builder.equal(root.get("credentials"), c));
        }
        else {
            select.where(builder.isNull(root.get("credentials")));
        }
        return getSession().createQuery(select).getResultList();
    }

    /**
     * @return return a list of all {@link SCCRepositoryAuth} objects
     */
    public static List<SCCRepositoryAuth> lookupRepositoryAuth() {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<SCCRepositoryAuth> select = builder.createQuery(SCCRepositoryAuth.class);
        select.from(SCCRepositoryAuth.class);
        return getSession().createQuery(select).getResultList();
    }

    /**
     * @return a list of repository auth objects which are linked to a {@link ContentSource}
     */
    public static List<SCCRepositoryAuth> lookupRepositoryAuthWithContentSource() {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<SCCRepositoryAuth> select = builder.createQuery(SCCRepositoryAuth.class);
        Root<SCCRepositoryAuth> root = select.from(SCCRepositoryAuth.class);
        select.where(builder.isNotNull(root.get("contentSource")));
        return getSession().createQuery(select).getResultList();
    }

    /**
     * @return list of repository ids which can be accessed
     */
    public static List<Long> lookupRepositoryIdsWithAuth() {
        List<BigDecimal> resultList =
                getSession().getNamedNativeQuery("SCCRepositoryAuth.lookupRepoIdWithAuth").getResultList();
        return resultList.stream().map(BigDecimal::longValue).collect(Collectors.toList());
    }

    /**
     * @param channelFamily channel family label
     * @return list of {@link SCCRepository} for the given channel family label
     */
    public static List<SCCRepository> lookupRepositoriesByChannelFamily(String channelFamily) {
        return getSession().getNamedQuery("SCCRepository.lookupByChannelFamily")
                .setParameter("channelFamily", channelFamily).getResultList();
    }

    /**
     * Find a compatible SCCRepository using a json repository
     * @param repos collection of available repositories
     * @param j the Json version of the repo
     * @return the found SCCRepository
     */
    public static Optional<SCCRepository> findRepo(Collection<SCCRepository> repos, SCCRepositoryJson j) {
        return repos.stream().filter(r -> r.getSccId().equals(j.getSCCId())).findFirst();
    }

    /**
     * Remove the repository
     * @param r the repository to remove
     */
    public static void deleteRepository(SCCRepository r) {
        singleton.removeObject(r);
    }

    /**
     * Remove the repository auth
     * @param a the repository auth to remove
     */
    public static void deleteRepositoryAuth(SCCRepositoryAuth a) {
        singleton.removeObject(a);
    }

    /**
     * Returns registration items of systems which should be forwarded to SCC
     *
     * @return list of {@link SCCRegCacheItem}
     */
    public static List<SCCRegCacheItem> findSystemsToForwardRegistration() {
        int regErrorExpireTime = Config.get().getInt(ConfigDefaults.REG_ERROR_EXPIRE_TIME, 168);
        Calendar retryTime = Calendar.getInstance();
        retryTime.add(Calendar.HOUR, -1 * regErrorExpireTime);

        return getSession().getNamedQuery("SCCRegCache.serversRequireRegistration")
                .setParameter("retryTime", new Date (retryTime.getTimeInMillis()))
                .getResultList();
    }

    /**
     * Returns registration items of systems which should be de-registered from SCC
     *
     * @return list of {@link SCCRegCacheItem}
     */
    public static List<SCCRegCacheItem> listDeregisterItems() {
        int regErrorExpireTime = Config.get().getInt(ConfigDefaults.REG_ERROR_EXPIRE_TIME, 168);
        Calendar retryTime = Calendar.getInstance();
        retryTime.add(Calendar.HOUR, -1 * regErrorExpireTime);

        return getSession().getNamedQuery("SCCRegCache.listDeRegisterItems")
                .setParameter("retryTime", new Date (retryTime.getTimeInMillis()))
                .getResultList();
    }
}
