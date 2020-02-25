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
package com.suse.manager.webui.controllers;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.system.SystemManager;

import com.google.gson.JsonObject;
import com.suse.manager.virtualization.PoolCapabilitiesJson;
import com.suse.manager.virtualization.PoolDefinition;
import com.suse.manager.virtualization.VirtManager;
import com.suse.manager.webui.errors.NotFoundException;
import com.suse.manager.webui.utils.gson.VirtualStoragePoolInfoJson;
import com.suse.manager.webui.utils.gson.VirtualStorageVolumeInfoJson;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller class providing backend for Virtual storage pools UI
 */
public class VirtualPoolsController {

    private final VirtManager virtManager;

    /**
     * Controller class providing backend for Virtual storage pools UI
     * @param virtManagerIn instance to manage virtualization
     */
    public VirtualPoolsController(VirtManager virtManagerIn) {
        this.virtManager = virtManagerIn;
    }

    /**
     * Initialize request routes for the pages served by VirtualPoolsController
     *
     * @param jade jade engine
     */
    public void initRoutes(JadeTemplateEngine jade) {
        get("/manager/systems/details/virtualization/storage/:sid",
                withUserPreferences(withCsrfToken(withUser(this::show))), jade);
        get("/manager/api/systems/details/virtualization/pools/:sid/data",
                withUser(this::data));
        get("/manager/api/systems/details/virtualization/pools/:sid/capabilities",
                withUser(this::getCapabilities));
        get("/manager/api/systems/details/virtualization/pools/:sid/pool/:name",
                withUser(this::getPool));
    }

    /**
     * Displays the virtual storages page.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the ModelAndView object to render the page
     */
    public ModelAndView show(Request request, Response response, User user) {
        return renderPage(request, response, user, "show", null);
    }


    /**
     * Returns JSON data describing the storage pools
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON result of the API call
     */
    public String data(Request request, Response response, User user) {
        Long serverId;

        try {
            serverId = Long.parseLong(request.params("sid"));
        }
        catch (NumberFormatException e) {
            throw new NotFoundException();
        }
        Server host = SystemManager.lookupByIdAndUser(serverId, user);
        String minionId = host.asMinionServer().orElseThrow(() -> new NotFoundException()).getMinionId();

        Map<String, JsonObject> infos = virtManager.getPools(minionId);
        Map<String, Map<String, JsonObject>> volInfos = virtManager.getVolumes(minionId);
        List<VirtualStoragePoolInfoJson> pools = infos.entrySet().stream().map(entry -> {
            Map<String, JsonObject> poolVols = volInfos.getOrDefault(entry.getKey(), new HashMap<>());
            List<VirtualStorageVolumeInfoJson> volumes = poolVols.entrySet().stream().map(volEntry -> {
                return new VirtualStorageVolumeInfoJson(volEntry.getKey(), volEntry.getValue());
            }).collect(Collectors.toList());

            VirtualStoragePoolInfoJson pool = new VirtualStoragePoolInfoJson(entry.getKey(),
                    entry.getValue(), volumes);

            return pool;
        }).collect(Collectors.toList());

        return json(response, pools);
    }

    /**
     * Executes the GET query to extract the pool capabilities
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON-formatted capabilities
     */
    public String getCapabilities(Request request, Response response, User user) {
        Long serverId;
        try {
            serverId = Long.parseLong(request.params("sid"));
        }
        catch (NumberFormatException e) {
            throw new NotFoundException();
        }

        Server host = SystemManager.lookupByIdAndUser(serverId, user);
        String minionId = host.asMinionServer().orElseThrow(() ->
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Can only get capabilities of Salt system")).getMinionId();

        PoolCapabilitiesJson caps = virtManager.getPoolCapabilities(minionId)
            .orElseThrow(() -> Spark.halt(HttpStatus.SC_BAD_REQUEST,
                "Failed to get virtual host storage pool capabilities"));

        return json(response, caps);
    }

/**
     * Executes the GET query to extract the pool definition
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON-formatted capabilities
     */
    public String getPool(Request request, Response response, User user) {
        Long serverId;
        try {
            serverId = Long.parseLong(request.params("sid"));
        }
        catch (NumberFormatException e) {
            throw new NotFoundException();
        }

        Server host = SystemManager.lookupByIdAndUser(serverId, user);
        String minionId = host.asMinionServer().orElseThrow(() ->
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Can only get pool definition of Salt system")).getMinionId();

        String poolName = request.params("name");
        PoolDefinition definition = virtManager.getPoolDefinition(minionId, poolName)
                .orElseThrow(() -> new NotFoundException());

        return json(response, definition);
    }

    /**
     * Displays a page server-related virtual page
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @param template the name to the Jade template of the page
     * @param modelExtender provides additional properties to pass to the Jade template
     * @return the ModelAndView object to render the page
     */
    private static ModelAndView renderPage(Request request, Response response, User user,
                                          String template,
                                          Supplier<Map<String, Object>> modelExtender) {
        Map<String, Object> data = new HashMap<>();
        Long serverId;
        Server server;

        try {
            serverId = Long.parseLong(request.params("sid"));
        }
        catch (NumberFormatException e) {
            throw new NotFoundException();
        }

        try {
            server = SystemManager.lookupByIdAndUser(serverId, user);
        }
        catch (LookupException e) {
            throw new NotFoundException();
        }

        /* For system-common.jade */
        data.put("server", server);
        data.put("inSSM", RhnSetDecl.SYSTEMS.get(user).contains(serverId));

        if (modelExtender != null) {
            data.putAll(modelExtender.get());
        }

        /* For the rest of the template */

        return new ModelAndView(data, String.format("templates/virtualization/pools/%s.jade", template));
    }
}
