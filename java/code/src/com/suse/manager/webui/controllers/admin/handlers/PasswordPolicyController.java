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

package com.suse.manager.webui.controllers.admin.handlers;

import static com.redhat.rhn.domain.role.RoleFactory.SAT_ADMIN;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.post;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.common.SatConfigFactory;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.webui.controllers.admin.beans.PasswordPolicyProperties;
import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;

import spark.Request;
import spark.Response;

public class PasswordPolicyController {
    private static final LocalizationService LOC = LocalizationService.getInstance();
    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    /**
     * initialize all the API Routes for the password policy
     */
    public void initRoutes() {
        post("/manager/api/admin/config/password-policy", withUser(this::postPasswordPolicy));
    }

    /**
     * edit the current password policy settings
     * @param request
     * @param response
     * @param user
     * @return an OK string
     */
    public String postPasswordPolicy(Request request, Response response, User user) {
        if (!user.hasRole(SAT_ADMIN)) {
            throw new PermissionException(SAT_ADMIN);
        }
        PasswordPolicyProperties passwordPolicyProperties = GSON.fromJson(request.body(),
                PasswordPolicyProperties.class);
        try {
            SatConfigFactory.setSatConfigValue(SatConfigFactory.PSW_CHECK_LENGTH_MIN,
                    passwordPolicyProperties.getMinLength().toString());
            SatConfigFactory.setSatConfigValue(SatConfigFactory.PSW_CHECK_LENGTH_MAX,
                    passwordPolicyProperties.getMaxLength().toString());
            SatConfigFactory.setSatConfigBooleanValue(SatConfigFactory.PSW_CHECK_DIGIT_FLAG,
                    passwordPolicyProperties.getDigitsFlag());
            SatConfigFactory.setSatConfigBooleanValue(SatConfigFactory.PSW_CHECK_LOWER_CHAR_FLAG,
                    passwordPolicyProperties.getLowerCharFlag());
            SatConfigFactory.setSatConfigBooleanValue(SatConfigFactory.PSW_CHECK_UPPER_CHAR_FLAG,
                    passwordPolicyProperties.getUpperCharFlag());
            SatConfigFactory.setSatConfigBooleanValue(SatConfigFactory.PSW_CHECK_CONSECUTIVE_CHAR_FLAG,
                    passwordPolicyProperties.getConsecutiveCharFlag());
            SatConfigFactory.setSatConfigBooleanValue(SatConfigFactory.PSW_CHECK_SPECIAL_CHAR_FLAG,
                    passwordPolicyProperties.getSpecialCharFlag());
            SatConfigFactory.setSatConfigValue(SatConfigFactory.PSW_CHECK_SPECIAL_CHARACTERS,
                    passwordPolicyProperties.getSpecialCharList());
            SatConfigFactory.setSatConfigBooleanValue(SatConfigFactory.PSW_CHECK_RESTRICTED_OCCURRENCE_FLAG,
                    passwordPolicyProperties.getRestrictedOccurrenceFlag());
            SatConfigFactory.setSatConfigValue(SatConfigFactory.PSW_CHECK_MAX_OCCURRENCE,
                    passwordPolicyProperties.getMaxCharOccurrence().toString());
            return json(GSON, response, ResultJson.success(""), new TypeToken<>() { });
        }
        catch (Exception e) {
            return json(GSON, response, HttpStatus.SC_BAD_REQUEST, ResultJson.error(e.getMessage()),
                    new TypeToken<>() { });
        }
    }

}
