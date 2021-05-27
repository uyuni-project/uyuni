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
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserAndServer;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.virtualization.BaseVirtualizationNetworkAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationNetworkStateChangeAction;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.webui.controllers.virtualization.gson.VirtualNetworkBaseActionJson;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualNetworkInfoJson;
import com.suse.manager.webui.errors.NotFoundException;
import com.suse.manager.webui.services.iface.VirtManager;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
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
        get("/manager/systems/details/virtualization/net/:sid",
                withUserPreferences(withCsrfToken(withDocsLocale(withUserAndServer(this::show)))), jade);

        get("/manager/api/systems/details/virtualization/nets/:sid/data",
                withUserAndServer(this::data));

        post("/manager/api/systems/details/virtualization/nets/:sid/start",
                withUserAndServer(this::start));
        post("/manager/api/systems/details/virtualization/nets/:sid/stop",
                withUserAndServer(this::stop));
        post("/manager/api/systems/details/virtualization/nets/:sid/delete",
                withUserAndServer(this::delete));
    }

    /**
     * Displays the virtual networks page.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return the ModelAndView object to render the page
     */
    public ModelAndView show(Request request, Response response, User user, Server host) {
        return renderPage("show", () -> {
            Map<String, Object> extra = new HashMap<>();
            extra.put("hypervisor", host.hasVirtualizationEntitlement() ?
                    virtManager.getHypervisor(host.getMinionId()).orElse("") :
                    "");
            return extra;
        });
    }

    /**
     * Returns JSON data describing the virtual networks
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return JSON result of the API call
     */
    public String data(Request request, Response response, User user, Server host) {
        String minionId = host.asMinionServer().orElseThrow(NotFoundException::new).getMinionId();

        Map<String, JsonObject> infos = virtManager.getNetworks(minionId);
        List<VirtualNetworkInfoJson> networks = infos.entrySet().stream()
                .map(entry -> new VirtualNetworkInfoJson(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        return json(response, networks);
    }

    /**
     * Executes the POST query to start a set of virtual networks
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return JSON list of created action IDs
     */
    public String start(Request request, Response response, User user, Server host) {
        return netStateChangeAction(request, response, user, host, "start");
    }

    /**
     * Executes the POST query to stop a set of virtual networks
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return JSON list of created action IDs
     */
    public String stop(Request request, Response response, User user, Server host) {
        return netStateChangeAction(request, response, user, host, "stop");
    }

    /**
     * Executes the POST query to delete a set of virtual networks
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return JSON list of created action IDs
     */
    public String delete(Request request, Response response, User user, Server host) {
        return netStateChangeAction(request, response, user, host, "delete");
    }

    private String netStateChangeAction(Request request, Response response, User user, Server host, String state) {
        return netAction(request, response, user, host, (data) -> {
            VirtualizationNetworkStateChangeAction action = (VirtualizationNetworkStateChangeAction)
                    ActionFactory.createAction(ActionFactory.TYPE_VIRTUALIZATION_NETWORK_STATE_CHANGE);
            action.setState(state);

            action.setName(LocalizationService.getInstance().getMessage("virt.network_" + state) + ": " +
                    String.join(",", data.getNames()));
            return action;
        });
    }

    private String netAction(Request request, Response response, User user, Server server,
                              Function<VirtualNetworkBaseActionJson, BaseVirtualizationNetworkAction> actionCreator) {
        return netAction(request, response, user, server, actionCreator, VirtualNetworkBaseActionJson.class);
    }

    private String netAction(Request request, Response response, User user, Server host,
                              Function<VirtualNetworkBaseActionJson, BaseVirtualizationNetworkAction> actionCreator,
                              Class<? extends VirtualNetworkBaseActionJson> jsonClass) {
        return action(request, response, user, host,
                (data, key) -> {
                    VirtualNetworkBaseActionJson poolData = (VirtualNetworkBaseActionJson)data;
                    BaseVirtualizationNetworkAction action = actionCreator.apply(poolData);
                    action.setNetworkName(key);
                    return action;
                },
                (data) -> ((VirtualNetworkBaseActionJson)data).getNames(),
                jsonClass
        );
    }
}
