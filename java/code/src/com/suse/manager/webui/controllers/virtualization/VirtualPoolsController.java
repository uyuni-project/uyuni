/*
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
import com.redhat.rhn.domain.action.virtualization.BaseVirtualizationPoolAction;
import com.redhat.rhn.domain.action.virtualization.BaseVirtualizationVolumeAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationPoolCreateAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationPoolDeleteAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationPoolRefreshAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationPoolStartAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationPoolStopAction;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.virtualization.HostInfo;
import com.suse.manager.virtualization.PoolCapabilitiesJson;
import com.suse.manager.virtualization.PoolDefinition;
import com.suse.manager.webui.controllers.MinionController;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualPoolBaseActionJson;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualPoolCreateActionJson;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualPoolDeleteActionJson;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualStoragePoolInfoJson;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualStorageVolumeInfoJson;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualVolumeBaseActionJson;
import com.suse.manager.webui.errors.NotFoundException;
import com.suse.manager.webui.services.iface.VirtManager;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller class providing backend for Virtual storage pools UI
 */
public class VirtualPoolsController extends AbstractVirtualizationController {

    /**
     * Controller class providing backend for Virtual storage pools UI
     * @param virtManagerIn instance to manage virtualization
     */
    public VirtualPoolsController(VirtManager virtManagerIn) {
        super(virtManagerIn, "templates/virtualization/pools");
    }

    /**
     * Initialize request routes for the pages served by VirtualPoolsController
     *
     * @param jade jade engine
     */
    public void initRoutes(JadeTemplateEngine jade) {
        get("/manager/systems/details/virtualization/storage/:sid",
                withUserPreferences(withCsrfToken(withDocsLocale(withUserAndServer(this::show)))), jade);
        get("/manager/systems/details/virtualization/storage/:sid/new",
                withUserPreferences(withCsrfToken(withDocsLocale(withUserAndServer(this::createDialog)))), jade);
        get("/manager/systems/details/virtualization/storage/:sid/edit/:name",
                withUserPreferences(withCsrfToken(withDocsLocale(withUserAndServer(this::editDialog)))), jade);
        get("/manager/api/systems/details/virtualization/pools/:sid/data",
                withUserAndServer(this::data));
        get("/manager/api/systems/details/virtualization/pools/:sid/capabilities",
                withUserAndServer(this::getCapabilities));
        get("/manager/api/systems/details/virtualization/pools/:sid/pool/:name",
                withUserAndServer(this::getPool));
        post("/manager/api/systems/details/virtualization/pools/:sid/refresh",
                withUserAndServer(this::poolRefresh));
        post("/manager/api/systems/details/virtualization/pools/:sid/start",
                withUserAndServer(this::poolStart));
        post("/manager/api/systems/details/virtualization/pools/:sid/stop",
                withUserAndServer(this::poolStop));
        post("/manager/api/systems/details/virtualization/pools/:sid/delete",
                withUserAndServer(this::poolDelete));
        post("/manager/api/systems/details/virtualization/pools/:sid/create",
                withUserAndServer(this::poolCreate));
        post("/manager/api/systems/details/virtualization/pools/:sid/edit",
                withUserAndServer(this::poolEdit));
        post("/manager/api/systems/details/virtualization/volumes/:sid/delete",
                withUserAndServer(this::volumeDelete));
    }

    /**
     * Displays the virtual storages page.
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
            Optional<HostInfo> hostInfo = virtManager.getHostInfo(host.getMinionId());
            String hypervisor = hostInfo.isPresent() ? hostInfo.get().getHypervisor() : "";
            extra.put("hypervisor", host.hasVirtualizationEntitlement() ? hypervisor : "");
            return extra;
        });
    }


    /**
     * Displays the virtual storage pool creation page.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param server the server
     * @return the ModelAndView object to render the page
     */
    public ModelAndView createDialog(Request request, Response response, User user, Server server) {
        return renderWithActionChains(user, "create", null);
    }


    /**
     * Displays the virtual storage pool edit page.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param server the server
     * @return the ModelAndView object to render the page
     */
    public ModelAndView editDialog(Request request, Response response, User user, Server server) {
        return renderWithActionChains(user, "edit",
            () -> {
                Map<String, Object> data = new HashMap<>();
                data.put("poolName", request.params("name"));
                return data;
            }
        );
    }


