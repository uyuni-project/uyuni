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
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.token.ActivationKeyManager;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.InputValidator;
import com.suse.manager.webui.utils.gson.BootstrapParameters;
import com.suse.manager.webui.utils.gson.JSONBootstrapHosts;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.calls.wheel.Key;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.results.SSHResult;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import spark.Request;
import spark.Response;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        JSONBootstrapHosts input = GSON.fromJson(request.body(), JSONBootstrapHosts.class);
        List<String> errors = InputValidator.INSTANCE.validateBootstrapInput(input);
        if (!errors.isEmpty()) {
            return bootstrapResult(response, false, errors);
        }

        // Setup pillar data to be passed when applying the bootstrap state
        Map<String, Object> pillarData = new HashMap<>();
        pillarData.put("master", ConfigDefaults.get().getCobblerHost());
        pillarData.put("minion_id", input.getHost());
        ActivationKeyManager.getInstance().findAll(user)
                .stream()
                .filter(ak -> input.getActivationKeys().contains(ak.getKey()))
                .findFirst()
                .ifPresent(ak -> pillarData.put("activation_key", ak.getKey()));

        // Generate minion keys and accept the public key
        if (SALT_SERVICE.keyExists(input.getHost())) {
            return bootstrapResult(response, false, Collections.singletonList(
                    "A key for this host (" +
                    input.getHost() + ") seems to already exist, please check!"));
        }
        com.suse.manager.webui.utils.salt.Key.Pair keyPair =
                SALT_SERVICE.generateKeysAndAccept(input.getHost(), false);
        if (keyPair.getPub().isPresent() && keyPair.getPriv().isPresent()) {
            pillarData.put("minion_pub", keyPair.getPub().get());
            pillarData.put("minion_pem", keyPair.getPriv().get());
        }

        try {
            // Apply the bootstrap state
            LOG.info("Bootstrapping host: " + input.getHost());
            List<String> bootstrapMods = Arrays.asList(
                    ApplyStatesEventMessage.CERTIFICATE, "bootstrap");
            BootstrapParameters parameters = new BootstrapParameters(input.getHost(),
                    input.getPortInteger(), input.getUser(), input.maybeGetPassword(),
                    input.getActivationKeys(), input.getIgnoreHostKeys());
            Result<SSHResult<Map<String, State.ApplyResult>>> result =
                    SALT_SERVICE.bootstrapMinion(parameters, bootstrapMods, pillarData);


            // Check if bootstrap was successful
            return result.fold(
                    error -> {
                        LOG.error("Error during bootstrap: " + error.toString());
                        SALT_SERVICE.deleteKey(input.getHost());
                        return bootstrapResult(response, false,
                                Collections.singletonList(error.toString()));
                    },
                    r -> {
                        // We have results, check if result=true for all the single states
                        List<String> messages;
                        boolean stateApplyResult = r.getReturn().isPresent();
                        if (stateApplyResult) {
                            messages = r.getReturn().get().entrySet().stream().flatMap(
                                    entry -> {
                                if (!entry.getValue().isResult()) {
                                    return Stream.of(entry.getKey() + " (retcode=" +
                                            r.getRetcode() + "): " +
                                            entry.getValue().getComment());
                                }
                                else {
                                    return Stream.empty();
                                }
                            }).collect(Collectors.toList());
                            stateApplyResult = messages.isEmpty();
                            if (!stateApplyResult) {
                                LOG.error(messages.stream().collect(
                                        Collectors.joining("\n")));
                            }
                        }
                        else {
                            messages = Collections.singletonList(
                                    r.getStdout().filter(s -> !s.isEmpty())
                                    .orElseGet(() -> r.getStderr().filter(s -> !s.isEmpty())
                                    .orElseGet(() -> "No result for " + input.getHost()))
                            );
                            LOG.info(messages);
                        }

                        // Clean up the generated key pair in case of failure
                        boolean success = stateApplyResult && r.getRetcode() == 0;
                        if (!success) {
                            SALT_SERVICE.deleteKey(input.getHost());
                        }
                        return bootstrapResult(response, success, messages);
                    }
            );
        }
        catch (SaltException e) {
            LOG.error("Error operating on roster file: " + e.getMessage());
            SALT_SERVICE.deleteKey(input.getHost());
            throw new RuntimeException(e);
        }
    }

    /**
     * Bootstrap a system for being managed via SSH.
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return json result of the API call
     */
    public static String bootstrapSSH(Request request, Response response, User user) {
        JSONBootstrapHosts input = GSON.fromJson(request.body(), JSONBootstrapHosts.class);
        List<String> errors = InputValidator.INSTANCE.validateBootstrapInput(input);
        if (!errors.isEmpty()) {
            return bootstrapResult(response, false,
                    errors.toArray(new String[errors.size()]));
        }

        LOG.info("Register host for SSH based management: " + input.getHost());
        return bootstrapResult(response, true);
    }

    private static String bootstrapResult(Response response, boolean success,
            List<String> messages) {
        LOG.info("Bootstrap success: " + success);
        Map<String, Object> ret = new LinkedHashMap<>();
        ret.put("success", success);
        ret.put("messages", messages);
        return json(response, ret);
    }
}
