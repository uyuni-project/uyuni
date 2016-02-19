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

import static com.suse.manager.webui.utils.SparkApplicationHelper.setup;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withOrgAdmin;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withProductAdmin;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.head;
import static spark.Spark.post;
import static spark.Spark.put;

import com.suse.manager.webui.controllers.DownloadController;
import com.suse.manager.webui.controllers.MinionsAPI;
import com.suse.manager.webui.controllers.StateCatalogController;
import com.suse.manager.webui.controllers.StatesAPI;
import com.suse.manager.webui.controllers.SubscriptionMatchingController;
import com.suse.manager.webui.controllers.MinionController;
import com.suse.manager.webui.controllers.VirtualHostManagerController;

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
        get("/manager/minions", withCsrfToken(MinionController::list), jade);
        // Remote command page
        get("/manager/minions/cmd", withCsrfToken(MinionController::cmd), jade);
        get("/manager/minions/:id", MinionController::show);
        post("/manager/minions/:id/accept", MinionController::accept);
        post("/manager/minions/:id/reject", MinionController::reject);
        post("/manager/minions/:id/delete", MinionController::destroy);

        // Package Management
        get("/manager/systems/details/packages",
                withCsrfToken(MinionController::packages),
                jade);

        // Minion APIs
        post("/manager/api/minions/cmd", withUser(MinionsAPI::run));
        get("/manager/api/minions/match", withUser(MinionsAPI::match));

        // States API
        post("/manager/api/states/apply", StatesAPI::apply);
        get("/manager/api/states/packages", StatesAPI::packages);
        post("/manager/api/states/packages/save", withUser(StatesAPI::save));
        get("/manager/api/states/packages/match", StatesAPI::match);

        // Download endpoint
        get("/manager/download/:channel/getPackage/:file",
                DownloadController::downloadPackage);
        get("/manager/download/:channel/repodata/:file",
                DownloadController::downloadMetadata);
        head("/manager/download/:channel/getPackage/:file",
                DownloadController::downloadPackage);
        head("/manager/download/:channel/repodata/:file",
                DownloadController::downloadMetadata);

        // Virtual Host Managers
        get("/manager/vhms",
                withCsrfToken(withOrgAdmin(VirtualHostManagerController::list)),
                jade);
        get("/manager/vhms/add",
                withCsrfToken(withOrgAdmin(VirtualHostManagerController::add)),
                jade);
        post("/manager/vhms",
                withCsrfToken(withUser(VirtualHostManagerController::create)),
                jade);
        get("/manager/vhms/:id",
                withCsrfToken(withOrgAdmin(VirtualHostManagerController::show)),
                jade);
        post("/manager/vhms/:id/delete",
                withOrgAdmin(VirtualHostManagerController::delete));
        post("/manager/vhms/:id/refresh",
                withOrgAdmin(VirtualHostManagerController::refresh));

        // Subscription Matching
        get("/manager/subscription-matching",
                withProductAdmin(SubscriptionMatchingController::show), jade);
        get("/manager/subscription-matching/data",
                withProductAdmin(SubscriptionMatchingController::data));
        get("/manager/subscription-matching/:filename",
                withProductAdmin(SubscriptionMatchingController::csv));
        post("/manager/subscription-matching/schedule-matcher-run",
                withProductAdmin(SubscriptionMatchingController::scheduleMatcherRun));
        post("/manager/subscription-matching/pins",
                withProductAdmin(SubscriptionMatchingController::createPin));
        post("/manager/subscription-matching/pins/:id/delete",
                withProductAdmin(SubscriptionMatchingController::deletePin));

        // Salt state catalog
        get("/manager/state_catalog",
                withOrgAdmin(StateCatalogController::list), jade);
        get("/manager/state_catalog/data",
                withOrgAdmin(StateCatalogController::data));

        get("/manager/state_catalog/state",
                withCsrfToken(withOrgAdmin(StateCatalogController::add)), jade);
        get("/manager/state_catalog/state/:name",
                withCsrfToken(withOrgAdmin(StateCatalogController::edit)), jade);
        post("/manager/state_catalog/state",
                withOrgAdmin(StateCatalogController::create));
        put("/manager/state_catalog/state/:name",
                withOrgAdmin(StateCatalogController::update));
        delete("/manager/state_catalog/state/:name",
                withOrgAdmin(StateCatalogController::delete));
    }
}
