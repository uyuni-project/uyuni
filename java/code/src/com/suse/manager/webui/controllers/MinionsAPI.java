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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;

import spark.Request;
import spark.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.user.User;
import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.salt.netapi.datatypes.target.MinionList;

/**
 * Controller class providing backend code for the minions page.
 */
public class MinionsAPI {

    public static final String SALT_CMD_RUN_TARGETS = "salt_cmd_run_targets";

    private static final Gson GSON = new GsonBuilder().create();
    private static final SaltService SALT_SERVICE = SaltAPIService.INSTANCE;


    private MinionsAPI() { }

    /**
     * API endpoint to execute a command on salt minions by target glob
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return json result of the API call
     */
    public static String run(Request request, Response response, User user) {
        String cmd = request.queryParams("cmd");

        Set<String> minionTargets = getSessionTarget(request, false);
        if (minionTargets == null) {
            response.status(HttpStatus.SC_BAD_REQUEST);
            response.type("application/json");
            return GSON.toJson(Arrays.asList("Click preview first"));
        }

        MinionList minionList = new MinionList(minionTargets
                .toArray(new String[minionTargets.size()]));
        Map<String, String> result = SALT_SERVICE.runRemoteCommand(minionList, cmd);
        response.type("application/json");
        return GSON.toJson(result);
    }

    /**
     * API endpoint to get all minions matching a target glob
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return json result of the API call
     */
    public static String match(Request request, Response response, User user) {
        String target = request.queryParams("target");
        Set<String> result = SALT_SERVICE.match(target).keySet();

        filterMinions(request, user, result);

        response.type("application/json");
        return GSON.toJson(result);
    }

    /**
     * Keep only the minions belonging to the users's organization.
     * Keep list of filtered minions in the session to make sure user cannot temper
     * with the list and also for scalability reasons.
     *
     * @param request the request object
     * @param user the response object
     * @param saltMatches all the minions that Salt matched
     */
    private static void filterMinions(Request request, User user, Set<String> saltMatches) {
        Set<String> targets = getSessionTarget(request, true);

        targets.clear();
        targets.addAll(saltMatches);
        List<MinionServer> minionServers = MinionServerFactory
                .findByGroupId(user.getOrg().getId());
        Set<String> minionIds = minionServers.stream()
                .map(s -> s.getMinionId()).collect(Collectors.toSet());
        targets.retainAll(minionIds);
    }

    private static Set<String> getSessionTarget(Request request, boolean createIfAbsent) {
        Set<String> targets = request.session().attribute(SALT_CMD_RUN_TARGETS);
        if (createIfAbsent && targets == null) {
            targets = new HashSet<>();
            request.session().attribute(SALT_CMD_RUN_TARGETS, targets);
        }
        return targets;
    }

}
