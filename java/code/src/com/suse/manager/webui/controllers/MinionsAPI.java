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

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;

import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.services.impl.SaltAPIService;
import spark.Request;
import spark.Response;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpStatus;

import com.redhat.rhn.domain.user.User;
import com.suse.salt.netapi.datatypes.target.MinionList;

/**
 * Controller class providing backend code for the minions page.
 */
public class MinionsAPI {

    public static final String SALT_CMD_RUN_TARGETS = "salt_cmd_run_targets";

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

        Set<String> minionTargets = request.session().attribute(SALT_CMD_RUN_TARGETS);
        if (minionTargets == null) {
            response.status(HttpStatus.SC_BAD_REQUEST);
            return json(response, Arrays.asList("Click preview first"));
        }

        MinionList minionList = new MinionList(minionTargets
                .toArray(new String[minionTargets.size()]));
        Map<String, String> result = SALT_SERVICE.runRemoteCommand(minionList, cmd);

        return json(response, result);
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

        Set<String> minions = SALT_SERVICE.getAllowedMinions(user, target);

        // Keep the list of allowed minions in the session to make sure
        // the user cannot tamper with it and also for scalability reasons.
        request.session().attribute(SALT_CMD_RUN_TARGETS, minions);

        return json(response, minions);
    }

}
