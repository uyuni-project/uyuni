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

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.listview.PageControl;

import com.suse.manager.utils.PagedSqlQueryBuilder;
import com.suse.manager.webui.utils.gson.AccessGroupJson;
import com.suse.manager.webui.utils.gson.AccessGroupUserJson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.type.StandardBasicTypes;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.persistence.Tuple;

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
     * @return the saved entity
     */
    public static AccessGroup save(AccessGroup accessGroupIn) {
        INSTANCE.saveObject(accessGroupIn);
        return accessGroupIn;
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
     * Lists s paginated list of access groups
     * @param pc the page control
     * @param parser the parser for filters when building query
     * @return the list of access groups
     */
    public static DataResult<AccessGroupJson> listAll(
            PageControl pc, Function<Optional<PageControl>, PagedSqlQueryBuilder.FilterWithValue> parser) {
        String from = "(select " +
                "ag.id as id, " +
                "ag.label as name, " +
                "ag.description as description, " +
                "ag.org_id as org_id, " +
                "wc.name as org_name, " +
                "case" +
                "  when uag.users is not null then uag.users else 0 " +
                "end as users, " +
                "case" +
                "  when agn.permissions is not null then agn.permissions else 0 " +
                "end as permissions " +
                "from access.accessgroup ag " +
                "left join " +
                "  (select group_id, count(group_id) users " +
                "  from access.useraccessgroup group by group_id) uag " +
                "on ag.id = uag.group_id " +
                "left join " +
                "  (select group_id, count(group_id) permissions " +
                "  from access.accessgroupnamespace group by group_id) agn " +
                "on ag.id = agn.group_id " +
                "left join web_customer wc " +
                "on wc.id = ag.org_id " +
                ") ag ";

        return new PagedSqlQueryBuilder("ag.id")
                .select("ag.*")
                .from(from)
                .where("true")
                .run(new HashMap<>(), pc, parser, AccessGroupJson.class);
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
                "SELECT uag.user_id FROM access.useraccessgroup uag WHERE uag.group_id = :group_id", Tuple.class)
                .setParameter("group_id", groupId)
                .addScalar("user_id", StandardBasicTypes.LONG)
                .stream().map(tuple -> tuple.get("user_Id", Long.class))
                .toList();
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
        return getSession()
                .createQuery("SELECT a FROM AccessGroup a WHERE a.label = :label AND a.org IS NULL",
                        AccessGroup.class)
                .setParameter("label", label)
                .uniqueResult();
    }
}
