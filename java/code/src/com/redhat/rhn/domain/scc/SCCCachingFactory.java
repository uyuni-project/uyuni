/**
 * Copyright (c) 2014 SUSE
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

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.Date;
import java.util.List;

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
}
