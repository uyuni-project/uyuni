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
import com.suse.manager.webui.controllers.FormulaCatalogController;
import com.suse.manager.webui.controllers.MinionsAPI;
import com.suse.manager.webui.controllers.StateCatalogController;
import com.suse.manager.webui.controllers.StatesAPI;
import com.suse.manager.webui.controllers.SubscriptionMatchingController;
import com.suse.manager.webui.controllers.TaskoTop;
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

        // Minions
        get("/manager/minions",
                withCsrfToken(withUser(MinionController::list)),
                jade);
        get("/manager/minions/bootstrap",
                withCsrfToken(withOrgAdmin(MinionController::bootstrap)),
                jade);
        get("/manager/minions/cmd",
                withCsrfToken(MinionController::cmd),
                jade);
        get("/manager/minions/:id",
                MinionController::show);

        // Minions API
        post("/manager/api/minions/bootstrap", withOrgAdmin(MinionsAPI::bootstrap));
        post("/manager/api/minions/cmd", withUser(MinionsAPI::run));
        get("/manager/api/minions/match", withUser(MinionsAPI::match));
        get("/manager/api/minions/keys", withUser(MinionsAPI::listKeys));
        post("/manager/api/minions/keys/:target/accept", withOrgAdmin(MinionsAPI::accept));
        post("/manager/api/minions/keys/:target/reject", withOrgAdmin(MinionsAPI::reject));
        post("/manager/api/minions/keys/:target/delete", withOrgAdmin(MinionsAPI::delete));

        // States
        get("/manager/systems/details/packages",
                withCsrfToken(MinionController::packageStates),
                jade);
        get("/manager/systems/details/custom",
                withCsrfToken(MinionController::minionCustomStates),
                jade);
        get("/manager/systems/details/highstate",
                withCsrfToken(MinionController::highstate),
                jade);
        get("/manager/multiorg/details/custom",
                withCsrfToken(MinionController::orgCustomStates),
                jade);
        get("/manager/yourorg/custom",
                withCsrfToken(withUser(MinionController::yourOrgCustomStates)),
                jade);
        get("/manager/groups/details/custom",
                withCsrfToken(withUser(MinionController::serverGroupCustomStates)),
                jade);

        // States API
        post("/manager/api/states/apply", withUser(StatesAPI::apply));
        get("/manager/api/states/match", withUser(StatesAPI::matchStates));
        post("/manager/api/states/save", withUser(StatesAPI::saveCustomStates));
        get("/manager/api/states/packages", StatesAPI::packages);
        post("/manager/api/states/packages/save", withUser(StatesAPI::savePackages));
        get("/manager/api/states/packages/match", StatesAPI::matchPackages);
        get("/manager/api/states/highstate", StatesAPI::showHighstate);

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
                withProductAdmin(SubscriptionMatchingController::show),
                jade);
        get("/manager/subscription-matching/:filename",
                withProductAdmin(SubscriptionMatchingController::csv));

        // Subscription Matching API
        get("/manager/api/subscription-matching/data",
                withProductAdmin(SubscriptionMatchingController::data));
        post("/manager/api/subscription-matching/schedule-matcher-run",
                withProductAdmin(SubscriptionMatchingController::scheduleMatcherRun));
        post("/manager/api/subscription-matching/pins",
                withProductAdmin(SubscriptionMatchingController::createPin));
        post("/manager/api/subscription-matching/pins/:id/delete",
                withProductAdmin(SubscriptionMatchingController::deletePin));

        // States Catalog
        get("/manager/state-catalog",
                withOrgAdmin(StateCatalogController::list), jade);
        get("/manager/state-catalog/state",
                withCsrfToken(withOrgAdmin(StateCatalogController::add)), jade);
        get("/manager/state-catalog/state/:name",
                withCsrfToken(withOrgAdmin(StateCatalogController::edit)), jade);

        // States Catalog API
        get("/manager/api/state-catalog/data",
                withOrgAdmin(StateCatalogController::data));
        get("/manager/api/state-catalog/state/:name/content",
                withUser(StateCatalogController::content));
        post("/manager/api/state-catalog/state",
                withOrgAdmin(StateCatalogController::create));
        put("/manager/api/state-catalog/state/:name",
                withOrgAdmin(StateCatalogController::update));
        delete("/manager/api/state-catalog/state/:name",
                withOrgAdmin(StateCatalogController::delete));

        // TaskoTop
        get("/manager/admin/runtime-status",
                withOrgAdmin(TaskoTop::show), jade);
        get("/manager/api/admin/runtime-status/data",
                withOrgAdmin(TaskoTop::data));

        // Download endpoint
        get("/manager/download/:channel/getPackage/:file",
                DownloadController::downloadPackage);
        get("/manager/download/:channel/repodata/:file",
                DownloadController::downloadMetadata);
        head("/manager/download/:channel/getPackage/:file",
                DownloadController::downloadPackage);
        head("/manager/download/:channel/repodata/:file",
                DownloadController::downloadMetadata);

        // Formula catalog
        get("/manager/formula-catalog",
                withOrgAdmin(FormulaCatalogController::list), jade);
        get("/manager/formula-catalog/formula/:name",
                withCsrfToken(withOrgAdmin(FormulaCatalogController::details)), jade);

        // Formula catalog API
        get("/manager/api/formula-catalog/data",
                withOrgAdmin(FormulaCatalogController::data));
        get("/manager/api/formula-catalog/formula/:name/data",
                withOrgAdmin(FormulaCatalogController::detailsData));

        // Formulas
        get("/manager/groups/details/formulas",
                withCsrfToken(withUser(MinionController::serverGroupFormulas)),
                jade);
        get("/manager/systems/details/formulas",
                withCsrfToken(withUser(MinionController::minionFormulas)),
                jade);
        get("/manager/groups/details/formula/:formula_id",
                withCsrfToken(withUser(MinionController::serverGroupFormula)),
                jade);
        get("/manager/systems/details/formula/:formula_id",
                withCsrfToken(MinionController::minionFormula),
                jade);

        // Formula API
        get("/manager/api/formulas/list/:targetType/:id",
                withOrgAdmin(MinionController::listSelectedFormulas));
        get("/manager/api/formulas/form/:targetType/:id/:formula_id",
                withUser(MinionController::formulaData));
        post("/manager/api/formulas/select",
                withUser(MinionController::saveSelectedFormulas));
        post("/manager/api/formulas/save",
                withUser(MinionController::saveFormula));
    }
}
