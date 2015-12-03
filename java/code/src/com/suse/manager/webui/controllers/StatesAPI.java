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
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.manager.rhnpackage.PackageManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.suse.manager.webui.utils.gson.PackageStateDto;
import spark.Request;
import spark.Response;

/**
 * Controller class providing the backend for API calls to work with states.
 */
public class StatesAPI {

    private static final Gson GSON = new GsonBuilder().create();



    private StatesAPI() { }

    /**
     * API endpoint to query the current list of package states for a given system.
     *
     * @param request the request object
     * @param response the response object
     * @return json result of the API call
     */
    public static String packages(Request request, Response response) {
        String serverId = request.queryParams("sid");
        Server server = ServerFactory.lookupById(new Long(serverId));
        // Lookup all revisions and take package states from the latest one
        List<ServerStateRevision> stateRevisions =
                StateFactory.lookupServerStateRevisions(server);
        Set<PackageState> packageStates = new HashSet<>();
        if (stateRevisions != null && !stateRevisions.isEmpty()) {
            packageStates = stateRevisions.get(0).getPackageStates();
        }
        response.type("application/json");
        List<PackageStateDto> collect = packageStates.stream().map(state ->
                new PackageStateDto(
                        state.getName().getName(),
                        Optional.ofNullable(state.getEvr()).map(PackageEvr::toString).orElse(""),
                        Optional.ofNullable(state.getArch()).map(PackageArch::getLabel).orElse(""),
                        state.getPackageStateTypeId(),
                        state.getVersionConstraintId()
                )
        ).collect(Collectors.toList());
        return GSON.toJson(collect);
    }

    /**
     * API endpoint to search all packages available to a given server (installed or not
     * installed) for a given string returning all matching packages.
     *
     * @param request the request object
     * @param response the response object
     * @return json result of the API call
     */
    public static String match(Request request, Response response) {
        String target = ".*" + request.queryParams("target") + ".*";
        String serverId = request.queryParams("sid");
        List<PackageListItem> matchingPackages = PackageManager
                .systemTotalPackages(Long.valueOf(serverId), null).stream()
                .filter(pkg -> pkg.getName().matches(target))
                .collect(Collectors.toList());
        response.type("application/json");
        return GSON.toJson(matchingPackages);
    }
}
