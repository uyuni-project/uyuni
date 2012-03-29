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

package com.redhat.rhn.domain.credentials;

import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.user.User;

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
     * Create a new pair of credentials for a given user.
     * @param user user to associate with these credentials
     * @return new pair of credentials
     */
    public static Credentials createNewCredentials(User user) {
        Credentials creds = new Credentials();
        creds.setUser(user);
        return creds;
    }

    /**
     * Store a pair of credentials to the database.
     * @param creds credentials
     */
    public static void storeCredentials(Credentials creds) {
        creds.setModified(new Date());
        singleton.saveObject(creds);
    }

    /**
     * Delete a pair of credentials from the database.
     * @param creds credentials
     */
    public static void removeCredentials(Credentials creds) {
        singleton.removeObject(creds);
    }

    /**
     * Load credentials for a given {@link User}.
     * @param user user
     * @return credentials or null
     */
    public static Credentials lookupByUser(User user) {
        if (user == null) {
            return null;
        }

        Session session = null;
        try {
            session = HibernateFactory.getSession();
            return (Credentials) session.getNamedQuery("Credentials.findByUser")
                    .setParameter("user", user)
                    // Retrieve from cache if there
                    .setCacheable(true).uniqueResult();
        }
        catch (HibernateException e) {
            log.error("Hibernate exception: " + e.toString());
            throw e;
        }
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
