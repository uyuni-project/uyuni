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

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ansible.AnsiblePath;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.AnsibleManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.webui.controllers.contentmanagement.handlers.ValidationUtils;
import com.suse.manager.webui.utils.gson.AnsiblePathJson;
import com.suse.manager.webui.utils.gson.AnsiblePlaybookExecutionJson;
import com.suse.manager.webui.utils.gson.AnsiblePlaybookIdJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.utils.Json;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.jade.JadeTemplateEngine;

/**
 * Spark controller class the CVE Audit page.
 */
public class AnsibleController {

    private static final Gson GSON = Json.GSON;

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

        post("/manager/api/systems/details/ansible/paths/playbook-contents",
                withUser(AnsibleController::fetchPlaybookContents));

        post("/manager/api/systems/details/ansible/schedule-playbook",
                withUser(AnsibleController::schedulePlaybook));
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
        List<AnsiblePathJson> paths = AnsibleManager.listAnsiblePaths(minionServerId, user).stream()
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
                currentPath = AnsibleManager.createAnsiblePath(json.getType(),
                        json.getMinionServerId(),
                        json.getPath(),
                        user);
            }
            else {
                currentPath = AnsibleManager.updateAnsiblePath(json.getId(),
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
        Long ansiblePathId = GSON.fromJson(req.body(), Long.class);

        try {
            AnsibleManager.removeAnsiblePath(ansiblePathId, user);
        }
        catch (LookupException e) {
            Spark.halt(404);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("success", ResultJson.success());
        return json(res, data);
    }

    /**
     * Fetch playbook contents using a salt sync call
     *
     * @param req the request
     * @param res the response
     * @param user the authorized user
     * @return the response with the playbook contents or with a localized error message when minion not responding
     */
    public static String fetchPlaybookContents(Request req, Response res, User user) {
        try {
            AnsiblePlaybookIdJson params = GSON.fromJson(req.body(), AnsiblePlaybookIdJson.class);
            return AnsibleManager.fetchPlaybookContents(params.getPathId(), params.getPlaybookRelPathStr(), user)
                    .map(contents -> json(res, contents))
                    .orElseGet(() -> json(res,
                            ResultJson.error(LOCAL.getMessage("ansible.control_node_not_responding"))));
        }
        catch (LookupException e) {
            throw Spark.halt(404);
        }
    }

    /**
     * Schedule playbook execution
     *
     * @param req the request
     * @param res the response
     * @param user the authorized user
     * @return the json with the scheduled action id or with a localized error message when taskomatic is down
     */
    public static String schedulePlaybook(Request req, Response res, User user) {
        try {
            AnsiblePlaybookExecutionJson params = GSON.fromJson(req.body(), AnsiblePlaybookExecutionJson.class);
            Long actionId = AnsibleManager.schedulePlaybook(
                    params.getPlaybookPath(),
                    params.getInventoryPath().orElse(null),
                    params.getControlNodeId(),
                    params.getEarliest().orElse(new Date()),
                    user);
            return json(res, actionId);
        }
        catch (LookupException e) {
            throw Spark.halt(404);
        }
        catch (TaskomaticApiException e) {
            return json(res, ResultJson.error(LOCAL.getMessage("taskscheduler.down")));
        }
    }
}
