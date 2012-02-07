/**
 * Copyright (c) 2012 Novell
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

package com.redhat.rhn.manager.org;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.org.Credentials;
import com.redhat.rhn.domain.org.Org;

/**
 * CredentialsFactory
 */
public class CredentialsFactory extends HibernateFactory {

    private static CredentialsFactory singleton = new CredentialsFactory();
    private static Logger log = Logger.getLogger(CredentialsFactory.class);

    private CredentialsFactory() {
        super();
    }

    /**
     * Create a new pair of credentials for a given organization.
     * @param org
     * @return
     */
    public static Credentials createNewCredentials(Org org) {
        Credentials creds = new Credentials();
        creds.setOrg(org);
        return creds;
    }

    /**
     * Store a pair of credentials to the database.
     */
    public static void storeCredentials(Credentials creds) {
        singleton.saveObject(creds);
    }

    /**
     * Delete a pair of credentials from the database.
     */
    public static void removeCredentials(Credentials creds) {
        singleton.removeObject(creds);
    }

    /**
     * Load a pair of credentials given by org ID.
     * @param orgID
     * @return
     */
    public static Credentials lookupByOrg(Org org) {
        if (org == null) {
            return null;
        }

        Session session = null;
        try {
            session = HibernateFactory.getSession();
            return (Credentials) session.getNamedQuery("Credentials.findByOrg")
                    .setParameter("org", org)
                    // Retrieve from cache if there
                    .setCacheable(true).uniqueResult();
        } catch (HibernateException e) {
            log.error("Hibernate exception: " + e.toString());
            throw e;
        }
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
