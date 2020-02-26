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
import com.redhat.rhn.domain.recurringactions.GroupRecurringAction;
import com.redhat.rhn.domain.recurringactions.MinionRecurringAction;
import com.redhat.rhn.domain.recurringactions.OrgRecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import java.util.List;

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
     * Create a minimal {@link RecurringAction} of given type. // todo better comment?
     *
     * @param type the Recurring Action type
     * @param entityId the ID of the target entity
     * @param user the creator
     * @return the newly created {@link RecurringAction}
     */
    public static RecurringAction createRecurringAction(RecurringAction.TYPE type, long entityId, User user) {
        switch (type) {
            case MINION:
                return createMinionRecurringAction(entityId, user);
        }

        throw new UnsupportedOperationException("type not supported");
    }

    /**
     * Create a minimal minion recurring action
     *  @param minionId id of the minion
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
            throw new PermissionException("Minion not accessible to user", e);
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
            throw new PermissionException("User does not have access to group");
        }
        try {
            /* Check if user has permission to access the group */
            groupManager.lookup(groupId, user);
            return RecurringActionFactory.listGroupRecurringActions(groupId);
        }
        catch (LookupException e) {
            throw new PermissionException("User does not have access to group", e);
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

    // checks perms & saves & schedules
    // all create/update operations must go through this!

    /**
     * Checks permissions on given {@link RecurringAction}, saves it and schedules corresponding taskomatic job.
     *
     * @param action the action
     * @param cron the cron string
     * @param user the user performing the operation
     * @throws PermissionException if the user does not have permission to save the action
     * @throws TaskomaticApiException when there is a problem with taskomatic during scheduling
     */
    public static void saveAndSchedule(RecurringAction action, String cron, User user) throws TaskomaticApiException {
        if (action instanceof MinionRecurringAction) {
            MinionRecurringAction minionAction = (MinionRecurringAction) action;
            if (!SystemManager.isAvailableToUser(user, minionAction.getMinion().getId())) {
                throw new PermissionException("Minion not accessible to user");
            }
        }
        // todo extend for other types
        // todo rewrite using inheritance (e.g. RecurringAction.canUserModify(user), or canAccess(user)?)
        // todo check for dupe names

        RecurringActionFactory.save(action);

        // todo test this codepath (when tasko throws an exception)
        taskomaticApi.scheduleSatBunch(user, action.computeTaskoScheduleName(), "recurring-state-apply-bunch", cron);
    }
}
