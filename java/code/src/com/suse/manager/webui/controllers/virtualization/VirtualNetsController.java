/**
 * Copyright (c) 2018 SUSE LLC
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
package com.suse.manager.webui.controllers.virtualization;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withDocsLocale;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.virtualization.BaseVirtualizationNetworkAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationNetworkCreateAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationNetworkStateChangeAction;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.virtualization.NetworkDefinition;
import com.suse.manager.webui.controllers.MinionController;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualNetworkBaseActionJson;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualNetworkCreateActionJson;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualNetworkInfoJson;
import com.suse.manager.webui.errors.NotFoundException;
import com.suse.manager.webui.services.iface.VirtManager;

import com.google.gson.JsonObject;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller class providing backend for Virtual networks UI
 */
public class VirtualNetsController extends AbstractVirtualizationController {

    /**
     * Controller class providing backend for Virtual networks UI
     * @param virtManagerIn instance to manage virtualization
     */
    public VirtualNetsController(VirtManager virtManagerIn) {
        super(virtManagerIn, "templates/virtualization/nets");
    }

    /**
     * Initialize request routes for the pages served by VirtualNetsController
     *
     * @param jade jade engine
     */
    public void initRoutes(JadeTemplateEngine jade) {
        get("/manager/systems/details/virtualization/nets/:sid",
                withUserPreferences(withCsrfToken(withDocsLocale(withUser(this::show)))), jade);
        get("/manager/systems/details/virtualization/nets/:sid/new",
                withUserPreferences(withCsrfToken(withDocsLocale(withUser(this::createDialog)))), jade);
        get("/manager/systems/details/virtualization/nets/:sid/edit/:name",
                withUserPreferences(withCsrfToken(withDocsLocale(withUser(this::editDialog)))), jade);

        get("/manager/api/systems/details/virtualization/nets/:sid/data",
                withUser(this::data));
        get("/manager/api/systems/details/virtualization/nets/:sid/devices",
                withUser(this::devices));
        post("/manager/api/systems/details/virtualization/nets/:sid/start",
                withUser(this::start));
        post("/manager/api/systems/details/virtualization/nets/:sid/stop",
                withUser(this::stop));
        post("/manager/api/systems/details/virtualization/nets/:sid/delete",
                withUser(this::delete));
        post("/manager/api/systems/details/virtualization/nets/:sid/create",
                withUser(this::create));
        post("/manager/api/systems/details/virtualization/nets/:sid/edit",
                withUser(this::edit));
        get("/manager/api/systems/details/virtualization/nets/:sid/net/:name",
                withUser(this::getNetwork));
    }

    /**
     * Displays the virtual networks page.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the ModelAndView object to render the page
     */
    public ModelAndView show(Request request, Response response, User user) {
        Server host = getServer(request, user);
        String minionId = host.asMinionServer().orElseThrow(NotFoundException::new).getMinionId();
        Map<String, Boolean> features = virtManager.getFeatures(minionId).orElse(new HashMap<>());
        return renderPage(request, response, user, "show", () -> {
            Map<String, Object> extra = new HashMap<>();
            extra.put("hypervisor", host.hasVirtualizationEntitlement() ?
                    virtManager.getHypervisor(host.getMinionId()).orElse("") :
                    "");
            extra.put("support_enhanced_network", features.getOrDefault("enhanced_network", false));
            return extra;
        });
    }

    /**
     * Displays the virtual network creation page.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the ModelAndView object to render the page
     */
    public ModelAndView createDialog(Request request, Response response, User user) {
        Server host = getServer(request, user);
        return renderPage(request, response, user, "create", () -> {
            Map<String, Object> data = new HashMap<>();
            MinionController.addActionChains(user, data);
            return data;
        });
    }

    /**
     * Displays the virtual network editing page.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the ModelAndView object to render the page
     */
    public ModelAndView editDialog(Request request, Response response, User user) {
        Server host = getServer(request, user);
        String minionId = host.asMinionServer().orElseThrow(NotFoundException::new).getMinionId();

        return renderPage(request, response, user, "edit", () -> {
            Map<String, Object> data = new HashMap<>();
            data.put("netName", request.params("name"));
            MinionController.addActionChains(user, data);
            return data;
        });
    }

