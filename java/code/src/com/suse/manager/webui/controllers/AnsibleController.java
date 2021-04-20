/**
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

package com.suse.manager.webui.controllers;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withDocsLocale;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.get;
import static spark.Spark.post;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ansible.AnsiblePath;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.SystemManager;
import com.suse.manager.webui.controllers.contentmanagement.handlers.ValidationUtils;
import com.suse.manager.webui.utils.gson.AnsiblePathJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Spark controller class the CVE Audit page.
 */
public class AnsibleController {

    private static final Gson GSON = new GsonBuilder().create();
    private static final LocalizationService LOCAL = LocalizationService.getInstance();

    private static Logger log = Logger.getLogger(AnsibleController.class);

    private AnsibleController() { }

    /**
     * Invoked from Router. Initialize routes for Systems Views.
     *
     * @param jade the Jade engine to use to render the pages
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/systems/details/ansible",
                withCsrfToken(withDocsLocale(withUser(AnsibleController::view))), jade);

        get("/manager/api/systems/details/ansible/paths/:minionServerId",
                withUser(AnsibleController::listAnsiblePathsByMinion));
        // todo no CSRF?
        post("/manager/api/systems/details/ansible/paths/save",
                withUser(AnsibleController::saveAnsiblePath));

        post("/manager/api/systems/details/ansible/paths/delete",
                withUser(AnsibleController::deleteAnsiblePath));
    }

    /**
     * Returns the ansible control node page
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public static ModelAndView view(Request req, Response res, User user) {
        String serverId = req.queryParams("sid");
        Map<String, Object> data = new HashMap<>();
        Server server = ServerFactory.lookupById(Long.valueOf(serverId));
        data.put("server", server);
        return new ModelAndView(data, "templates/minion/ansible-control-node.jade");
    }

    /**
     * List Ansible Paths by minion
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return string with JSON representation of the paths
     */
    public static String listAnsiblePathsByMinion(Request req, Response res, User user) {
        long minionServerId = Long.parseLong(req.params("minionServerId"));
        List<AnsiblePathJson> paths = SystemManager.listAnsiblePaths(minionServerId, user).stream()
                .map(AnsiblePathJson::new)
                .collect(Collectors.toList());
        return json(res, paths);
    }

    /**
     * Save or update (depends of the json.getId() contents) an Ansible path
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the string with JSON response
     */
    public static String saveAnsiblePath(Request req, Response res, User user) {
        AnsiblePathJson json = GSON.fromJson(req.body(), AnsiblePathJson.class);

        AnsiblePath currentPath;
        try {
            if (json.getId() == null) {
                currentPath = SystemManager.createAnsiblePath(AnsiblePath.Type.fromLabel(json.getType()),
                        json.getMinionServerId(),
                        json.getPath(),
                        user);
            }
            else {
                currentPath = SystemManager.updateAnsiblePath(json.getId(),
                        json.getPath(),
                        user);
            }
        }
        catch (ValidatorException e) {
            return json(res, ResultJson.error(
                    ValidationUtils.convertValidationErrors(e),
                    ValidationUtils.convertFieldValidationErrors(e)));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("success", ResultJson.success());
        data.put("newPathId", currentPath.getId());
        return json(res, data);
    }
    /**
     * Delete an Ansible path
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the string with JSON response
     */
    public static String deleteAnsiblePath(Request req, Response res, User user) {
        AnsiblePathJson json = GSON.fromJson(req.body(), AnsiblePathJson.class);

        try {
            SystemManager.removeAnsiblePath(SystemManager.lookupAnsiblePathById(json.getId(), user).get(), user);
        }
        catch (ValidatorException e) {
            return json(res, ResultJson.error(
                    ValidationUtils.convertValidationErrors(e),
                    ValidationUtils.convertFieldValidationErrors(e)));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("success", ResultJson.success());
        return json(res, data);
    }

}
