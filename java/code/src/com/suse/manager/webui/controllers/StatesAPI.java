/**
 * Copyright (c) 2015 SUSE LLC
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

import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.state.PackageState;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.rhnpackage.PackageManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suse.manager.webui.utils.gson.JSONPackageState;
import com.suse.manager.webui.utils.gson.JSONServerPackageStates;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import spark.Request;
import spark.Response;

/**
 * Controller class providing the backend for API calls to work with states.
 */
public class StatesAPI {

    private static final Gson GSON = new GsonBuilder().create();

    private StatesAPI() { }

    /**
     * Query the current list of package states for a given server.
     *
     * @param request the request object
     * @param response the response object
     * @return JSON result of the API call
     */
    public static String packages(Request request, Response response) {
        String serverId = request.queryParams("sid");
        Server server = ServerFactory.lookupById(Long.valueOf(serverId));

        // Lookup all revisions and take package states from the latest one
        List<ServerStateRevision> stateRevisions =
                StateFactory.lookupServerStateRevisions(server);
        Set<PackageState> packageStates = new HashSet<>();
        if (stateRevisions != null && !stateRevisions.isEmpty()) {
            packageStates = stateRevisions.get(0).getPackageStates();
        }

        // Convert into a list of DTOs
        List<JSONPackageState> collect = packageStates.stream().map(state ->
                new JSONPackageState(
                        state.getName().getName(),
                        Optional.ofNullable(state.getEvr())
                                .map(PackageEvr::toString).orElse(""),
                        Optional.ofNullable(state.getArch())
                                .map(PackageArch::getLabel).orElse(""),
                        Optional.of(state.getPackageStateTypeId()),
                        Optional.of(state.getVersionConstraintId())
                )
        ).collect(Collectors.toList());

        response.type("application/json");
        return GSON.toJson(collect);
    }

    /**
     * Search all packages available to a given server (installed or not installed) for a
     * given string returning all matching packages.
     *
     * @param request the request object
     * @param response the response object
     * @return JSON result of the API call
     */
    public static String match(Request request, Response response) {
        String target = ".*" + request.queryParams("target") + ".*";
        String serverId = request.queryParams("sid");

        // Find matches among available packages and convert to DTOs
        List<JSONPackageState> matchingPackages = PackageManager
                .systemTotalPackages(Long.valueOf(serverId), null).stream()
                .filter(p -> p.getName().matches(target))
                .map(p -> new JSONPackageState(p.getName(), p.getEvr(), p.getArch()))
                .collect(Collectors.toList());

        response.type("application/json");
        return GSON.toJson(matchingPackages);
    }

    /**
     * Save a new state revision for a server given an incoming list of package states in
     * the request body.
     *
     * @param request the request object
     * @param response the response object
     * @param user the user
     * @return null to make spark happy
     */
    public static Object save(Request request, Response response, User user) {
        // Parse the JSON content of the body into an object
        JSONServerPackageStates dto = GSON.fromJson(request.body(),
                JSONServerPackageStates.class);

        // Save a new state revision for the specified server
        ServerStateRevision state = new ServerStateRevision();
        state.setServer(ServerFactory.lookupById(dto.getServerId()));
        state.setCreator(user);
        dto.getPackageStates().forEach(pkgState -> {
            pkgState.convertToPackageState().ifPresent(state::addPackageState);
        });
        StateFactory.save(state);

        // Make spark happy
        return null;
    }
}
