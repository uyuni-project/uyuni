/**
 * Copyright (c) 2012 SUSE LLC
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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.user.User;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * Create new empty {@link Credentials}.
     * @return new empty credentials
     */
    public static Credentials createCredentials() {
        Credentials creds = new Credentials();
        return creds;
    }

    /**
     * Store {@link Credentials} to the database.
     * @param creds credentials
     */
    public static void storeCredentials(Credentials creds) {
        creds.setModified(new Date());
        singleton.saveObject(creds);
    }

    /**
     * Delete {@link Credentials} from the database.
     * @param creds credentials
     */
    public static void removeCredentials(Credentials creds) {
        singleton.removeObject(creds);
    }

    /**
     * Load {@link Credentials} for a given {@link User} and type label.
     * @param user user
     * @param typeLabel type label
     * @return credentials or null
     */
    public static Credentials lookupByUserAndType(User user, String typeLabel) {
        if (user == null || typeLabel == null) {
            return null;
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("user", user);
        params.put("label", typeLabel);
        return (Credentials) singleton.lookupObjectByNamedQuery(
                "Credentials.findByUserAndTypeLabel", params);
    }

    /**
     * Find a {@link CredentialsType} by a given label.
     * @param label label
     * @return CredentialsType instance for given label
     */
    public static CredentialsType findCredentialsTypeByLabel(String label) {
        if (label == null) {
            return null;
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("label", label);
        return (CredentialsType) singleton.lookupObjectByNamedQuery(
                "CredentialsType.findByLabel", params);
    }

    /**
     * Helper method for creating new SUSE Studio {@link Credentials} for a
     * given user.
     * @param user user to associate with these credentials
     * @return new credentials for SUSE Studio
     */
    public static Credentials createStudioCredentials(User user) {
        Credentials creds = createCredentials();
        creds.setUser(user);
        creds.setType(CredentialsFactory
                .findCredentialsTypeByLabel(Credentials.TYPE_SUSESTUDIO));
        return creds;
    }

    /**
     * Helper method for looking up SUSE Studio credentials.
     * @param user user
     * @return credentials or null
     */
    public static Credentials lookupStudioCredentials(User user) {
        return lookupByUserAndType(user, Credentials.TYPE_SUSESTUDIO);
    }

    /**
     * Helper method for creating new SCC {@link Credentials}
     * @return new credential with type SCC
     */
    public static Credentials createSCCCredentials() {
        Credentials creds = createCredentials();
        creds.setType(CredentialsFactory
                .findCredentialsTypeByLabel(Credentials.TYPE_SCC));
        return creds;
    }

    /**
     * Helper method for looking up SCC credentials.
     * @return credentials or null
     */
    @SuppressWarnings("unchecked")
    public static List<Credentials> lookupSCCCredentials() {
        Session session = getSession();
        Criteria c = session.createCriteria(Credentials.class);
        c.add(Restrictions.eq("type", CredentialsFactory
                .findCredentialsTypeByLabel(Credentials.TYPE_SCC)));
        c.addOrder(Order.asc("url"));
        c.addOrder(Order.asc("id"));
        return c.list();
    }

    /**
     * Helper method for creating new Virtual Host Manager {@link Credentials}
     * @return new credential with type Virtual Host Manager
     */
    public static Credentials createVHMCredentials() {
        Credentials creds = createCredentials();
        creds.setType(CredentialsFactory
                .findCredentialsTypeByLabel(Credentials.TYPE_VIRT_HOST_MANAGER));
        return creds;
    }

    /**
     * Find credentials by ID.
     * @param id the id
     * @return credentials object or null
     */
    public static Credentials lookupCredentialsById(long id) {
        Session session = getSession();
        Credentials creds = (Credentials) session.get(Credentials.class, id);
        return creds;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
