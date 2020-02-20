package com.redhat.rhn.manager.recurringactions;

import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.recurringactions.MinionRecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import java.util.List;

public class RecurringActionManager {

    private static final TaskomaticApi TASKOMATIC_API = new TaskomaticApi();

    public static void createMinionRecurringAction(long minionId, String cron, boolean testMode,
                                                   boolean active, User user) throws TaskomaticApiException {
        if (!SystemManager.isAvailableToUser(user, minionId)) {
            throw new PermissionException("Minion not accessible to user");
        }
        MinionServer minion = MinionServerFactory.lookupById(minionId)
                .orElseThrow(() -> new EntityNotExistsException(MinionServer.class, minionId));
        MinionRecurringAction action = new MinionRecurringAction(testMode, active, minion);
        RecurringActionFactory.save(action);

        TASKOMATIC_API.scheduleSatBunch(user, action.computeTaskoScheduleName(), "recurring-state-apply-bunch", cron);
    }

    public static List<MinionRecurringAction> listMinionRecurringAction(long minionId, User user) {
        if (!SystemManager.isAvailableToUser(user, minionId)) {
            throw new PermissionException("Minion not accessible to user");
        }
        return RecurringActionFactory.listMinionRecurringActions(minionId);
    }
}
