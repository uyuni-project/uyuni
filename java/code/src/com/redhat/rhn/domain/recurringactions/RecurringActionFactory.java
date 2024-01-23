/*
 * Copyright (c) 2020 SUSE LLC
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

package com.redhat.rhn.domain.recurringactions;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.recurringactions.state.InternalState;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;

import com.suse.manager.utils.PagedSqlQueryBuilder;
import com.suse.manager.webui.utils.gson.RecurringActionScheduleJson;
import com.suse.manager.webui.utils.gson.SimpleMinionJson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ServerFactory - the class used to fetch and store
 * com.redhat.rhn.domain.server.RecurringAction objects from the database.
 */
public class RecurringActionFactory extends HibernateFactory {

    private static final Logger LOG = LogManager.getLogger(RecurringActionFactory.class);
    private static final RecurringActionFactory INSTANCE = new RecurringActionFactory();

    /**
     * List minion recurring actions with minion id.
     *
     * @param minion - the minion
     * @return list of minion recurring actions
     */
    public static List<RecurringAction> listMinionRecurringActions(Server minion) {
        // HQL 'IN' clause doesn't like empty lists
        var groups = minion.getGroups().isEmpty() ? null : minion.getGroups();
        return getSession()
                .createQuery("SELECT action FROM RecurringAction action " +
                       "WHERE action.minion = :minion " +
                       "OR action.group in :groups " +
                       "OR action.org = :org " +
                       "ORDER BY action.id DESC",
                       RecurringAction.class)
               .setParameter("minion", minion)
               .setParameter("groups", groups)
               .setParameter("org", minion.getOrg())
               .list();
    }

    /**
     * List group recurring actions with group id.
     *
     * @param group - the server group
     * @return list of group recurring actions
     */
    public static List<RecurringAction> listGroupRecurringActions(ServerGroup group) {
        return getSession()
                .createQuery("SELECT action FROM RecurringAction action " +
                        "WHERE action.group = :group " +
                        "OR action.org = :org " +
                        "ORDER BY action.id DESC",
                        RecurringAction.class)
                .setParameter("group", group)
                .setParameter("org", group.getOrg())
                .list();
    }

    /**
     * List org recurring actions with org id.
     *
     * @param id - id of the organization
     * @return list of org recurring actions
     */
    public static List<RecurringAction> listOrgRecurringActions(Long id) {
        return getSession()
                .createQuery("SELECT action FROM OrgRecurringAction action " +
                        "WHERE action.org.id = :oid " +
                        "ORDER BY action.id DESC",
                        RecurringAction.class)
                .setParameter("oid", id)
                .list();
    }

    /**
     * List all {@link RecurringAction}s that are associated with entities
     * belonging to the {@link Org} of given {@link User}.
     *
     * @param user the user
     * @param pc the page control
     * @param parser the parser for filters when building query
     * @return the list of {@link RecurringAction}s
     */
    public static DataResult<RecurringActionScheduleJson> listAllRecurringActions(
            User user, PageControl pc, Function<Optional<PageControl>, PagedSqlQueryBuilder.FilterWithValue> parser) {
        String from = "(select " +
            "ra.id as recurring_action_id, " +
            "ra.cron_expr as cron, " +
            "ra.action_type as action_type, " +
            "ra.name as schedule_name, " +
            "ra.active as active, " +
            "case" +
            "  when ra.target_type = 'organization' then org.id " +
            "  when ra.target_type = 'group' then sg.id " +
            "  when ra.target_type = 'minion' then minion.id " +
            "end as target_id, " +
            "case" +
            "  when ra.target_type = 'organization' then org.name " +
            "  when ra.target_type = 'group' then sg.name " +
            "  when ra.target_type = 'minion' then minion.name " +
            "end as target_name, " +
            "case" +
            "  when ra.target_type = 'organization' then 'ORG' " +
            "  else UPPER(ra.target_type) " +
            "end as target_type, " +
            "case" +
            "  when ra.target_type = 'organization' then ra.org_id " +
            "  when ra.target_type = 'group' then sg.org_id " +
            "  when ra.target_type = 'minion' then minion.org_id " +
            "end as target_org " +
            "from suseRecurringAction ra " +
            "left join rhnservergroup sg on sg.id = ra.group_id " +
            "left join rhnserver minion on minion.id = ra.minion_id " +
            "left join web_customer org on org.id = ra.org_id" +
            ") ra ";
        List<Org> orgs = user.hasRole(RoleFactory.SAT_ADMIN) ? OrgFactory.lookupAllOrgs() : List.of(user.getOrg());
        Map<String, Object> params = Map.of("orgsIds", orgs.stream().map(Org::getId).collect(Collectors.toList()));

        return new PagedSqlQueryBuilder("ra.recurring_action_id")
                .select("ra.*")
                .from(from)
                .where("ra.target_org in (:orgsIds)")
                .run(params, pc, parser, RecurringActionScheduleJson.class);
    }