    /**
     * Returns JSON data describing the storage pools
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return JSON result of the API call
     */
    public String data(Request request, Response response, User user, Server host) {
        String minionId = host.asMinionServer().orElseThrow(NotFoundException::new).getMinionId();

        Map<String, JsonObject> infos = virtManager.getPools(minionId);
        Map<String, Map<String, JsonObject>> volInfos = virtManager.getVolumes(minionId);
        List<VirtualStoragePoolInfoJson> pools = infos.entrySet().stream().map(entry -> {
            Map<String, JsonObject> poolVols = volInfos.getOrDefault(entry.getKey(), new HashMap<>());
            List<VirtualStorageVolumeInfoJson> volumes = poolVols.entrySet().stream()
                    .map(volEntry -> new VirtualStorageVolumeInfoJson(volEntry.getKey(), volEntry.getValue()))
                    .collect(Collectors.toList());

            return new VirtualStoragePoolInfoJson(entry.getKey(), entry.getValue(), volumes);
        }).collect(Collectors.toList());

        return json(response, pools, new TypeToken<>() { });
    }

    /**
     * Executes the GET query to extract the pool capabilities
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return JSON-formatted capabilities
     */
    public String getCapabilities(Request request, Response response, User user, Server host) {
        String minionId = host.asMinionServer().orElseThrow(() ->
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Can only get capabilities of Salt system")).getMinionId();

        PoolCapabilitiesJson caps = virtManager.getPoolCapabilities(minionId)
            .orElseThrow(() -> Spark.halt(HttpStatus.SC_BAD_REQUEST,
                "Failed to get virtual host storage pool capabilities"));

        return json(response, caps, new TypeToken<>() { });
    }

    /**
     * Executes the GET query to extract the pool definition
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return JSON-formatted capabilities
     */
    public String getPool(Request request, Response response, User user, Server host) {
        String minionId = host.asMinionServer().orElseThrow(() ->
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Can only get pool definition of Salt system")).getMinionId();

        String poolName = request.params("name");
        PoolDefinition definition = virtManager.getPoolDefinition(minionId, poolName)
                .orElseThrow(NotFoundException::new);

        return json(response, definition, new TypeToken<>() { });
    }

    /**
     * Executes the POST query to refresh a set of virtual pools.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return JSON list of created action IDs
     */
    public String poolRefresh(Request request, Response response, User user, Server host) {
        return poolAction(request, response, user, host, (data) -> {
            VirtualizationPoolRefreshAction action = (VirtualizationPoolRefreshAction)
                    ActionFactory.createAction(ActionFactory.TYPE_VIRTUALIZATION_POOL_REFRESH);
            action.setName(action.getActionType().getName() + ": " + String.join(",", data.getPoolNames()));
            return action;
        });
    }

    /**
     * Executes the POST query to start a set of virtual pools.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return JSON list of created action IDs
     */
    public String poolStart(Request request, Response response, User user, Server host) {
        return poolAction(request, response, user, host, (data) -> {
            VirtualizationPoolStartAction action = (VirtualizationPoolStartAction)
                    ActionFactory.createAction(ActionFactory.TYPE_VIRTUALIZATION_POOL_START);
            action.setName(action.getActionType().getName() + ": " + String.join(",", data.getPoolNames()));
            return action;
        });
    }

    /**
     * Executes the POST query to stop a set of virtual pools.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return JSON list of created action IDs
     */
    public String poolStop(Request request, Response response, User user, Server host) {
        return poolAction(request, response, user, host, (data) -> {
            VirtualizationPoolStopAction action = (VirtualizationPoolStopAction)
                    ActionFactory.createAction(ActionFactory.TYPE_VIRTUALIZATION_POOL_STOP);
            action.setName(action.getActionType().getName() + ": " + String.join(",", data.getPoolNames()));
            return action;
        });
    }

    /**
     * Executes the POST query to delete a set of virtual pools.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return JSON list of created action IDs
     */
    public String poolDelete(Request request, Response response, User user, Server host) {
        return poolAction(request, response, user, host, (data) -> {
            VirtualizationPoolDeleteAction action = (VirtualizationPoolDeleteAction)
                    ActionFactory.createAction(ActionFactory.TYPE_VIRTUALIZATION_POOL_DELETE);
            action.setName(action.getActionType().getName() + ": " + String.join(",", data.getPoolNames()));

            if (data.getPurge() != null) {
                action.setPurge(data.getPurge());
            }
            return action;
        }, VirtualPoolDeleteActionJson.class);
    }

    /**
     * Executes the POST query to create a virtual pool.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param host the server
     * @return JSON list of created action IDs
     */
    public String poolCreate(Request request, Response response, User user, Server host) {
        return poolCreateOrUpdate(request, response, user, host, null);
    }

    /**
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param server the server
     * @param actionName the display name of the action. If null, default to the one from the ActionType
     * @return JSON list of created action IDs
     */
    public String poolCreateOrUpdate(Request request, Response response, User user, Server server, String actionName) {
        return poolAction(request, response, user, server, (data) -> {
            VirtualizationPoolCreateAction action = (VirtualizationPoolCreateAction)
                    ActionFactory.createAction(ActionFactory.TYPE_VIRTUALIZATION_POOL_CREATE);
            String displayName = actionName != null ? actionName : action.getActionType().getName();
            action.setName(displayName + ": " + String.join(",", data.getPoolNames()));

            if (data.getPoolNames().isEmpty()) {
                throw new IllegalArgumentException("pool names needs to contain an element");
            }

            action.setUuid(data.getUuid());
            action.setPoolName(data.getPoolNames().get(0));
            action.setType(data.getType());
            action.setAutostart(data.isAutostart());
            if (data.getTarget() != null) {
                action.setTarget(data.getTarget().getPath());
                action.setOwner(data.getTarget().getOwner());
                action.setGroup(data.getTarget().getGroup());
                action.setMode(data.getTarget().getMode());
                action.setSeclabel(data.getTarget().getSeclabel());
            }
            action.setSource(data.getSource());

            return action;
        }, VirtualPoolCreateActionJson.class);
    }


    /**
     * Executes the POST query to edit a virtual pool.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param server the server
     * @return JSON list of created action IDs
     */
    public String poolEdit(Request request, Response response, User user, Server server) {
        String actionName = LocalizationService.getInstance().getMessage("virt.pool_update");
        return poolCreateOrUpdate(request, response, user, server, actionName);
    }

    /**
     * Executes the POST query to delete a set of virtual volumes.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param server the server
     * @return JSON list of created action IDs
     */
    public String volumeDelete(Request request, Response response, User user, Server server) {
        return volumeAction(request, response, user, server,
                data -> (BaseVirtualizationVolumeAction)ActionFactory.createAction(
                        ActionFactory.TYPE_VIRTUALIZATION_VOLUME_DELETE),
                VirtualVolumeBaseActionJson.class);
    }



    /**
     * Displays a virtual storage pool-relate page with actionChains in the model.
     *
     * @param user the user
     * @param template the name to the Jade template of the page
     * @param modelExtender provides additional properties to pass to the Jade template
     * @return the ModelAndView object to render the page
     */
    private ModelAndView renderWithActionChains(User user, String template,
                                                Supplier<Map<String, Object>> modelExtender) {
        return renderPage(template,
            () -> {
                Map<String, Object> data = new HashMap<>();
                MinionController.addActionChains(user, data);
                if (modelExtender != null) {
                    data.putAll(modelExtender.get());
                }
                return data;
            }
        );
    }

    private String poolAction(Request request, Response response, User user, Server server,
            Function<VirtualPoolBaseActionJson, BaseVirtualizationPoolAction> actionCreator) {
        return poolAction(request, response, user, server, actionCreator, VirtualPoolBaseActionJson.class);
    }

    private <T extends VirtualPoolBaseActionJson> String poolAction(Request request, Response response, User user,
            Server server,
            Function<T, BaseVirtualizationPoolAction> actionCreator,
            Class<T> jsonClass) {
        return action(request, response, user, server,
               (data, key) -> {
                   BaseVirtualizationPoolAction action = actionCreator.apply(data);
                   action.setPoolName(key);
                   return action;
               },
                VirtualPoolBaseActionJson::getPoolNames,
               jsonClass);
    }

    private <T extends VirtualVolumeBaseActionJson> String volumeAction(Request request, Response response, User user,
            Server server,
            Function<T, BaseVirtualizationVolumeAction> actionCreator,
            Class<T> jsonClass) {
        return action(request, response, user, server,
                (data, key) -> {
                    BaseVirtualizationVolumeAction action = actionCreator.apply(data);
                    action.setName(action.getActionType().getName() + ": " + key);
                    Matcher m = Pattern.compile("^([^/]+)/(.*)$").matcher(key);
                    if (m.matches()) {
                        action.setPoolName(m.group(1));
                        action.setVolumeName(m.group(2));
                    }
                    return action;
                },
                VirtualVolumeBaseActionJson::getVolumesPath,
                jsonClass
         );
    }
}
