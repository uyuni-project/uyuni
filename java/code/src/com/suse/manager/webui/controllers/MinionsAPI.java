/*
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

import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.result;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withOrgAdmin;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserAndServer;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.rhnpackage.PackageAction;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.SetLabels;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.struts.SessionSetHelper;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.attestation.AttestationManager;
import com.suse.manager.model.attestation.ServerCoCoAttestationConfig;
import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.utils.SaltKeyUtils;
import com.suse.manager.webui.controllers.bootstrap.RegularMinionBootstrapper;
import com.suse.manager.webui.controllers.bootstrap.SSHMinionBootstrapper;
import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.impl.MinionPendingRegistrationService;
import com.suse.manager.webui.utils.MinionActionUtils;
import com.suse.manager.webui.utils.PageControlHelper;
import com.suse.manager.webui.utils.gson.BootstrapHostsJson;
import com.suse.manager.webui.utils.gson.BootstrapParameters;
import com.suse.manager.webui.utils.gson.CoCoAttestationReportJson;
import com.suse.manager.webui.utils.gson.CoCoSettingsJson;
import com.suse.manager.webui.utils.gson.ListKeysJson;
import com.suse.manager.webui.utils.gson.PackageActionJson;
import com.suse.manager.webui.utils.gson.PagedDataResultJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.utils.gson.SaltMinionJson;
import com.suse.manager.webui.utils.gson.ScheduledRequestJson;
import com.suse.manager.webui.utils.gson.ServerSetProxyJson;
import com.suse.salt.netapi.calls.wheel.Key;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import spark.Request;
import spark.Response;

/**
 * Controller class providing backend code for the minions page.
 */
public class MinionsAPI {

    public static final String SALT_CMD_RUN_TARGETS = "salt_cmd_run_targets";

    private static final LocalizationService LOCAL = LocalizationService.getInstance();

    private final SaltApi saltApi;
    private final SSHMinionBootstrapper sshMinionBootstrapper;
    private final RegularMinionBootstrapper regularMinionBootstrapper;
    private final SaltKeyUtils saltKeyUtils;

