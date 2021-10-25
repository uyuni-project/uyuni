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
package com.suse.manager.maintenance.rescheduling;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.model.maintenance.MaintenanceSchedule;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CancelRescheduleStrategy - Cancel the action for the system
 */
public class CancelRescheduleStrategy implements RescheduleStrategy {
    private static final Logger LOG = Logger.getLogger(CancelRescheduleStrategy.class);

    @Override
    public RescheduleStrategyType getType() {
        return RescheduleStrategyType.CANCEL;
    }

    @Override
    public RescheduleResult reschedule(User user, Map<Action, List<Server>> actionsServers,
            MaintenanceSchedule schedule) throws RescheduleException {
        RescheduleResult result = new RescheduleResult(getType().getLabel(), schedule.getName(), actionsServers);
        try {
            for (Action action: actionsServers.keySet()) {
                List<Long> serverIds = actionsServers.get(action).stream()
                        .map(s -> s.getId())
                        .collect(Collectors.toList());
                ActionManager.cancelActions(user, Collections.singletonList(action), Optional.of(serverIds));
                LOG.info(String.format("Cancel action '%s' for servers '%s'",
                        action.toString(), serverIds.toString()));
            }
        }
        catch (TaskomaticApiException | RuntimeException e) {
            LOG.error(e);
            throw new RescheduleException(e);
        }
        result.setSuccess(true);
        return result;
    }

}
