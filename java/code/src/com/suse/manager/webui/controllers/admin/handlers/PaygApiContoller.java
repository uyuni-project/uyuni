/*
 * Copyright (c) 2021 SUSE LLC
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

package com.suse.manager.webui.controllers.admin.handlers;

import static com.redhat.rhn.domain.role.RoleFactory.SAT_ADMIN;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.delete;
import static spark.Spark.post;
import static spark.Spark.put;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.domain.cloudpayg.PaygSshDataFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.taskomatic.TaskomaticApi;

import com.suse.manager.admin.PaygAdminManager;
import com.suse.manager.webui.controllers.admin.beans.PaygProperties;
import com.suse.manager.webui.controllers.admin.mappers.PaygResponseMappers;
import com.suse.manager.webui.controllers.contentmanagement.handlers.ValidationUtils;
import com.suse.manager.webui.utils.FlashScopeHelper;
import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;

import java.util.Optional;

import spark.Request;
import spark.Response;



public class PaygApiContoller {
    private static final LocalizationService LOC = LocalizationService.getInstance();
    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    private PaygAdminManager paygAdminManager;

    /**
     * default constructos
     */
    public PaygApiContoller() {
        this(new PaygAdminManager(new TaskomaticApi()));
    }

    /**
     * constructor with a pauy admin manager instance to be used
     * @param paygAdminManagerIn
     */
    public PaygApiContoller(PaygAdminManager paygAdminManagerIn) {
        this.paygAdminManager = paygAdminManagerIn;
    }

    /**
     * initialize all the API Routes for the pauyg support
     */
    public void initRoutes() {
        post("/manager/api/admin/config/payg",
                withUser(this::createPayg));
        delete("/manager/api/admin/config/payg/:id",
                withUser(this::removePaygInstance));
        put("/manager/api/admin/config/payg/:id",
                withUser(this::updatePayg));
    }

    /**
     * create a new payg ssh connection data instance
     * @param request
     * @param response
     * @param user
     * @return json string with the database id for the created data
     */
    public String createPayg(Request request, Response response, User user) {
        if (!user.hasRole(SAT_ADMIN)) {
            throw new PermissionException(SAT_ADMIN);
        }
        PaygProperties paygProperties = GSON.fromJson(request.body(), PaygProperties.class);
        try {
            PaygSshData payg = paygAdminManager.create(paygProperties);

            FlashScopeHelper.flash(
                    request,
                    LOC.getMessage("payg.ssh_data_created", payg.getHost())
            );

            return json(GSON, response, ResultJson.success(payg.getId()), new TypeToken<>() { });

        }
        catch (EntityExistsException error) {
            return json(GSON, response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error(LOC.getMessage("payg.host_exists")), new TypeToken<>() { });
        }
        catch (ValidatorException e) {
            return json(GSON, response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error(ValidationUtils.convertValidationErrors(e),
                            ValidationUtils.convertFieldValidationErrors(e)), new TypeToken<>() { });
        }
        catch (Exception e) {
            return json(GSON, response, HttpStatus.SC_BAD_REQUEST, ResultJson.error(e.getMessage()),
                    new TypeToken<>() { });
        }
    }

    /**
     * Remove a payg ssh connection data from the database
     * @param req
     * @param res
     * @param user
     * @return result message
     */
    public String removePaygInstance(Request req, Response res, User user) {
        if (!user.hasRole(SAT_ADMIN)) {
            throw new PermissionException(SAT_ADMIN);
        }
        Integer id = Integer.parseInt(req.params("id"));
        Optional<PaygSshData> paygSshDataOptional = PaygSshDataFactory.lookupById(id);
        if (paygSshDataOptional.isEmpty()) {
            return json(GSON, res, ResultJson.error(), new TypeToken<>() { });
        }

        boolean removingResult = paygAdminManager.delete(id);
        if (removingResult) {
            String successMessage = LOC.getMessage("payg.ssh_data_deleted", paygSshDataOptional.get().getHost());
            FlashScopeHelper.flash(
                    req,
                    successMessage
            );
            return json(GSON, res, ResultJson.successMessage(successMessage),
                    new TypeToken<>() { });
        }
        return json(GSON, res, ResultJson.error(), new TypeToken<>() { });
    }

    /**
     * Return the JSON with the result of updating the properties of a content project.
     * @param req the http request
     * @param res the http response
     * @param user the current user
     * @return the JSON data
     */
    public String updatePayg(Request req, Response res, User user) {
        if (!user.hasRole(SAT_ADMIN)) {
            throw new PermissionException(SAT_ADMIN);
        }
        Integer sshPaygId = Integer.parseInt(req.params("id"));
        try {
            PaygProperties newDescription = GSON.fromJson(req.body(), PaygProperties.class);
            PaygSshData paygDB = paygAdminManager.setDetails(sshPaygId, newDescription);
            return json(GSON, res,
                    ResultJson.success(PaygResponseMappers.mapPaygPropertiesFullFromDB(paygDB)),
                    new TypeToken<>() { });
        }
        catch (EntityExistsException error) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error(LOC.getMessage("payg.host_exists")), new TypeToken<>() { });
        }
        catch (ValidatorException e) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error(ValidationUtils.convertValidationErrors(e),
                            ValidationUtils.convertFieldValidationErrors(e)), new TypeToken<>() { });
        }
        catch (Exception e) {
            return json(GSON, res, HttpStatus.SC_BAD_REQUEST, ResultJson.error(e.getMessage()), new TypeToken<>() { });
        }
    }
}
