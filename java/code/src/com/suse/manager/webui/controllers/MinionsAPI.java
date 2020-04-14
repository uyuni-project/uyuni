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
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;

import com.suse.manager.utils.SaltKeyUtils;
import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import com.suse.manager.webui.controllers.utils.RegularMinionBootstrapper;
import com.suse.manager.webui.controllers.utils.SSHMinionBootstrapper;
import com.suse.manager.webui.services.impl.MinionPendingRegistrationService;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.utils.gson.BootstrapHostsJson;
import com.suse.manager.webui.utils.gson.SaltMinionJson;
import com.suse.salt.netapi.calls.wheel.Key;
import org.apache.log4j.Logger;
import spark.Request;
import spark.Response;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withOrgAdmin;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.get;
import static spark.Spark.post;

/**
 * Controller class providing backend code for the minions page.
 */
public class MinionsAPI {

    public static final String SALT_CMD_RUN_TARGETS = "salt_cmd_run_targets";

    private final SystemQuery systemQuery;
    private final SSHMinionBootstrapper sshMinionBootstrapper;
    private final RegularMinionBootstrapper regularMinionBootstrapper;

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
            .serializeNulls()
            .create();

    private static final Logger LOG = Logger.getLogger(MinionsAPI.class);

    /**
     * @param systemQueryIn instance to use.
     * @param regularMinionBootstrapperIn regular bootstrapper
     * @param sshMinionBootstrapperIn ssh bootstrapper
     */
    public MinionsAPI(SystemQuery systemQueryIn, SSHMinionBootstrapper sshMinionBootstrapperIn,
                      RegularMinionBootstrapper regularMinionBootstrapperIn) {
        this.systemQuery = systemQueryIn;
        sshMinionBootstrapper = sshMinionBootstrapperIn;
        regularMinionBootstrapper = regularMinionBootstrapperIn;

    }

    /**
     * Invoked from Router. Initializes routes.
     */
    public void initRoutes() {
        post("/manager/api/systems/bootstrap", withOrgAdmin(this::bootstrap));
        post("/manager/api/systems/bootstrap-ssh", withOrgAdmin(this::bootstrapSSH));
        get("/manager/api/systems/keys", withUser(this::listKeys));
        post("/manager/api/systems/keys/:target/accept", withOrgAdmin(this::accept));
        post("/manager/api/systems/keys/:target/reject", withOrgAdmin(this::reject));
        post("/manager/api/systems/keys/:target/delete", withOrgAdmin(this::delete));
    }

    /**
     * API endpoint to get all minions matching a target glob
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return json result of the API call
     */
    public String listKeys(Request request, Response response, User user) {
        Key.Fingerprints fingerprints = systemQuery.getFingerprints();

        Map<String, Object> data = new TreeMap<>();
        data.put("isOrgAdmin", user.hasRole(RoleFactory.ORG_ADMIN));

        Set<String> minionIds = Stream.of(
                fingerprints.getMinions(),
                fingerprints.getDeniedMinions(),
                fingerprints.getRejectedMinions(),
                fingerprints.getUnacceptedMinions()
        ).flatMap(s -> s.keySet()
         .stream())
         .collect(Collectors.toSet());

        Map<String, Long> serverIdMapping = MinionServerFactory
                .lookupByMinionIds(minionIds)
                .stream().collect(Collectors.toMap(
                        MinionServer::getMinionId, MinionServer::getId));

        Map<String, Long> visibleToUser = MinionServerFactory.lookupVisibleToUser(user)
                .collect(Collectors.toMap(MinionServer::getMinionId, MinionServer::getId));

        Predicate<String> isVisible = (minionId) ->
            visibleToUser.containsKey(minionId) || !serverIdMapping.containsKey(minionId);

        data.put("minions", SaltMinionJson.fromFingerprints(
                fingerprints, visibleToUser, isVisible));
        return json(response, data);
    }

    /**
     * API endpoint to accept minion keys
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return json result of the API call
     */
    public String accept(Request request, Response response, User user) {
        String target = request.params("target");
        MinionPendingRegistrationService.addMinion(
                user, target, ContactMethodUtil.DEFAULT, Optional.empty());
        try {
            systemQuery.acceptKey(target);
        }
        catch (Exception e) {
            MinionPendingRegistrationService.removeMinion(target);
            throw e;
        }
        return json(response, true);
    }

    /**
     * API endpoint to delete minion keys
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return json result of the API call
     */
    public String delete(Request request, Response response, User user) {
        String target = request.params("target");

        // Is org admin checked somewhere else ?
        if (!user.hasRole(RoleFactory.ORG_ADMIN)) {
            throw new PermissionCheckFailureException(RoleFactory.ORG_ADMIN);
        }
        return json(response, SaltKeyUtils.deleteSaltKey(user, target));
    }

    /**
     * API endpoint to reject minion keys
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return json result of the API call
     */
    public String reject(Request request, Response response, User user) {
        String target = request.params("target");
        systemQuery.rejectKey(target);
        return json(response, true);
    }

    /**
     * API endpoint for bootstrapping minions.
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return json result of the API call
     */
    public String bootstrap(Request request, Response response, User user) {
        return json(
                response,
                regularMinionBootstrapper.bootstrap(
                        GSON.fromJson(request.body(), BootstrapHostsJson.class),
                        user, ContactMethodUtil.getRegularMinionDefault()).asMap());
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
    public String bootstrapSSH(Request request, Response response, User user) {
        return json(
                response,
                sshMinionBootstrapper.bootstrap(
                        GSON.fromJson(request.body(), BootstrapHostsJson.class),
                        user, ContactMethodUtil.getSSHMinionDefault()).asMap());
    }

}
