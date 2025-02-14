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

import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withOrgAdmin;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.domain.cloudpayg.PaygSshDataFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.setup.ProxySettingsDto;
import com.redhat.rhn.manager.setup.ProxySettingsManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;

import com.suse.manager.admin.PaygAdminManager;
import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.IssPeripheral;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.controllers.admin.beans.HubResponse;
import com.suse.manager.webui.controllers.admin.mappers.PaygResponseMappers;
import com.suse.manager.webui.utils.FlashScopeHelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Spark controller class for admin pages.
 */
public class AdminViewsController {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .serializeNulls()
            .create();

    private static final PaygAdminManager PAYG_ADMIN_MANAGER = new PaygAdminManager(new TaskomaticApi());

    private AdminViewsController() { }

    /**
     * @param jade JadeTemplateEngine
     * Invoked from Router. Init routes for Admin Views.
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/admin/hub/hub-details",
                withUserPreferences(withCsrfToken(withOrgAdmin(AdminViewsController::showISSv3Hub))), jade);
        get("/manager/admin/hub/peripherals",
                withUserPreferences(withCsrfToken(withOrgAdmin(AdminViewsController::showISSv3Peripherals))), jade);
        get("/manager/admin/hub/peripherals/create",
                withUserPreferences(withCsrfToken(withOrgAdmin(AdminViewsController::createISSv3Peripheral))), jade);
        get("/manager/admin/hub/peripherals/update",
                withUserPreferences(withCsrfToken(withOrgAdmin(AdminViewsController::updateISSv3Peripheral))), jade);
        get("/manager/admin/config/monitoring",
                withUserPreferences(withCsrfToken(withOrgAdmin(AdminViewsController::showMonitoring))), jade);
        get("/manager/admin/setup/payg",
                withUserPreferences(withCsrfToken(withOrgAdmin(AdminViewsController::listPayg))), jade);
        get("/manager/admin/setup/payg/create",
                withUserPreferences(withCsrfToken(withOrgAdmin(AdminViewsController::createPayg))), jade);
        get("/manager/admin/setup/payg/:id",
                withUserPreferences(withCsrfToken(withOrgAdmin(AdminViewsController::showPayg))), jade);
        get("/manager/admin/setup/proxy",
                withUserPreferences(withCsrfToken(withOrgAdmin(AdminViewsController::showProxy))), jade);
    }

    /**
     * Show monitoring tab.
     * @param request http request
     * @param response http response
     * @param user current user
     * @return the view to show
     */
    public static ModelAndView showMonitoring(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("isUyuni", ConfigDefaults.get().isUyuni());
        return new ModelAndView(data, "controllers/admin/templates/issv3/monitoring.jade");
    }

    /**
     * Show iss hub tab.
     * @param request http request
     * @param response http response
     * @param user current user
     * @return the view to show
     */
    public static ModelAndView showISSv3Hub(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        HubFactory hubFactory = new HubFactory();
        data.put("hub", hubFactory.lookupIssHub().orElse(null));
        return new ModelAndView(data, "controllers/admin/templates/issv3/hub-details.jade");
    }

    /**
     * Show iss peripheral tab.
     * @param request http request
     * @param response http response
     * @param user current user
     * @return the view to show
     */
    public static ModelAndView showISSv3Peripherals(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        HubFactory hubFactory = new HubFactory();
        Type listType = new TypeToken<List<IssPeripheral>>() { }.getType();
        data.put("hubs", GSON.toJson(hubFactory.listPeripherals(), listType));
        return new ModelAndView(data, "controllers/admin/templates/issv3/list-peripherals.jade");
    }

    /**
     * Show iss peripheral tab.
     * @param request http request
     * @param response http response
     * @param user current user
     * @return the view to show
     */
    public static ModelAndView createISSv3Peripheral(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        return new ModelAndView(data, "controllers/admin/templates/issv3/create-peripheral.jade");
    }

    /**
     * Show iss peripheral tab.
     * @param request http request
     * @param response http response
     * @param user current user
     * @return the view to show
     */
    public static ModelAndView updateISSv3Peripheral(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        //TODO: get peripheral data
        data.put("peripheral", null);
        return new ModelAndView(data, "controllers/admin/templates/issv3/update-peripheral.jade");
    }


    /**
     * show list of saved payg ssh connection data
     * @param request
     * @param response
     * @param user
     * @return list of payg ssh connection data
     */
    public static ModelAndView listPayg(Request request, Response response, User user) {
        HubFactory hubFactory = new HubFactory();
        Map<String, Object> data = new HashMap<>();
        List<PaygSshData> payg = PAYG_ADMIN_MANAGER.list();
        data.put("flashMessage", FlashScopeHelper.flash(request));
        data.put("contentPaygInstances", GSON.toJson(PaygResponseMappers.mapPaygPropertiesResumeFromDB(payg)));
        data.put("isIssPeripheral", hubFactory.isISSPeripheral());
        return new ModelAndView(data, "controllers/admin/templates/payg_list.jade");
    }

    /**
     * Reder new payg ssh connection data create page
     * @param request
     * @param response
     * @param user
     * @return return the form to create a new payg ssh connection data
     */
    public static ModelAndView createPayg(Request request, Response response, User user) {
        return new ModelAndView(new HashMap<>(), "controllers/admin/templates/payg_create.jade");
    }

    /**
     * retrieve information about one payg ssh connection data
     * @param request
     * @param response
     * @param user
     * @return one payg ssh connection data
     */
    public static ModelAndView showPayg(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();

        Integer sshPaygId = Integer.valueOf(request.params("id"));
        Optional<PaygSshData> paygSshDataOptional = PaygSshDataFactory.lookupById(sshPaygId);

        paygSshDataOptional.ifPresent(paygSshData -> data.put("paygInstance",
                GSON.toJson(PaygResponseMappers.mapPaygPropertiesFullFromDB(paygSshData))));
        if (!paygSshDataOptional.isEmpty()) {
            data.put("wasFreshlyCreatedMessage", FlashScopeHelper.flash(request));
        }
        else {
            data.put("paygInstance", GSON.toJson(null));
        }
        return new ModelAndView(data, "controllers/admin/templates/payg.jade");
    }

    /**
     * Show proxy tab.
     * @param request http request
     * @param response http response
     * @param user current user
     * @return the view to show
     */
    public static ModelAndView showProxy(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        ProxySettingsDto proxySettings = ProxySettingsManager.getProxySettings();
        data.put("proxySettings", GSON.toJson(proxySettings));
        return new ModelAndView(data, "controllers/admin/templates/proxy.jade");
    }
}
