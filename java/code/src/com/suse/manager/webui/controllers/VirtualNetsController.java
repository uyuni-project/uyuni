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
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.get;

import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.SystemManager;

import com.google.gson.JsonObject;
import com.suse.manager.webui.errors.NotFoundException;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.utils.gson.VirtualNetworkInfoJson;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller class providing backend for Virtual networks UI
 */
public class VirtualNetsController {

    private VirtManager virtManager;

    /**
     * Controller class providing backend for Virtual networks UI
     * @param virtManagerIn instance to manage virtualization
     */
    public VirtualNetsController(VirtManager virtManagerIn) {
        this.virtManager = virtManagerIn;
    }

    /**
     * Initialize request routes for the pages served by VirtualNetsController
     *
     * @param jade jade engine
     */
    public void initRoutes(JadeTemplateEngine jade) {
        get("/manager/api/systems/details/virtualization/nets/:sid/data",
                withUser(this::data));
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
        Long serverId;

        try {
            serverId = Long.parseLong(request.params("sid"));
        }
        catch (NumberFormatException e) {
            throw new NotFoundException();
        }
        Server host = SystemManager.lookupByIdAndUser(serverId, user);
        String minionId = host.asMinionServer().orElseThrow(() -> new NotFoundException()).getMinionId();

        Map<String, JsonObject> infos = virtManager.getNetworks(minionId);
        List<VirtualNetworkInfoJson> networks = infos.entrySet().stream().map(entry -> {
            VirtualNetworkInfoJson net = new VirtualNetworkInfoJson(entry.getKey(),
                    entry.getValue());

            return net;
        }).collect(Collectors.toList());

        return json(response, networks);
    }
}
