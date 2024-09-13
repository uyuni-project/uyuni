/*
 * Copyright (c) 2024 SUSE LLC
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
package com.suse.manager.webui.controllers.users;

import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withOrgAdmin;
import static spark.Spark.post;

import com.redhat.rhn.common.util.validation.password.PasswordPolicyCheckFail;
import com.redhat.rhn.common.util.validation.password.PasswordValidationUtils;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.List;

import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

public class UsersDetailsController {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .serializeNulls()
            .create();

    private static Logger log = LogManager.getLogger(UsersDetailsController.class);

    /**
     * Initialize routes for UserSettingsController Api.
     * For now, works only for password validation
     * @param jade the template engine
     */
    public void initRoutes(JadeTemplateEngine jade) {
        post("/users/details/validatePassword", asJson(withOrgAdmin(this::validatePassword)));
    }

    /**
     *
     * @param req
     * @param res
     * @param user
     * @return
     */
    public String validatePassword(Request req, Response res, User user) {
        PasswordValidationRequest request = GSON.fromJson(req.body(), PasswordValidationRequest.class);
        List<PasswordPolicyCheckFail> fails =
                PasswordValidationUtils.validatePasswordFromSatConfiguration(request.getPassword());
        return json(GSON, res, ResultJson.success(fails), new TypeToken<>() { });
    }

    public static class PasswordValidationRequest {
        private final String password;

        public PasswordValidationRequest(String passwordIn) {
            password = passwordIn;
        }

        public String getPassword() {
            return password;
        }
    }

}