    /**
     * Returns JSON data describing the virtual networks
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON result of the API call
     */
    public String data(Request request, Response response, User user) {
        Server host = getServer(request, user);
        String minionId = host.asMinionServer().orElseThrow(NotFoundException::new).getMinionId();

        Map<String, JsonObject> infos = virtManager.getNetworks(minionId);
        List<VirtualNetworkInfoJson> networks = infos.entrySet().stream()
                .map(entry -> new VirtualNetworkInfoJson(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        return json(response, networks);
    }

    /**
     * Returns JSON data describing the host ethernet devices
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON result of the API call
     */
    public String devices(Request request, Response response, User user) {
        Server host = getServer(request, user);
        String minionId = host.asMinionServer().orElseThrow(NotFoundException::new).getMinionId();

        List<JsonObject> allDevices = virtManager.getHostDevices(minionId);
        Map<String, JsonObject> byName = allDevices.stream().collect(
                Collectors.toMap(item -> item.get("name").getAsString(), Function.identity()));

        List<JsonObject> netDevices = allDevices.stream()
                .filter(item -> "net".equals(item.get("caps").getAsString()))
                .map(item -> {
                    item.remove("caps");
                    // Extract infos from the parent device if possible
                    if (item.has("device name")) {
                        JsonObject parent = byName.get(item.get("device name").getAsString());
                        item.remove("device name");
                        if (parent != null) {
                            boolean isVirtual = parent.has("physical function");
                            item.addProperty("VF", isVirtual);
                            if (isVirtual) {
                                item.addProperty("PCI address", parent.get("address").getAsString());
                            }
                            item.addProperty("PF", parent.has("virtual functions"));
                        }
                    }
                    return item;
                })
                .collect(Collectors.toList());

        return json(response, netDevices);
    }

    /**
     * Executes the POST query to start a set of virtual networks
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON list of created action IDs
     */
    public String start(Request request, Response response, User user) {
        return netStateChangeAction(request, response, user, "start");
    }

    /**
     * Executes the POST query to stop a set of virtual networks
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON list of created action IDs
     */
    public String stop(Request request, Response response, User user) {
        return netStateChangeAction(request, response, user, "stop");
    }

    /**
     * Executes the POST query to delete a set of virtual networks
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON list of created action IDs
     */
    public String delete(Request request, Response response, User user) {
        return netStateChangeAction(request, response, user, "delete");
    }

    /**
     * Executes the POST query to create a virtual network
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON list of created action IDs
     */
    public String create(Request request, Response response, User user) {
        return createOrUpdate(request, response, user, null);
    }

    /**
     * Executes the POST query to edit a virtual network
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON list of created action IDs
     */
    public String edit(Request request, Response response, User user) {
        String actionName = LocalizationService.getInstance().getMessage("virt.network_update");
        return createOrUpdate(request, response, user, actionName);
    }

    private String createOrUpdate(Request request, Response response, User user, String actionName) {
        return netAction(request, response, user, (data) -> {
            VirtualizationNetworkCreateAction action = (VirtualizationNetworkCreateAction)
                    ActionFactory.createAction(ActionFactory.TYPE_VIRTUALIZATION_NETWORK_CREATE);
            String displayName = actionName != null ? actionName : action.getActionType().getName();
            action.setName(displayName + ": " + String.join(",", data.getNames()));

            VirtualNetworkCreateActionJson createData = (VirtualNetworkCreateActionJson)data;
            if (createData.getNames().isEmpty()) {
                throw new IllegalArgumentException("Network names needs to contain an element");
            }

            action.setNetworkName(createData.getNames().get(0));
            action.setDefinition(createData.getDefinition());

            return action;
        }, VirtualNetworkCreateActionJson.class);
    }

    /**
     * Executes the GET query to extract the network definition
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON-formatted capabilities
     */
    public String getNetwork(Request request, Response response, User user) {
        Server host = getServer(request, user);
        String minionId = host.asMinionServer().orElseThrow(() ->
                Spark.halt(HttpStatus.SC_BAD_REQUEST, "Can only get network definition of Salt system")).getMinionId();

        String netName = request.params("name");
        NetworkDefinition definition = virtManager.getNetworkDefinition(minionId, netName)
                .orElseThrow(NotFoundException::new);

        response.type("application/json");
        return GSON.toJson(definition);
    }

    private String netStateChangeAction(Request request, Response response, User user, String state) {
        return netAction(request, response, user, (data) -> {
            VirtualizationNetworkStateChangeAction action = (VirtualizationNetworkStateChangeAction)
                    ActionFactory.createAction(ActionFactory.TYPE_VIRTUALIZATION_NETWORK_STATE_CHANGE);
            action.setState(state);

            action.setName(LocalizationService.getInstance().getMessage("virt.network_" + state) + ": " +
                    String.join(",", data.getNames()));
            return action;
        });
    }

    private String netAction(Request request, Response response, User user,
                              Function<VirtualNetworkBaseActionJson, BaseVirtualizationNetworkAction> actionCreator) {
        return netAction(request, response, user, actionCreator, VirtualNetworkBaseActionJson.class);
    }

    private String netAction(Request request, Response response, User user,
                              Function<VirtualNetworkBaseActionJson, BaseVirtualizationNetworkAction> actionCreator,
                              Class<? extends VirtualNetworkBaseActionJson> jsonClass) {
        return action(request, response, user,
                (data, key) -> {
                    VirtualNetworkBaseActionJson netData = (VirtualNetworkBaseActionJson)data;
                    BaseVirtualizationNetworkAction action = actionCreator.apply(netData);
                    action.setNetworkName(key);
                    return action;
                },
                (data) -> ((VirtualNetworkBaseActionJson)data).getNames(),
                jsonClass
        );
    }
}
