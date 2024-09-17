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
import com.redhat.rhn.common.util.validation.password.PasswordPolicy;
import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.domain.cloudpayg.PaygSshDataFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.taskomatic.TaskomaticApi;

import com.suse.manager.admin.PaygAdminManager;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.controllers.admin.mappers.PaygResponseMappers;
import com.suse.manager.webui.utils.FlashScopeHelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
        get("/manager/admin/config/monitoring",
                withUserPreferences(withCsrfToken(withOrgAdmin(AdminViewsController::showMonitoring))), jade);
        get("/manager/admin/config/password-policy",
                withUserPreferences(withCsrfToken(withOrgAdmin(AdminViewsController::showPasswordPolicy))), jade);
        get("/manager/admin/setup/payg",
                withUserPreferences(withCsrfToken(withOrgAdmin(AdminViewsController::listPayg))), jade);
        get("/manager/admin/setup/payg/create",
                withUserPreferences(withCsrfToken(withOrgAdmin(AdminViewsController::createPayg))), jade);
        get("/manager/admin/setup/payg/:id",
                withUserPreferences(withCsrfToken(withOrgAdmin(AdminViewsController::showPayg))), jade);
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
        return new ModelAndView(data, "controllers/admin/templates/monitoring.jade");
    }

    /**
     * Show password policy tab.
     * @param request http request
     * @param response http response
     * @param user current user
     * @return the view to show
     */
    public static ModelAndView showPasswordPolicy(Request request, Response response, User user) {
        PasswordPolicy pc = PasswordPolicy.buildPasswordPolicyFromSatFactory();
        Map<String, Object> data = new HashMap<>();
        data.put("minLength", pc.getMinLength());
        data.put("maxLength", pc.getMaxLength());
        data.put("digitsFlag", pc.isDigitFlag());
        data.put("lowerCharFlag", pc.isLowerCharFlag());
        data.put("upperCharFlag", pc.isUpperCharFlag());
        data.put("consecutiveCharFlag", pc.isConsecutiveCharsFlag());
        data.put("specialCharFlag", pc.isSpecialCharFlag());
        data.put("specialCharList", pc.getSpecialChars());
        data.put("restrictedOccurrenceFlag", pc.isRestrictedOccurrenceFlag());
        data.put("maxCharOccurrence", pc.getMaxCharacterOccurrence());
        return new ModelAndView(data, "controllers/admin/templates/password-policy.jade");
    }

    /**
     * show list of saved payg ssh connection data
     * @param request
     * @param response
     * @param user
     * @return list of payg ssh connection data
     */
    public static ModelAndView listPayg(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        List<PaygSshData> payg = PAYG_ADMIN_MANAGER.list();
        data.put("flashMessage", FlashScopeHelper.flash(request));
        data.put("contentPaygInstances", GSON.toJson(PaygResponseMappers.mapPaygPropertiesResumeFromDB(payg)));
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
}
