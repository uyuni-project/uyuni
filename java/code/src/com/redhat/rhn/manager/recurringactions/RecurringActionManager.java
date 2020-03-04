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

package com.redhat.rhn.manager.recurringactions;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.recurringactions.GroupRecurringAction;
import com.redhat.rhn.domain.recurringactions.MinionRecurringAction;
import com.redhat.rhn.domain.recurringactions.OrgRecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.hibernate.HibernateException;

import java.util.List;
import java.util.Optional;

/**
 * RecurringActionManager
 * @version $Rev$
 */
public class RecurringActionManager {

    private static  TaskomaticApi taskomaticApi = new TaskomaticApi();

    /**
     * Set the {@link TaskomaticApi} instance to use. Only needed for unit tests.
     * @param taskomaticApiIn the {@link TaskomaticApi}
     */
    public static void setTaskomaticApi(TaskomaticApi taskomaticApiIn) {
        taskomaticApi = taskomaticApiIn;
    }

    /**
     * Private constructor.
     */
    private RecurringActionManager() {
    }

    /**
     * Create a minimal {@link RecurringAction} of given type.
     *
     * @param type the Recurring Action type
     * @param entityId the ID of the target entity
     * @param user the creator
     * @return the newly created {@link RecurringAction}
     */
    public static RecurringAction createRecurringAction(RecurringAction.Type type, long entityId, User user) {
        switch (type) {
            case MINION:
                return createMinionRecurringAction(entityId, user);
            case GROUP:
                return createGroupRecurringAction(entityId, user);
            case ORG:
                return createOrgRecurringAction(entityId, user);
            default:
                throw new UnsupportedOperationException("type not supported");
        }
    }

    /**
     * Create a minimal minion recurring action
     *
     * @param minionId id of the minion
     * @param user the user
     * @return
     */
    private static MinionRecurringAction createMinionRecurringAction(long minionId, User user) {
        MinionServer minion = MinionServerFactory.lookupById(minionId)
                .orElseThrow(() -> new EntityNotExistsException(MinionServer.class, minionId));
        MinionRecurringAction action = new MinionRecurringAction(false, true, minion, user);
        return action;
    }

    /**
     * Create a minimal group recurring action
     *
     * @param groupId id of the group
     * @param user the user
     * @return
     */
    private static GroupRecurringAction createGroupRecurringAction(long groupId, User user) {
        ServerGroup group = ServerGroupFactory.lookupByIdAndOrg(groupId, user.getOrg());
        if (group == null) {
            throw new EntityNotExistsException(ServerGroup.class, groupId);
        }
        GroupRecurringAction action = new GroupRecurringAction(false, true, group, user);
        return action;
    }

    /**
     * Create a minimal org recurring action
     *
     * @param orgId id of the organization
     * @param user the user
     * @return
     */
    private static OrgRecurringAction createOrgRecurringAction(long orgId, User user) {
        try {
            Org org = OrgFactory.lookupById(orgId);
            OrgRecurringAction action = new OrgRecurringAction(false, true, org, user);
            return action;
        }
        catch (HibernateException e) {
            throw new EntityNotExistsException(Org.class, orgId);
        }
    }

    /**
     * List minion recurring action with minion id
     *
     * @param minionId id of the minion
     * @param user the user
     * @return list of minion recurring actions
     */
    public static List<MinionRecurringAction> listMinionRecurringActions(long minionId, User user) {
        try {
            SystemManager.ensureAvailableToUser(user, minionId);
        }
        catch (LookupException e) {
            throw new PermissionException(String.format("Minion id %d not accessible to user ", minionId), e);
        }
        return RecurringActionFactory.listMinionRecurringActions(minionId);
    }

    /**
     * List group recurring action with group id
     *
     * @param groupId id of the group
     * @param user the user
     * @return list of group recurring actions
     */
    public static List<GroupRecurringAction> listGroupRecurringActions(long groupId, User user) {
        ServerGroupManager groupManager = ServerGroupManager.getInstance();
        if (!user.hasRole(RoleFactory.SYSTEM_GROUP_ADMIN)) {
            throw new PermissionException(String.format("User does not have access to group id %d", groupId));
        }
        try {
            /* Check if user has permission to access the group */
            groupManager.lookup(groupId, user);
            return RecurringActionFactory.listGroupRecurringActions(groupId);
        }
        catch (LookupException e) {
            throw new PermissionException(String.format("User does not have access to group id %d", groupId), e);
        }
    }

    /**
     * List group recurring action with org id
     *
     * @param orgId id of the organization
     * @param user the user
     * @return list of org recurring actions
     */
    public static List<OrgRecurringAction> listOrgRecurringActions(long orgId, User user) {
        if (!user.hasRole(RoleFactory.ORG_ADMIN)) {
            throw new PermissionException("Org not accessible to user");
        }
        return RecurringActionFactory.listOrgRecurringActions(orgId);
    }

    /**
     * List all {@link RecurringAction}s visible to the given user
     *
     * @param user the user
     * @return the actions visible to the user
     */
    public static List<? extends RecurringAction> listAllRecurringActions(User user) {
        return RecurringActionFactory.listAllRecurringActions(user);
    }

    // checks perms & saves & schedules
    // all create/update operations must go through this!

    /**
     * Checks permissions on given {@link RecurringAction}, saves it and schedules corresponding taskomatic job.
     *
     * All save/update operations on {@link RecurringAction} must use this method
     * (to make sure the taskomatic schedule is updated as well).
     *
     * @param action the action
     * @param user the user performing the operation
     * @throws PermissionException if the user does not have permission to save the action
     * @throws EntityExistsException if equal entity (with different id) already exists
     * @throws TaskomaticApiException when there is a problem with taskomatic during scheduling
     */
    public static void saveAndSchedule(RecurringAction action, User user) throws TaskomaticApiException {
        if (!action.canAccess(user)) {
            throw new PermissionException(String.format("%s not accessible to user %s", action, user));
        }

        validateAction(action);

        RecurringActionFactory.save(action);

        // mainly the possible need for revert
        taskomaticApi.scheduleRecurringAction(action, user);
    }

    private static void validateAction(RecurringAction action) {
        Optional<Long> existingIdOpt = RecurringActionFactory.lookupEqualEntityId(action);
        Long actionId = action.getId();

        existingIdOpt.ifPresent(existingId -> {
            // Equal entity already exists. Throw an exception if:
            // - either given action is null (create scenario),
            // - or the ID of given action is different from the existing entity (update scenario).
            if (actionId == null || !actionId.equals(existingId)) {
                throw new EntityExistsException(String.format("Equal entity already exists: ID %d", existingId));
            }
        });
    }

    /**
     * Checks permission on given {@link RecurringAction}, deletes it and unschedules corresponding taskomatic job.
     *
     * @param action the action
     * @param user the user performing the action
     * @throws PermissionException if the user does not have permission to delete the action
     * @throws TaskomaticApiException when there is a problem with taskomatic during unscheduling
     */
    public static void deleteAndUnschedule(RecurringAction action, User user)  throws TaskomaticApiException {
        if (!action.canAccess(user)) {
            throw new PermissionException(String.format("%s not accessible to user %s", action, user));
        }
        RecurringActionFactory.delete(action);

        taskomaticApi.unscheduleRecurringAction(action, user);
    }

}
