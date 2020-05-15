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

import com.redhat.rhn.FaultException;

import com.suse.manager.model.maintenance.MaintenanceSchedule;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An {@link RuntimeException} bearing a set of {@link MaintenanceSchedule}s.
 *
 * Typically thrown, when scheduling an action at the date that doesn't fit to Maintenance window of
 * some systems.
 *
 * This exception also serves as a XMLRPC Fault
 */
public class NotInMaintenanceModeException extends FaultException {

    /**
     * Standard constructor
     *
     * @param schedules the {@link MaintenanceSchedule}s
     * @param date the scheduling date
     */
    public NotInMaintenanceModeException(Set<MaintenanceSchedule> schedules, Date date) {
        super(1334,
                "not_in_maintenance",
                String.format("Schedules '%s' are not matching date %s ",
                        scheduleNames(schedules), date));
    }

    private static String scheduleNames(Set<MaintenanceSchedule> schedulesIn) {
        return schedulesIn.stream().map(s -> s.getName()).collect(Collectors.joining(","));
    }
}
