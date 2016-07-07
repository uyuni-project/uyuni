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

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.manager.webui.utils.InputValidator;
import com.suse.manager.webui.utils.SaltRoster;
import com.suse.manager.webui.utils.gson.JSONBootstrapHosts;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.calls.wheel.Key;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;

import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.results.SSHResult;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Controller class providing backend code for the minions page.
 */
public class MinionsAPI {

    public static final String SALT_CMD_RUN_TARGETS = "salt_cmd_run_targets";

    private static final SaltService SALT_SERVICE = SaltAPIService.INSTANCE;

    private static final Gson GSON = new GsonBuilder()
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
        JSONBootstrapHosts input = GSON.fromJson(request.body(), JSONBootstrapHosts.class);
        List<String> validationErrors = InputValidator.validateBootstrapInput(input);
        if (!validationErrors.isEmpty()) {
            // TODO: Display lists of error messages in the UI
            return bootstrapResult(response, false, Optional.of(validationErrors.get(0)));
        }

        // Setup pillar data to be passed when applying the bootstrap state
        Map<String, Object> pillarData = new HashMap<>();
        pillarData.put("master", ConfigDefaults.get().getCobblerHost());

        try {
            // Generate (temporary) roster file based on data from the UI
            SaltRoster saltRoster = new SaltRoster();
            saltRoster.addHost(input.getHost(), input.getUser(), input.getPassword(),
                    input.getPortInteger());
            Path rosterFilePath = saltRoster.persistInTempFile();
            String roster = rosterFilePath.toString();
            LOG.debug("Roster file: " + roster);

            // Apply the bootstrap state
            LOG.info("Bootstrapping host: " + input.getHost());
            List<String> bootstrapMods = Arrays.asList(
                    ApplyStatesEventMessage.CERTIFICATE, "bootstrap");
            LocalCall<Map<String, State.ApplyResult>> call = State.apply(
                    bootstrapMods, Optional.of(pillarData), Optional.of(true));
            Map<String, Result<SSHResult<Map<String, State.ApplyResult>>>> results =
                    SALT_SERVICE.callSyncSSH(call, new MinionList(input.getHost()),
                            input.getIgnoreHostKeys(), roster,
                            !"root".equals(input.getUser()));

            // Delete the roster file
            Files.delete(rosterFilePath);

            // Check if bootstrap was successful
            return results.get(input.getHost()).fold(
                    error -> {
                        LOG.error("Error during bootstrap: " + error.toString());
                        return bootstrapResult(response,
                                false, Optional.of(error.toString()));
                    },
                    r -> {
                        // We have results, check if result = true for all the single states
                        Optional<String> message = Optional.empty();
                        boolean stateApplyResult = r.getReturn().isPresent();
                        if (stateApplyResult) {
                            for (State.ApplyResult apply : r.getReturn().get().values()) {
                                if (!apply.isResult()) {
                                    stateApplyResult = false;
                                    message = Optional.of("Bootstrap failed (retcode=" +
                                            r.getRetcode() + "): " + apply.getComment());
                                    break;
                                }
                            }
                        }
                        else {
                            message = Optional.of(r.getStdout().filter(s -> !s.isEmpty())
                                    .orElseGet(() -> r.getStderr().filter(s -> !s.isEmpty())
                                    .orElseGet(() -> "No result for " + input.getHost())));
                            message.ifPresent(msg -> LOG.info(msg));
                        }
                        return bootstrapResult(response,
                                stateApplyResult && r.getRetcode() == 0, message);
                    }
            );
        }
        catch (IOException e) {
            LOG.error("Error operating on roster file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static String bootstrapResult(Response response, boolean success,
            Optional<String> message) {
        Map<String, Object> ret = new LinkedHashMap<>();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Bootstrap success: " + success);
        }
        ret.put("success", success);
        message.ifPresent(value -> ret.put("errorMessage", value));
        return json(response, ret);
    }
}
