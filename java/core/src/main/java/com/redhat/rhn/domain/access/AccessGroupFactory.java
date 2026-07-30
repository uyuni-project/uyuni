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
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.domain.user.legacy.PersonalInfo;
import com.redhat.rhn.domain.user.legacy.UserImpl;

import com.suse.manager.webui.utils.gson.AccessGroupJson;
import com.suse.manager.webui.utils.gson.AccessGroupUserJson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.type.StandardBasicTypes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.persistence.Tuple;

/**
 * Factory class for RBAC's {@link AccessGroup} entities
 */
public class AccessGroupFactory extends HibernateFactory {

    private static final AccessGroupFactory INSTANCE = new AccessGroupFactory();
    private static final Logger LOG = LogManager.getLogger(AccessGroupFactory.class);
    private static final Map<String, Long> LABEL_TO_ID = new ConcurrentHashMap<>();

    public static AccessGroup getChannelAdmin() {
        return lookupDefault("channel_admin");
    }

    public static AccessGroup getConfigAdmin() {
        return lookupDefault("config_admin");
    }

    public static AccessGroup getSystemGroupAdmin() {
        return lookupDefault("system_group_admin");
    }

    public static AccessGroup getActivationKeyAdmin() {
        return lookupDefault("activation_key_admin");
    }

    public static AccessGroup getImageAdmin() {
        return lookupDefault("image_admin");
    }

    public static AccessGroup getRegularUser() {
        return lookupDefault("regular_user");
    }

    public static Set<AccessGroup> getDefaultGroups() {
        return Set.of(
            getChannelAdmin(),
            getConfigAdmin(),
            getSystemGroupAdmin(),
            getActivationKeyAdmin(),
            getImageAdmin(),
            getRegularUser()
        );
    }

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
     * Lists all custom access groups for a given org.
     * @param org the org
     * @return the list of custom access groups
     */
    public static List<AccessGroupJson> listCustom(Org org) {
        String sql = """
            SELECT
                ag.id as id,
                ag.label as name,
                ag.description as description,
                ag.org_id as org_id,
                wc.name as org_name,
                COALESCE(uag.users, 0) as users,
                COALESCE(agn.permissions, 0) as permissions
            FROM access.accessgroup ag
            LEFT JOIN (
                SELECT group_id, count(*) AS users
                FROM access.useraccessgroup
                GROUP BY group_id
            ) uag ON ag.id = uag.group_id
            LEFT JOIN (
                SELECT group_id, count(*) AS permissions
                FROM access.accessgroupnamespace
                GROUP BY group_id
            ) agn ON ag.id = agn.group_id
            LEFT JOIN web_customer wc ON wc.id = ag.org_id
            WHERE ag.org_id = :org_id
        """;
        return getSession()
                .createNativeQuery(sql, Tuple.class)
                .addSynchronizedEntityClass(Org.class)
                .addSynchronizedEntityClass(UserImpl.class)
                .addSynchronizedEntityClass(AccessGroup.class)
                .setParameter("org_id", org.getId())
                .getResultList()
                .stream()
                .map(AccessGroupJson::new)
                .toList();
    }

    /**
     * Lists all the users of a given organization
     * @param orgId the org id
     * @return the list of users as json object
     */
    public static List<AccessGroupUserJson> listUsers(Long orgId) {
        return getSession().createNativeQuery("""
                 SELECT wc.id,
                        wc.login,
                        wupi.email,
                        concat(wupi.last_name, ', ', wupi.first_names) AS name,
                        wcu.name AS org_name
                 FROM web_contact wc
                 JOIN web_user_personal_info wupi ON wc.id = wupi.web_user_id
                 JOIN web_customer wcu ON wc.org_id = wcu.id
                 WHERE wcu.id = :org_id
                 """, Tuple.class)
                .addSynchronizedEntityClass(Org.class)
                .addSynchronizedEntityClass(UserImpl.class)
                .addSynchronizedEntityClass(PersonalInfo.class)
                .setParameter("org_id", orgId)
                .stream().map(AccessGroupUserJson::new)
                .toList();
    }

    /**
     * Lists all the users that are subscribed to the given access group
     * @param groupId the access group id
     * @return the list of users
     */
    public static List<User> listAccessGroupUsers(Long groupId) {
        List<Long> ids = getSession().createNativeQuery(
                "SELECT uag.user_id FROM access.useraccessgroup uag WHERE uag.group_id = :group_id", Long.class)
                .addSynchronizedEntityClass(UserImpl.class)
                .setParameter("group_id", groupId)
                .addScalar("user_id", StandardBasicTypes.LONG)
                .getResultList();
        return UserFactory.lookupByIds(ids);
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
     * Looks up an access group by its id.
     * @param id the id of the access group
     * @return an {@code Optional} containing the access group, or an empty {@code Optional} if not found
     */
    public static Optional<AccessGroup> lookupById(Long id) {
        return getSession()
                .createQuery("SELECT a FROM AccessGroup a WHERE a.id = :id", AccessGroup.class)
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    /**
     * Looks up a default access group by its label.
     * @param label the label of the access group
     * @return the access group
     */
    public static AccessGroup lookupDefault(String label) {
        // Cache IDs to use Hibernate's L1 cache and avoid multiple queries in loops.
        // L2 Cache (@Cacheable) is not used here because AccessGroup has an eager @ManyToMany
        // relationship with Namespace, which would require cascading cache annotations.
        // Also, since the DB can be modified by independent processes L2 cache could become outdated,
        // causing "ghost" permissions in Tomcat.
        Long id = LABEL_TO_ID.computeIfAbsent(label, l ->
            Optional.ofNullable(getSession()
                    .createQuery("SELECT a FROM AccessGroup a WHERE a.label = :label AND a.org IS NULL",
                            AccessGroup.class)
                    .setParameter("label", l)
                    .uniqueResult())
                    .map(AccessGroup::getId)
                    .orElse(null)
        );

        return Optional.ofNullable(id)
                       .map(i -> getSession().find(AccessGroup.class, i))
                       .orElse(null);
    }
}
