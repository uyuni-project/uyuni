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

package com.redhat.rhn.frontend.struts;

import com.redhat.rhn.common.util.DatePicker;
import com.redhat.rhn.domain.server.Server;

import com.suse.manager.maintenance.MaintenanceManager;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * Helper class for maintenance windows in the Struts area.
 */
public class MaintenanceWindowHelper {

    private static MaintenanceManager maintenanceManager = new MaintenanceManager();

    private MaintenanceWindowHelper() { }

    /**
     * Given the systems, populate the request object with available maintenance windows.
     *
     * @param request the Struts request
     * @param systemIds list of {@link Server}s
     */
    public static void prepopulateMaintenanceWindows(HttpServletRequest request,
            Set<Long> systemIds) {
        try {
            maintenanceManager
                    .calculateUpcomingMaintenanceWindows(systemIds)
                    .ifPresent(windows -> {
                        request.setAttribute(DatePicker.SCHEDULE_TYPE, DatePicker.ScheduleType.ACTION_CHAIN.toString());
                        request.setAttribute("maintenanceWindows", windows);
                    });
        }
        catch (IllegalStateException e) {
            request.setAttribute("maintenanceWindowsMultiSchedules", true);
        }
    }
}
