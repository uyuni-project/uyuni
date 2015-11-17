/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.webui;

import static com.suse.manager.webui.utils.SparkApplicationUtils.setup;
import static spark.Spark.get;
import static spark.Spark.head;

import com.suse.manager.webui.controllers.DownloadController;
import com.suse.manager.webui.controllers.MinionsAPI;
import com.suse.manager.webui.controllers.MinionsController;

import spark.servlet.SparkApplication;
import spark.template.jade.JadeTemplateEngine;

/**
 * Router class defining the web UI routes.
 */
public class Router implements SparkApplication {

    /**
     * Invoked from the SparkFilter. Add routes here.
     */
    @Override
    public void init() {
        JadeTemplateEngine jade = setup();

        // Salt Master pages
        get("/manager/minions", MinionsController::listMinions, jade);
        get("/manager/minions/overview/:minion", MinionsController::systemOverview);
        get("/manager/minions/accept/:minion", MinionsController::acceptMinion);
        get("/manager/minions/delete/:minion", MinionsController::deleteMinion);
        get("/manager/minions/reject/:minion", MinionsController::rejectMinion);

        // Minion APIs
        get("/manager/api/minions/cmd", MinionsAPI::run);
        get("/manager/api/minions/match", MinionsAPI::match);

        // Remote command page
        get("/manager/minions/cmd", MinionsController::remoteCommands, jade);

        // Download endpoint
        get("/manager/download/:channel/getPackage/:file",
                DownloadController::downloadPackage);
        get("/manager/download/:channel/repodata/:file",
                DownloadController::downloadMetadata);
        head("/manager/download/:channel/getPackage/:file",
                DownloadController::downloadPackage);
        head("/manager/download/:channel/repodata/:file",
                DownloadController::downloadMetadata);
    }
}
