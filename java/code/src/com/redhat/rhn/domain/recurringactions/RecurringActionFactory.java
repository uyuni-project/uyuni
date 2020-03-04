/**
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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.user.User;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ServerFactory - the class used to fetch and store
 * com.redhat.rhn.domain.server.RecurringAction objects from the database.
 * @version $Rev$
 */
public class RecurringActionFactory extends HibernateFactory {

    private static final Logger LOG = Logger.getLogger(RecurringActionFactory.class);
    private static final RecurringActionFactory INSTANCE = new RecurringActionFactory();

    /**
     * List minion recurring actions with minion id.
     *
     * @param id - id of the minion
     * @return list of minion recurring actions
     */
    public static List<MinionRecurringAction> listMinionRecurringActions(Long id) {
       return getSession().createQuery("SELECT action FROM MinionRecurringAction action " +
               "WHERE action.minion.id = :mid " +
               "ORDER BY action.id DESC")
               .setParameter("mid", id)
               .list();
    }

    /**
     * List group recurring actions with group id.
     *
     * @param id - id of the group
     * @return list of group recurring actions
     */
    public static List<GroupRecurringAction> listGroupRecurringActions(Long id) {
        return getSession().createQuery("SELECT action FROM GroupRecurringAction action " +
                "WHERE action.group.id = :gid " +
                "ORDER BY action.id DESC")
                .setParameter("gid", id)
                .list();
    }

    /**
     * List org recurring actions with org id.
     *
     * @param id - id of the organization
     * @return list of org recurring actions
     */
    public static List<OrgRecurringAction> listOrgRecurringActions(Long id) {
        return getSession().createQuery("SELECT action FROM OrgRecurringAction action " +
                "WHERE action.org.id = :oid " +
                "ORDER BY action.id DESC")
                .setParameter("oid", id)
                .list();
    }

    /**
     * List all {@link RecurringAction}s that are associated with entities
     * belonging to the {@link Org} of given {@link User}.
     *
     * @param user the user
     * @return the list of {@link RecurringAction}s
     */
    public static List<? extends RecurringAction> listAllRecurringActions(User user) {
        Org org = user.getOrg();
        Stream<? extends RecurringAction> orgActions = getSession().createQuery(
                "SELECT orgAction FROM OrgRecurringAction orgAction " +
                        "WHERE orgAction.org = :org " +
                        "ORDER by orgAction.id DESC")
                .setParameter("org", org)
                .stream();

        Stream<? extends RecurringAction> groupActions = getSession().createQuery(
                "SELECT groupAction FROM GroupRecurringAction groupAction " +
                        "WHERE groupAction.group.org = :org " +
                        "ORDER by groupAction.id DESC")
                .setParameter("org", org)
                .stream();

        Stream<? extends RecurringAction> minionActions = getSession().createQuery(
                "SELECT minionAction FROM MinionRecurringAction minionAction " +
                        "WHERE minionAction.minion.org = :org " +
                        "ORDER by minionAction.id DESC")
                .setParameter("org", org)
                .stream();

        return Stream.concat(orgActions, Stream.concat(groupActions, minionActions)).collect(Collectors.toList());
    }

    /**
     * Lookup recurring action with given id.
     *
     * @param id - id of the recurring action
     * @return optional of matching recurring action
     */
    public static Optional<RecurringAction> lookupById(long id) {
        return getSession().createQuery("SELECT action FROM RecurringAction action " +
                "WHERE action.id = :id")
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
        Stream<RecurringAction> stream = getSession().createQuery("SELECT dbAction FROM RecurringAction dbAction " +
                "WHERE dbAction.name = :name " +
                "AND :entityId IN (dbAction.minion.id, dbAction.group.id, dbAction.org.id)")
                .setParameter("name", action.getName())
                .setParameter("entityId", action.getEntityId())
                .stream();

        // 2. then we filter out the entity of given type
        List<RecurringAction> matches = stream
                .filter(entity -> entity.getType() == action.getType())
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
