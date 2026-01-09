/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.domain.access;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.org.Org;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Factory class for RBAC's {@link AccessGroup} entities
 */
public class AccessGroupFactory extends HibernateFactory {

    private static final AccessGroupFactory INSTANCE = new AccessGroupFactory();
    private static final Logger LOG = LogManager.getLogger(AccessGroupFactory.class);

    public static final AccessGroup CHANNEL_ADMIN = lookupDefault("channel_admin");
    public static final AccessGroup CONFIG_ADMIN = lookupDefault("config_admin");
    public static final AccessGroup SYSTEM_GROUP_ADMIN = lookupDefault("system_group_admin");
    public static final AccessGroup ACTIVATION_KEY_ADMIN = lookupDefault("activation_key_admin");
    public static final AccessGroup IMAGE_ADMIN = lookupDefault("image_admin");
    public static final AccessGroup REGULAR_USER = lookupDefault("regular_user");
    public static final Set<AccessGroup> DEFAULT_GROUPS =
            Set.of(CHANNEL_ADMIN, CONFIG_ADMIN, SYSTEM_GROUP_ADMIN, ACTIVATION_KEY_ADMIN, IMAGE_ADMIN, REGULAR_USER);

    private AccessGroupFactory() {
        super();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * Persists an {@code AccessGroup} entity to DB.
     * @param accessGroupIn the entity to save
     * @return the saved entity (managed instance)
     */
    public static AccessGroup save(AccessGroup accessGroupIn) {
        return (AccessGroup) INSTANCE.saveObject(accessGroupIn);
    }

    /**
     * Deletes an {@code AccessGroup} entity from DB.
     * @param accessGroupIn the entity to remove
     */
    public static void remove(AccessGroup accessGroupIn) {
        INSTANCE.removeObject(accessGroupIn);
    }

    /**
     * Lists all access groups defined in MLM.
     * @return the list of access groups
     */
    public static List<AccessGroup> listAll() {
        return getSession()
                .createQuery("SELECT a FROM AccessGroup a", AccessGroup.class)
                .getResultList();
    }

    /**
     * Lists all access groups that are available to an org, including the default ones.
     * @param org the org
     * @return the list of access groups
     */
    public static List<AccessGroup> list(Org org) {
        return getSession()
                .createQuery("SELECT a FROM AccessGroup a WHERE a.org = :org OR a.org IS NULL", AccessGroup.class)
                .setParameter("org", org)
                .getResultList();
    }

    /**
     * Looks up an access group by its label.
     * @param label the label of the access group
     * @param org the org to search in
     * @return an {@code Optional} containing the access group, or an empty {@code Optional} if not found
     */
    public static Optional<AccessGroup> lookupByLabelAndOrg(String label, Org org) {
        return getSession()
                .createQuery("SELECT a FROM AccessGroup a WHERE a.label = :label AND (a.org = :org OR a.org IS NULL)",
                        AccessGroup.class)
                .setParameter("label", label)
                .setParameter("org", org)
                .uniqueResultOptional();
    }

    /**
     * Looks up a default access group by its label.
     * @param label the label of the access group
     * @return the access group
     */
    public static AccessGroup lookupDefault(String label) {
        return getSession()
                .createQuery("SELECT a FROM AccessGroup a WHERE a.label = :label AND a.org IS NULL",
                        AccessGroup.class)
                .setParameter("label", label)
                .uniqueResult();
    }
}
