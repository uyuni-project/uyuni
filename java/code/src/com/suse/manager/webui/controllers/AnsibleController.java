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

package com.suse.manager.webui.controllers;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withDocsLocale;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserAndServer;
import static com.suse.manager.webui.utils.gson.ResultJson.error;
import static com.suse.manager.webui.utils.gson.ResultJson.success;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ansible.AnsiblePath;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.context.Context;
import com.redhat.rhn.manager.system.AnsibleManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.webui.controllers.contentmanagement.handlers.ValidationUtils;
import com.suse.manager.webui.utils.gson.AnsiblePathJson;
import com.suse.manager.webui.utils.gson.AnsiblePlaybookExecutionJson;
import com.suse.manager.webui.utils.gson.AnsiblePlaybookIdJson;
import com.suse.manager.webui.utils.gson.SimpleMinionJson;
import com.suse.utils.Json;

import com.google.gson.Gson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Spark controller class the CVE Audit page.
 */
public class AnsibleController {

    private static final Logger LOG = LogManager.getLogger(AnsibleController.class);
    private static final Gson GSON = Json.GSON;
    private static final Yaml YAML = new Yaml(new SafeConstructor());

    private static final LocalizationService LOCAL = LocalizationService.getInstance();

    private AnsibleController() { }

    /**
     * Invoked from Router. Initialize routes for Systems Views.
     *
     * @param jade the Jade engine to use to render the pages
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/systems/details/ansible/control-node",
                withCsrfToken(withDocsLocale(withUserAndServer(AnsibleController::view))), jade);

        get("/manager/systems/details/ansible/playbooks",
                withCsrfToken(withDocsLocale(withUserAndServer(AnsibleController::playbooks))), jade);

        get("/manager/systems/details/ansible/inventories",
                withCsrfToken(withDocsLocale(withUserAndServer(AnsibleController::inventories))), jade);

        get("/manager/api/systems/details/ansible/paths/:minionServerId",
                withUser(AnsibleController::listAnsiblePathsByMinion));

        get("/manager/api/systems/details/ansible/paths/:pathType/:minionServerId",
                withUser(AnsibleController::listTypedPathsByMinion));

        post("/manager/api/systems/details/ansible/paths/save",
                withUser(AnsibleController::saveAnsiblePath));

        post("/manager/api/systems/details/ansible/paths/delete",
                withUser(AnsibleController::deleteAnsiblePath));

        post("/manager/api/systems/details/ansible/paths/playbook-contents",
                withUser(AnsibleController::fetchPlaybookContents));

        post("/manager/api/systems/details/ansible/schedule-playbook",
                withUser(AnsibleController::schedulePlaybook));

        get("/manager/api/systems/details/ansible/introspect-inventory/:pathId",
                withUser(AnsibleController::introspectInventory));

        get("/manager/api/systems/details/ansible/discover-playbooks/:pathId",
                withUser(AnsibleController::discoverPlaybooks));
    }

    /**
     * Returns the ansible control node page
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @param server the server
     * @return the model and view
     */
    public static ModelAndView view(Request req, Response res, User user, Server server) {
        return new ModelAndView(new HashMap<String, Object>(), "templates/minion/ansible-control-node.jade");
    }

    /**
     * Returns the ansible playbooks page
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @param server the server
     * @return the model and view
     */
    public static ModelAndView playbooks(Request req, Response res, User user, Server server) {
        Map<String, Object> data = new HashMap<>();
        data.put("pathContentType", AnsiblePath.Type.PLAYBOOK.getLabel());
        MinionController.addActionChains(user, data);
        return new ModelAndView(data, "templates/minion/ansible-path-content.jade");
    }

    /**
     * Returns the ansible inventories page
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @param server the server
     * @return the model and view
     */
    public static ModelAndView inventories(Request req, Response res, User user, Server server) {
        Map<String, Object> data = new HashMap<>();
        data.put("pathContentType", AnsiblePath.Type.INVENTORY.getLabel());
        return new ModelAndView(data, "templates/minion/ansible-path-content.jade");
    }

    /**
     * List Ansible Paths by minion and by path type (Playbook or Inventory)
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return string with JSON representation of the paths
     */
    public static String listTypedPathsByMinion(Request req, Response res, User user) {
        String pathType = req.params("pathType");
        if (LOG.isDebugEnabled()) {
            LOG.debug("path type: {}", pathType.replaceAll("[\n\r\t]", "_"));
        }
        long minionServerId = Long.parseLong(req.params("minionServerId"));
        List<AnsiblePathJson> paths;

        if (pathType.equalsIgnoreCase(AnsiblePath.Type.PLAYBOOK.getLabel())) {
            paths = AnsibleManager.listAnsiblePlaybookPaths(minionServerId, user).stream()
                    .map(AnsiblePathJson::new)
                    .collect(Collectors.toList());
        }
        else {
            paths = AnsibleManager.listAnsibleInventoryPaths(minionServerId, user).stream()
                    .map(AnsiblePathJson::new)
                    .collect(Collectors.toList());
        }
        return json(res, success(paths));
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
        return json(res, success(paths));
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
            return json(res, error(
                    ValidationUtils.convertValidationErrors(e),
                    ValidationUtils.convertFieldValidationErrors(e)));
        }

