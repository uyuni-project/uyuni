/**
 * Copyright (c) 2018 SUSE LLC
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
package com.suse.manager.webui.controllers.contentmanagement;

import com.suse.manager.webui.controllers.contentmanagement.handlers.EnvironmentApiController;
import com.suse.manager.webui.controllers.contentmanagement.handlers.FilterApiController;
import com.suse.manager.webui.controllers.contentmanagement.handlers.ProjectActionsApiController;
import com.suse.manager.webui.controllers.contentmanagement.handlers.ProjectApiController;
import com.suse.manager.webui.controllers.contentmanagement.handlers.ProjectSourcesApiController;

/**
 * Spark controller ContentManagement Api.
 */
public class ContentManagementApiController {


    private ContentManagementApiController() {
    }

    /** Invoked from Router. Init routes for ContentManagement Api.*/
    public static void initRoutes() {
        ProjectApiController.initRoutes();
        ProjectSourcesApiController.initRoutes();
        FilterApiController.initRoutes();
        EnvironmentApiController.initRoutes();
        ProjectActionsApiController.initRoutes();
    }

}
