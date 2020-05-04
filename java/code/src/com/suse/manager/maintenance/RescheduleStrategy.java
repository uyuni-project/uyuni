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
package com.suse.manager.maintenance;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.model.maintenance.MaintenanceSchedule;

import java.util.List;
import java.util.Map;

/**
 * RescheduleStrategy - simple interface to implement strategy modules
 * to reschedule actions to a different point in time
 */
public interface RescheduleStrategy {

    /**
     * Try to reschedule the given action for the given system according the
     * implementation.
     *
     * Return true when the rescheduling was successful, otherwise false.
     *
     * @param user the user
     * @param actionServer the actions with list of affected systems
     * @param schedule the schedule where the server actions needs to apply to
     * @return true on success, otherwise false
     */
    public boolean reschedule(User user, Map<Action, List<Server>> actionServer, MaintenanceSchedule schedule);
}
