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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.suse.manager.webui.controllers.utils.SSHMinionBootstrapper;
import com.suse.manager.webui.controllers.utils.TraditionalMinionBootstrapper;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.gson.JSONBootstrapHosts;
import com.suse.salt.netapi.calls.wheel.Key;
import com.suse.salt.netapi.datatypes.target.MinionList;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import spark.Request;
import spark.Response;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;

/**
 * Controller class providing backend code for the minions page.
 */
public class MinionsAPI {

    public static final String SALT_CMD_RUN_TARGETS = "salt_cmd_run_targets";

    private static final SaltService SALT_SERVICE = SaltService.INSTANCE;

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
            .serializeNulls()
            .create();

    private static final Logger LOG = Logger.getLogger(MinionsAPI.class);

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
        Map<String, String> result = SALT_SERVICE.runRemoteCommand(minionList, cmd)
                .entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().fold(
                                err -> err.fold(
                                        Object::toString,
                                        Object::toString,
                                        genError -> "Generic error running remote command"
                                ),
                                res -> res
                        )
                ));

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

    /**
     * API endpoint to get all minions matching a target glob
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return json result of the API call
     */
    public static String listKeys(Request request, Response response, User user) {
        Key.Fingerprints fingerprints = SALT_SERVICE.getFingerprints();
        Map<String, Object> data = new TreeMap<>();
        data.put("isOrgAdmin", user.hasRole(RoleFactory.ORG_ADMIN));
        data.put("fingerprints", fingerprints);
        return json(response, data);
    }


    /**
     * API endpoint to accept minion keys
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return json result of the API call
     */
    public static String accept(Request request, Response response, User user) {
        String target = request.params("target");
        SALT_SERVICE.acceptKey(target);
        return json(response, true);
    }

    /**
     * API endpoint to delete minion keys
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return json result of the API call
     */
    public static String delete(Request request, Response response, User user) {
        String target = request.params("target");
        SALT_SERVICE.deleteKey(target);
        return json(response, true);
    }

    /**
     * API endpoint to reject minion keys
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return json result of the API call
     */
    public static String reject(Request request, Response response, User user) {
        String target = request.params("target");
        SALT_SERVICE.rejectKey(target);
        return json(response, true);
    }

    /**
     * API endpoint for bootstrapping minions.
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return json result of the API call
     */
    public static String bootstrap(Request request, Response response, User user) {
        return json(
                response,
                TraditionalMinionBootstrapper.getInstance().bootstrap(
                        GSON.fromJson(request.body(), JSONBootstrapHosts.class),
                        user));
    }


    /**
     * Bootstrap a system for being managed via SSH.
     *
     * This also uses the mgr_ssh_identity state module to copy the ssh
     * certificate of the manager to the ssh authorized_keys of the target
     * system (so that for the following salt-ssh calls, no password is
     * needed).
     *
     * @param request  the request object
     * @param response the response object
     * @param user     the current user
     * @return json result of the API call
     */
    public static String bootstrapSSH(Request request, Response response, User user) {
        return json(
                response,
                SSHMinionBootstrapper.getInstance().bootstrap(
                        GSON.fromJson(request.body(), JSONBootstrapHosts.class),
                        user));
    }

}