    /**
     * List all members of a given server group
     *
     * @param id the server group id
     * @param pc the page control
     * @param parser the parser for filters when building query
     * @return the list of {@link SimpleMinionJson}s
     */
    public static DataResult<SimpleMinionJson> listGroupMembers(Long id, PageControl pc, Function<Optional<PageControl>,
            PagedSqlQueryBuilder.FilterWithValue> parser) {
        Map<String, Object> params = Map.of("id", id);
        return new PagedSqlQueryBuilder("s.id")
                .select("s.id, s.name")
                .from("rhnServer s join rhnServerGroupMembers sgm on s.id = sgm.server_id")
                .where("sgm.server_group_id = :id")
                .run(params, pc, parser, SimpleMinionJson.class);
    }

    /**
     * List all members of a given organization
     *
     * @param id the id of the organization
     * @param pc the page control
     * @param parser the parser for filters when building query
     * @return the list of {@link SimpleMinionJson}s
     */
    public static DataResult<SimpleMinionJson> listOrgMembers(Long id, PageControl pc, Function<Optional<PageControl>,
            PagedSqlQueryBuilder.FilterWithValue> parser) {
        Map<String, Object> params = Map.of("id", id);
        return new PagedSqlQueryBuilder("s.id")
                .select("s.id, s.name")
                .from("rhnServer s")
                .where("s.org_id = :id")
                .run(params, pc, parser, SimpleMinionJson.class);
    }

    /**
     * Return a list of recurring actions that use given config channel
     *
     * @param channel the config channel
     * @return list of actions
     */
    public static List<RecurringAction> listActionWithConfChannel(ConfigChannel channel) {
        return getSession().createQuery("SELECT action FROM RecurringAction action " +
                        "JOIN TREAT(action.recurringActionType AS RecurringState) " +
                        "type LEFT JOIN type.stateConfig conf " +
                        "WHERE conf.configChannel = :channel",
                RecurringAction.class)
                .setParameter("channel", channel)
                .list();
    }

    /**
     * Return a list of all internal states
     *
     * @return list of internal states
     */
    public static List<InternalState> listInternalStates() {
        return getSession().createQuery("FROM InternalState",
                InternalState.class)
                .list();
    }

    /**
     * Lookup recurring action with given id.
     *
     * @param id - id of the recurring action
     * @return optional of matching recurring action
     */
    public static Optional<RecurringAction> lookupById(long id) {
        return getSession().createQuery("SELECT action FROM RecurringAction action " +
                "WHERE action.id = :id", RecurringAction.class)
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    /**
     * Look up {@link RecurringAction} id based on name, type and referenced entity of given object
     *
     * @param action the {@link RecurringAction} object (can even be a Hibernate entity in transient state)
     * @return optional ID of matching {@link RecurringAction}
     */
    public static Optional<Long> lookupEqualEntityId(RecurringAction action) {
        // 1. we create a stream of entities with given name and entity id
        Stream<RecurringAction> stream = getSession()
                .createQuery("SELECT dbAction FROM RecurringAction dbAction " +
                        "WHERE dbAction.name = :name " +
                        "AND :entityId IN (dbAction.minion.id, dbAction.group.id, dbAction.org.id)",
                        RecurringAction.class)
                .setParameter("name", action.getName())
                .setParameter("entityId", action.getEntityId())
                .stream();

        // 2. then we filter out the entity of given type
        List<RecurringAction> matches = stream
                .filter(entity -> entity.getTargetType() == action.getTargetType())
                .collect(Collectors.toList());

        // we can only have either 0 or 1 matches
        switch (matches.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(matches.get(0).getId());
            default:
                throw new IllegalStateException("More than 1 match returned");
        }
    }

    /**
     * Lookup recurring action with given taskomatic schedule name
     *
     * @param scheduleName the name of recurring action taskomatic schedule
     * @return optional of matching recurring action
     */
    public static Optional<RecurringAction> lookupByJobName(String scheduleName) {
        long id = Long.parseLong(scheduleName.replace(RecurringAction.RECURRING_ACTION_PREFIX, ""));
        return lookupById(id);
    }

    /**
     * Lookup internal state with given state name
     *
     * @param stateName the name of the state
     * @return optional of matching internal state
     */
    public static Optional<InternalState> lookupInternalStateByName(String stateName) {
        return getSession().createQuery("SELECT state FROM InternalState state " +
                "WHERE state.name = :name", InternalState.class)
                .setParameter("name", stateName)
                .uniqueResultOptional();
    }

    /**
     * Save a recurring action
     *
     * @param action the action to save
     */
    public static void save(RecurringAction action) {
        INSTANCE.saveObject(action);
    }

    /**
     * Delete a recurring action
     *
     * @param action the action to delete
     */
    public static void delete(RecurringAction action) {
        INSTANCE.removeObject(action);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
