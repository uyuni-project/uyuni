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
import com.redhat.rhn.domain.recurringactions.GroupRecurringAction;
import com.redhat.rhn.domain.recurringactions.MinionRecurringAction;
import com.redhat.rhn.domain.recurringactions.OrgRecurringAction;
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
    private static final RecurringActionManager INSTANCE = new RecurringActionManager();

    /**
     * Set the {@link TaskomaticApi} instance to use. Only needed for unit tests.
     * @param taskomaticApiIn the {@link TaskomaticApi}
     */
    public static void setTaskomaticApi(TaskomaticApi taskomaticApiIn) {
        taskomaticApi = taskomaticApiIn;
    }
    /**
     * Singleton Instance to get manager object
     * @return an instance of the manager
     */
    public static RecurringActionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Private constructor.
     */
    private RecurringActionManager() {
    }

    /**
     * Create a new minion recurring action
     *
     * @param minionId id of the minion
     * @param cron the cron expression
     * @param testMode if test mode
     * @param active is is active
     * @param user the user
     * @throws TaskomaticApiException if TaskoSchedule creation fails
     */
    public static void createMinionRecurringAction(long minionId, String cron, boolean testMode,
                                                   boolean active, User user) throws TaskomaticApiException {
        if (!SystemManager.isAvailableToUser(user, minionId)) {
            throw new PermissionException("Minion not accessible to user");
        }
        MinionServer minion = MinionServerFactory.lookupById(minionId)
                .orElseThrow(() -> new EntityNotExistsException(MinionServer.class, minionId));
        MinionRecurringAction action = new MinionRecurringAction(testMode, active, minion);
        RecurringActionFactory.save(action);

        taskomaticApi.scheduleSatBunch(user, action.computeTaskoScheduleName(), "recurring-state-apply-bunch", cron);
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
}
