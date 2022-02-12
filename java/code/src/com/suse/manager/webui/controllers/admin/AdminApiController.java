/*
 * Copyright (c) 2019 SUSE LLC
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

package com.suse.manager.webui.controllers.admin;

import com.redhat.rhn.taskomatic.TaskomaticApi;

import com.suse.manager.admin.PaygAdminManager;
import com.suse.manager.webui.controllers.admin.handlers.MonitoringApiController;
import com.suse.manager.webui.controllers.admin.handlers.PaygApiContoller;

/**
 * Spark controller Admin Api.
 */
public class AdminApiController {

    private AdminApiController() { }

    /** Invoked from Router. Init routes for ContentManagement Api.
     * @param taskomaticApi*/
    public static void initRoutes(TaskomaticApi taskomaticApi) {
        // monitoring
        MonitoringApiController.initRoutes();

        PaygApiContoller paygApiContoller = new PaygApiContoller(new PaygAdminManager(taskomaticApi));
        paygApiContoller.initRoutes();

    }
}