    private final AttestationManager attestationManager;

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
            .registerTypeAdapter(BootstrapHostsJson.AuthMethod.class, new AuthMethodAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .serializeNulls()
            .create();

    private static final Logger LOG = LogManager.getLogger(MinionsAPI.class);

    /**
     * @param saltApiIn instance to use.
     * @param regularMinionBootstrapperIn regular bootstrapper
     * @param sshMinionBootstrapperIn ssh bootstrapper
     * @param saltKeyUtilsIn salt key utils instance
     * @param attestationManagerIn the attestation manager
     */
    public MinionsAPI(SaltApi saltApiIn, SSHMinionBootstrapper sshMinionBootstrapperIn,
                      RegularMinionBootstrapper regularMinionBootstrapperIn,
                      SaltKeyUtils saltKeyUtilsIn, AttestationManager attestationManagerIn) {
        this.saltApi = saltApiIn;
        this.sshMinionBootstrapper = sshMinionBootstrapperIn;
        this.regularMinionBootstrapper = regularMinionBootstrapperIn;
        this.saltKeyUtils = saltKeyUtilsIn;
        this.attestationManager = attestationManagerIn;
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
        post("/manager/api/systems/proxy", asJson(withOrgAdmin(this::setProxy)));
        get("/manager/api/systems/:sid/details/ptf/allowedActions",
            asJson(withUserAndServer(this::allowedPtfActions)));
        get("/manager/api/systems/:sid/details/ptf/installed",
            asJson(withUserAndServer(this::installedPtfsForSystem)));
        get("/manager/api/systems/:sid/details/ptf/available",
            asJson(withUserAndServer(this::availablePtfsForSystem)));
        post("/manager/api/systems/:sid/details/ptf/scheduleAction",
            asJson(withUserAndServer(this::schedulePtfAction)));
        get("/manager/api/systems/:sid/details/coco/settings",
            asJson(withUserAndServer(this::getCoCoSettings)));
        post("/manager/api/systems/:sid/details/coco/settings",
            asJson(withUserAndServer(this::setCoCoSettings)));
        get("/manager/api/systems/:sid/details/coco/listAttestations",
            asJson(withUserAndServer(this::listAllAttestations)));
        post("/manager/api/systems/:sid/details/coco/scheduleAction",
            asJson(withUserAndServer(this::scheduleCoCoAttestation)));
    }

    /**
     * API endpoint to get all minions matching a target glob
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return json result of the API call
     */
    public String listKeys(Request request, Response response, User user) {
        Key.Fingerprints fingerprints = saltApi.getFingerprints();

        Map<String, Object> data = new TreeMap<>();
        boolean isOrgAdmin = user.hasRole(RoleFactory.ORG_ADMIN);

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

        var minions = SaltMinionJson.fromFingerprints(fingerprints, visibleToUser, isVisible);
        return json(response, new ListKeysJson(isOrgAdmin, minions), new TypeToken<>() { });
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
        MinionPendingRegistrationService.addMinion(user, target, ContactMethodUtil.DEFAULT);
        try {
            saltApi.acceptKey(target);
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
        return json(response, saltKeyUtils.deleteSaltKey(user, target));
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
        saltApi.rejectKey(target);
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
        BootstrapHostsJson input = GSON.fromJson(request.body(), BootstrapHostsJson.class);
        BootstrapParameters params = regularMinionBootstrapper.createBootstrapParams(input);
        String defaultContactMethod = ContactMethodUtil.getRegularMinionDefault();
        return json(response, regularMinionBootstrapper.bootstrap(params, user, defaultContactMethod).asJson());
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
        BootstrapHostsJson input = GSON.fromJson(request.body(), BootstrapHostsJson.class);
        BootstrapParameters params = sshMinionBootstrapper.createBootstrapParams(input);
        String defaultContactMethod = ContactMethodUtil.getSSHMinionDefault();
        return json(response, sshMinionBootstrapper.bootstrap(params, user, defaultContactMethod).asJson());
    }

    private static class AuthMethodAdapter extends TypeAdapter<BootstrapHostsJson.AuthMethod> {
        @Override
        public BootstrapHostsJson.AuthMethod read(JsonReader in) throws IOException {
            try {
                if (in.peek().equals(JsonToken.NULL)) {
                    in.nextNull();
                    return null;
                }
                String authMethod = in.nextString();
                return BootstrapHostsJson.AuthMethod.parse(authMethod);
            }
            catch (IllegalArgumentException e) {
                throw new JsonParseException(e);
            }
        }

        @Override
        public void write(JsonWriter jsonWriter, BootstrapHostsJson.AuthMethod authMethod) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * API endpoint to set a proxy
     *
     * @param req the request object
     * @param res the response object
     * @param user the current user
     * @return json result of the API call
     */
    public String setProxy(Request req, Response res, User user) {
        ServerSetProxyJson rq = GSON.fromJson(req.body(),
                ServerSetProxyJson.class);

        try {
             Map<String, Object> data = new TreeMap<>();
             List<Long> actions = ActionManager.changeProxy(user, rq.getIds(), rq.getProxy());
             if (actions.isEmpty()) {
                 throw new RuntimeException("No action in schedule result");
             }
             data.put("actions", actions);
             return json(GSON, res, ResultJson.success(data), new TypeToken<>() { });
        }
        catch (Exception e) {
            LOG.error("Could not change proxy", e);
            res.status(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return "{}";
        }
    }

    /**
     * API to list the allowed actions for this user that can perfomed on PTF
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @param server the current server
     * @return json list of allowed actions
     */
    private String allowedPtfActions(Request request, Response response, User user, Server server) {
        if (!server.doesOsSupportPtf()) {
            return result(response, ResultJson.success(Collections.emptyList()), new TypeToken<>() { });
        }

        List<String> allowedActions = new ArrayList<>();
        if (SystemManager.serverHasFeature(server.getId(), "ftr_package_remove") &&
            ServerFactory.isPtfUninstallationSupported(server)) {
            allowedActions.add(ActionFactory.TYPE_PACKAGES_REMOVE.getLabel());
        }

        if (SystemManager.serverHasFeature(server.getId(), "ftr_package_updates")) {
            allowedActions.add(ActionFactory.TYPE_PACKAGES_UPDATE.getLabel());
        }

        return result(response, ResultJson.success(allowedActions), new TypeToken<>() { });
    }

    /**
     * API to list the installed ptf on the given system
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @param server the current server
     * @return json paged list of ptf installed on the given system
     */
    public String installedPtfsForSystem(Request request, Response response, User user, Server server) {
        PageControl pc = getPageControl(request);
        DataResult<PackageListItem> resultList = PackageManager.systemPtfList(server.getId(), pc);

        // Check if the request is for all the selction keys
        if ("id".equals(request.queryParams("f"))) {
            return json(response, listAllSelectionKey(resultList));
        }

        Set<String> selectedItems = getSessionSet(request, server, SetLabels.PTF_LIST_REMOVE);
        return json(response, new PagedDataResultJson<>(resultList, selectedItems), new TypeToken<>() { });
    }

    /**
     * API to list the ptf available for installation for the given system
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @param server the current server
     * @return json paged list of ptf that can be installed on the given system
     */
    public String availablePtfsForSystem(Request request, Response response, User user, Server server) {
        PageControl pc = getPageControl(request);
        DataResult<PackageListItem> resultList;
        try {
            resultList = PackageManager.systemAvailablePtf(server.getId(), pc);
        }
        catch (RuntimeException ex) {
            LOG.error("Unable to execute query", ex);
            throw ex;
        }

        // Check if the request is for all the selction keys
        if ("id".equals(request.queryParams("f"))) {
            return json(response, listAllSelectionKey(resultList));
        }

        Set<String> selectedItems = getSessionSet(request, server, SetLabels.PTF_INSTALL);
        return json(response, new PagedDataResultJson<>(resultList, selectedItems), new TypeToken<>() { });
    }

    /**
     * API to remove the currently selected PTFs from the current system
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @param server the current server
     * @return id of the schedule action or of the updated action chain
     */
    public String schedulePtfAction(Request request, Response response, User user, Server server) {
        PackageActionJson scheduleRequest = GSON.fromJson(request.body(), PackageActionJson.class);

        Date earliestDate = MinionActionUtils.getScheduleDate(scheduleRequest.getEarliest());
        ActionChain chain = MinionActionUtils.getActionChain(scheduleRequest.getActionChain(), user);
        List<Map<String, Long>> pkgMap = scheduleRequest.getSelectedPackageMap();

        Long result;

        try {
            PackageAction action;

            if (ActionFactory.TYPE_PACKAGES_REMOVE.getLabel().equals(scheduleRequest.getActionType())) {
                action = ActionChainManager.schedulePackageRemoval(user, server, pkgMap, earliestDate, chain);
            }
            else if (ActionFactory.TYPE_PACKAGES_UPDATE.getLabel().equals(scheduleRequest.getActionType())) {
                action = ActionChainManager.schedulePackageInstall(user, server, pkgMap, earliestDate, chain);
            }
            else {
                LOG.warn("Unable to schedule unknown action {}", scheduleRequest.getActionType());
                return json(GSON, response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Unable to schedule unknown action " + scheduleRequest.getActionType()));
            }

            result = chain != null ? chain.getId() : action.getId();
        }
        catch (TaskomaticApiException e) {
            LOG.error("Unable to schedule package remove action", e);
            return json(GSON, response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                ResultJson.error("Unable to schedule action"), new TypeToken<>() { });
        }

        return json(GSON, response, result.longValue());
    }

    private static PageControl getPageControl(Request request) {
        PageControlHelper pageHelper = new PageControlHelper(request, "nvre");
        PageControl pc = pageHelper.getPageControl();
        pc.setFilterColumn("nvre");

        // When getting ids for the select all we just get all systems ID matching the filter, no paging
        if ("id".equals(pageHelper.getFunction())) {
            return null; // null page control getting them all
        }

        return pc;
    }

    private static List<String> listAllSelectionKey(DataResult<PackageListItem> resultList) {
        return resultList.stream()
                         .map(PackageListItem::getSelectionKey)
                         .collect(Collectors.toList());
    }

    private static Set<String> getSessionSet(Request request, Server server, String setLabel) {
        return SessionSetHelper.lookupAndBind(request.raw(), setLabel + server.getId());
    }


    /**
     * Get current coco attestation settings for the current server
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @param server the current server
     * @return the current coco settings as json object
     */
    public String getCoCoSettings(Request request, Response response, User user, Server server) {
        if (!server.doesOsSupportCoCoAttestation()) {
            return json(GSON, response, ResultJson.success(new CoCoSettingsJson(false),
                LOCAL.getMessage("system.audit.coco.unsupported")), new TypeToken<>() { });
        }

        CoCoSettingsJson jsonConfig = attestationManager.getConfig(user, server)
            .map(cfg -> new CoCoSettingsJson(cfg))
            .orElseGet(() -> new CoCoSettingsJson(true));

        return json(GSON, response, ResultJson.success(jsonConfig), new TypeToken<>() { });
    }


    private String setCoCoSettings(Request request, Response response, User user, Server server) {
        if (!server.doesOsSupportCoCoAttestation()) {
            return json(GSON, response, ResultJson.success(new CoCoSettingsJson(false),
                    LOCAL.getMessage("system.audit.coco.unsupported")), new TypeToken<>() { });
        }

        CoCoSettingsJson jsonConfig = GSON.fromJson(request.body(), CoCoSettingsJson.class);
        try {
            ServerCoCoAttestationConfig updatedConfig = attestationManager.getConfig(user, server)
                .map(cfg -> {
                    cfg.setEnabled(jsonConfig.isEnabled());
                    cfg.setEnvironmentType(jsonConfig.getEnvironmentType());
                    cfg.setAttestOnBoot(jsonConfig.isAttestOnBoot());

                    attestationManager.saveConfig(user, cfg);

                    return cfg;
                })
                .orElseGet(() -> {
                    return attestationManager.createConfig(user, server,
                        jsonConfig.getEnvironmentType(),
                        jsonConfig.isEnabled(),
                        jsonConfig.isAttestOnBoot()
                    );
                });

            return json(GSON, response, ResultJson.success(new CoCoSettingsJson(updatedConfig),
                LOCAL.getMessage("system.audit.coco.configUpdated")), new TypeToken<>() { });
        }
        catch (RuntimeException ex) {
            return json(GSON, response, ResultJson.error(LOCAL.getMessage("system.audit.coco.configNotUpdated")),
                new TypeToken<>() { });
        }
    }

    private String listAllAttestations(Request request, Response response, User user, Server server) {
        PageControlHelper pageHelper = new PageControlHelper(request);
        PageControl pc = pageHelper.getPageControl();

        long totalSize = attestationManager.countCoCoAttestationReports(user, server);

        List<CoCoAttestationReportJson> reportsJson = attestationManager.listCoCoAttestationReports(user, server, pc)
            .stream()
            .map(CoCoAttestationReportJson::new)
            .collect(Collectors.toList());

        return json(GSON, response, new PagedDataResultJson<>(reportsJson, totalSize, Collections.emptySet()),
            new TypeToken<>() { });
    }

    private String scheduleCoCoAttestation(Request request, Response response, User user, Server server) {
        if (!server.doesOsSupportCoCoAttestation()) {
            return json(GSON, response, ResultJson.error("system.audit.coco.unsupported"), new TypeToken<>() { });
        }

        ScheduledRequestJson scheduleRequest = GSON.fromJson(request.body(), ScheduledRequestJson.class);

        Date earliestDate = MinionActionUtils.getScheduleDate(scheduleRequest.getEarliest());
        ActionChain chain = MinionActionUtils.getActionChain(scheduleRequest.getActionChain(), user);

        Long result;

        try {
            MinionServer minion = server.asMinionServer().orElse(null);
            if (minion == null) {
                return json(GSON, response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ResultJson.error("System is not a minion"), new TypeToken<>() { });
            }

            Action action = attestationManager.scheduleAttestationAction(user, minion, earliestDate, chain);
            result = chain != null ? chain.getId() : action.getId();
        }
        catch (TaskomaticApiException e) {
            LOG.error("Unable to schedule attestation action", e);
            return json(GSON, response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                ResultJson.error("Unable to schedule action"), new TypeToken<>() { });
        }

        return json(GSON, response, result.longValue());
    }

}