        return json(res, success(Map.of("pathId", currentPath.getId())));
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
            return json(res, error(LOCAL.getMessage("ansible.entity_not_found")));
        }

        return json(res, success());
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
            return getAnsibleManager().fetchPlaybookContents(params.getPathId(), params.getPlaybookRelPathStr(), user)
                    .map(contents -> json(res, success(contents)))
                    .orElseGet(() -> json(res,
                            error(LOCAL.getMessage("ansible.control_node_not_responding"))));
        }
        catch (IllegalStateException e) {
            return json(res,
                    error(LOCAL.getMessage("ansible.salt_error", e.getMessage())));
        }
        catch (LookupException e) {
            return json(res, error(LOCAL.getMessage("ansible.entity_not_found")));
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
                    params.isTestMode(),
                    params.isFlushCache(),
                    params.getEarliest().map(AnsibleController::getScheduleDate).orElse(new Date()),
                    params.getActionChainLabel(),
                    user);

            return json(res, success(params.getActionChainLabel()
                    .map(l -> ActionChainFactory.getActionChain(user, l).getId()).orElse(actionId)));
        }
        catch (LookupException e) {
            return json(res, error(LOCAL.getMessage("ansible.entity_not_found")));
        }
        catch (TaskomaticApiException e) {
            return json(res, error(LOCAL.getMessage("taskscheduler.down")));
        }
    }

    private static Date getScheduleDate(LocalDateTime dateTime) {
        ZoneId zoneId = Context.getCurrentContext().getTimezone().toZoneId();
        return Date.from(dateTime.atZone(zoneId).toInstant());
    }

    /**
     * Introspect ansible inventory
     *
     * @param req the request
     * @param res the response
     * @param user the authorized user
     * @return the string in YAML format representing the structure of the inventory
     */
    public static String introspectInventory(Request req, Response res, User user) {
        long pathId = Long.parseLong(req.params("pathId"));

        try {
            return getAnsibleManager().introspectInventory(pathId, user)
                    .map(inventory -> {
                        Map<String, Object> data = new HashMap<>();

                        Set<String> hostvars = parseInventoryAndGetHostnames(inventory);
                        List<SimpleMinionJson> registeredServers = new LinkedList<>();
                        List<String> unknownHostNames = new LinkedList<>();

                        // fork content into registered or unknown hostnames
                        hostvars.forEach(
                                serverName ->
                                    ServerFactory.findByFqdn(serverName).ifPresentOrElse(
                                            s -> registeredServers.add(new SimpleMinionJson(s.getId(), s.getName())),
                                            () -> unknownHostNames.add(serverName)
                                    )
                        );

                        data.put("dump", YAML.dump(inventory));
                        data.put("knownSystems", registeredServers);
                        data.put("unknownSystems", unknownHostNames);

                        return json(res, success(data));
                    })
                    .orElseGet(() -> json(res,
                            error(LOCAL.getMessage("ansible.control_node_not_responding"))));
        }
        catch (IllegalStateException e) {
            return json(res,
                    error(LOCAL.getMessage("ansible.salt_error", e.getMessage())));
        }
        catch (LookupException e) {
            return json(res, error(LOCAL.getMessage("ansible.entity_not_found")));
        }
    }

    /**
     * Parse Ansible Inventory content to look for host names
     *
     * @param inventoryMap the Ansible Inventory content
     * @return the Set of hostnames
     */
    public static Set<String> parseInventoryAndGetHostnames(Map<String, Map<String, Object>> inventoryMap) {
        // Assumption: "_meta" and the nested "hostvars" keys always present in the map to contains all hostnames
        if (inventoryMap.containsKey("_meta") &&
                inventoryMap.get("_meta").containsKey("hostvars") &&
                inventoryMap.get("_meta").get("hostvars") instanceof Map) {
            return ((Map<String, Object>) inventoryMap.get("_meta").get("hostvars")).keySet();
        }
        return new HashSet<>();
    }

    /**
     * Discover ansible playbooks
     *
     * @param req the request
     * @param res the response
     * @param user the authorized user
     * @return the json with structure of the discovered playbooks (@see AnsibleManager.discoverPlaybooks doc}
     */
    public static String discoverPlaybooks(Request req, Response res, User user) {
        long pathId = Long.parseLong(req.params("pathId"));

        try {
            return getAnsibleManager().discoverPlaybooks(pathId, user)
                    .map(playbook -> json(res, success(playbook)))
                    .orElseGet(() -> json(res,
                            error(LOCAL.getMessage("ansible.control_node_not_responding"))));
        }
        catch (IllegalStateException e) {
            return json(res,
                    error(LOCAL.getMessage("ansible.salt_error", e.getMessage())));
        }
        catch (LookupException e) {
            return json(res, error(LOCAL.getMessage("ansible.entity_not_found")));
        }
    }

    private static AnsibleManager getAnsibleManager() {
        return new AnsibleManager(GlobalInstanceHolder.SALT_API);
    }
}
