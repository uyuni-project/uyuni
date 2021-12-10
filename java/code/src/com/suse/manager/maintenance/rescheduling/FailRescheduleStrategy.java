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

import com.suse.manager.model.maintenance.MaintenanceSchedule;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * FailRescheduleStrategy - fallback which always fail to reschedule the action.
 */
public class FailRescheduleStrategy implements RescheduleStrategy {
    private static final Logger LOG = Logger.getLogger(CancelRescheduleStrategy.class);

    @Override
    public RescheduleResult reschedule(User user, Map<Action, List<Server>> actionsServers,
            MaintenanceSchedule schedule) throws RescheduleException {

        if (actionsServers.isEmpty()) {
            RescheduleResult result = new RescheduleResult(getType().getLabel(), schedule.getName(), actionsServers);
            result.setSuccess(true);
            return  result;
        }

        LOG.info("Rescheduling failed");
        throw new RescheduleException();
    }

    @Override
    public RescheduleStrategyType getType() {
        return RescheduleStrategyType.FAIL;
    }

}
