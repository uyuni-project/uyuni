/**
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.domain.role;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.log4j.Logger;
import org.hibernate.Session;

/**
 * RoleFactory
 * @version $Rev$
 */
public class RoleFactory extends HibernateFactory {

    private static Logger log = Logger.getLogger(RoleFactory.class);

    /**
     * Constructs an RoleFactory and initializes the Hibernate
     * Configuration and SessionFactory.
     */
    private RoleFactory() { }


    /**
     * Create a new Role object
     * @return Role to use
     */
    public static Role createRole() {
        return new RoleImpl();
    }

    /** {@inheritDoc} */
    protected Logger getLogger() {
        return log;
    }

    /**
     * Returns the Role with the given id.
     * @param id of Role to be found.
     * @return the Role with the given id.
     */
    public static Role lookupById(Long id) {
        Session session = HibernateFactory.getSession();
        return (Role) session.getNamedQuery("Role.findById")
                                       .setLong("id", id.longValue())
                                       //Retrieve from cache if there
                                       .setCacheable(true)
                                       .uniqueResult();
    }

    /**
     * Get the statetype by name.
     * @param name Name of statetype
     * @return statetype whose name matches the given name.
     */
    public static Role lookupByLabel(String name) {
        Session session = HibernateFactory.getSession();
        return (Role) session.getNamedQuery("Role.findByLabel")
                                       .setString("label", name)
                                       //Retrieve from cache if there
                                       .setCacheable(true)
                                       .uniqueResult();
    }

    /**
     * The constant representing org_admin role.  Used for comparison.
     */
    public static final Role ORG_ADMIN = lookupByLabel("org_admin");

    /**
     * The constant representing satellite_admin role.  Used for comparison.
     */
    public static final Role SAT_ADMIN = lookupByLabel("satellite_admin");

    /**
     * The constant representing channel_admin
     */
    public static final Role CHANNEL_ADMIN = lookupByLabel("channel_admin");

    /**
     * The constant representing config_admin
     */
    public static final Role CONFIG_ADMIN = lookupByLabel("config_admin");

    /**
     * The constant representing system_group_admin
     */
    public static final Role SYSTEM_GROUP_ADMIN = lookupByLabel("system_group_admin");

    /**
     * The constant representing activation_key_admin
     */
    public static final Role ACTIVATION_KEY_ADMIN = lookupByLabel("activation_key_admin");
}
