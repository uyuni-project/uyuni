/*
 * Copyright (c) 2015--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.webui.controllers;

import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.badRequest;
import static com.suse.manager.webui.utils.SparkApplicationHelper.internalServerError;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.result;
import static com.suse.manager.webui.utils.SparkApplicationHelper.success;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withOrgAdmin;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserAndServer;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.CoCoAttestationAction;
import com.redhat.rhn.domain.action.dup.DistUpgradeAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageAction;
import com.redhat.rhn.domain.action.supportdata.UploadGeoType;
import com.redhat.rhn.domain.product.SUSEProductSet;
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
import com.redhat.rhn.manager.distupgrade.DistUpgradeManager;
import com.redhat.rhn.manager.distupgrade.DistUpgradePaygException;
import com.redhat.rhn.manager.distupgrade.NoInstalledProductException;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.cloud.CloudPaygManager;
import com.suse.manager.attestation.AttestationManager;
import com.suse.manager.model.attestation.ServerCoCoAttestationConfig;
import com.suse.manager.model.products.migration.MigrationChannelsRequest;
import com.suse.manager.model.products.migration.MigrationDataFactory;
import com.suse.manager.model.products.migration.MigrationScheduleRequest;
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
import com.suse.manager.webui.utils.gson.SupportDataRequest;
import com.suse.manager.webui.utils.gson.SystemScheduledRequestJson;
import com.suse.manager.webui.utils.gson.SystemsCoCoSettingsJson;
import com.suse.salt.netapi.calls.wheel.Key;
import com.suse.utils.gson.RecordTypeAdapterFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    private final TaskomaticApi taskomaticApi;

    private final CloudPaygManager cloudPaygManager;

    private final MigrationDataFactory dataFactory;

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
            .registerTypeAdapter(BootstrapHostsJson.AuthMethod.class, new AuthMethodAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
            .serializeNulls()
            .create();

    private static final Logger LOG = LogManager.getLogger(MinionsAPI.class);

    /**
     * @param saltApiIn instance to use.
     * @param regularMinionBootstrapperIn regular bootstrapper
     * @param sshMinionBootstrapperIn ssh bootstrapper
     * @param saltKeyUtilsIn salt key utils instance
     * @param attestationManagerIn the attestation manager
     * @param taskomaticApiIn the taskomatic api client
     * @param cloudPaygManagerIn they payg manager
     * @param dataFactoryIn the migration data factory
     */
    public MinionsAPI(SaltApi saltApiIn, SSHMinionBootstrapper sshMinionBootstrapperIn,
                      RegularMinionBootstrapper regularMinionBootstrapperIn, SaltKeyUtils saltKeyUtilsIn,
                      AttestationManager attestationManagerIn, TaskomaticApi taskomaticApiIn,
                      CloudPaygManager cloudPaygManagerIn, MigrationDataFactory dataFactoryIn) {
        this.saltApi = saltApiIn;
        this.sshMinionBootstrapper = sshMinionBootstrapperIn;
        this.regularMinionBootstrapper = regularMinionBootstrapperIn;
        this.saltKeyUtils = saltKeyUtilsIn;
        this.attestationManager = attestationManagerIn;
        this.taskomaticApi = taskomaticApiIn;
        this.cloudPaygManager = cloudPaygManagerIn;
        this.dataFactory = dataFactoryIn;
    }

    /**
     * Invoked from Router. Initializes routes.
     */
    public void initRoutes() {
        initBootstrapRoutes();
        initKeysRoutes();
        initProxyRoutes();
        initDetailsRoutes();
        initPTFRoutes();
        initCoCoRoutes();
        initMigrationRoutes();
    }

    private void initBootstrapRoutes() {
        post("/manager/api/systems/bootstrap", withOrgAdmin(this::bootstrap));
        post("/manager/api/systems/bootstrap-ssh", withOrgAdmin(this::bootstrapSSH));
    }

    private void initKeysRoutes() {
        get("/manager/api/systems/keys", withUser(this::listKeys));
        post("/manager/api/systems/keys/:target/accept", withOrgAdmin(this::accept));
        post("/manager/api/systems/keys/:target/reject", withOrgAdmin(this::reject));
        post("/manager/api/systems/keys/:target/delete", withOrgAdmin(this::delete));
    }

    private void initProxyRoutes() {
        post("/manager/api/systems/proxy", asJson(withOrgAdmin(this::setProxy)));
    }

    private void initDetailsRoutes() {
        post("/manager/api/systems/:sid/details/uploadSupportData",
            asJson(withUserAndServer(this::uploadSupportData)));
    }

    private void initPTFRoutes() {
        get("/manager/api/systems/:sid/details/ptf/allowedActions",
            asJson(withUserAndServer(this::allowedPtfActions)));
        get("/manager/api/systems/:sid/details/ptf/installed",
            asJson(withUserAndServer(this::installedPtfsForSystem)));
        get("/manager/api/systems/:sid/details/ptf/available",
            asJson(withUserAndServer(this::availablePtfsForSystem)));
        post("/manager/api/systems/:sid/details/ptf/scheduleAction",
            asJson(withUserAndServer(this::schedulePtfAction)));
    }

    private void initCoCoRoutes() {
        get("/manager/api/systems/:sid/details/coco/settings",
            asJson(withUserAndServer(this::getCoCoSettings)));
        post("/manager/api/systems/:sid/details/coco/settings",
            asJson(withUserAndServer(this::setCoCoSettings)));
        get("/manager/api/systems/:sid/details/coco/listAttestations",
            asJson(withUserAndServer(this::listAllAttestations)));
        post("/manager/api/systems/:sid/details/coco/scheduleAction",
            asJson(withUserAndServer(this::scheduleCoCoAttestation)));
        post("/manager/api/systems/coco/settings",
            asJson(withUser(this::setAllCoCoSettings)));
        post("/manager/api/systems/coco/scheduleAction",
            asJson(withUser(this::scheduleAllCoCoAttestation)));
    }

    private void initMigrationRoutes() {
        post("/manager/api/systems/migration/computeChannels",
            asJson(withUser(this::computeMigrationChannels)));
        post("/manager/api/systems/migration/schedule",
            asJson(withUser(this::scheduleMigration)));
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
                 throw new IllegalStateException("No action in schedule result");
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
     * API to schedule the execution of the upload support data action
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @param server the current server
     * @return json success or failure
     */
    private String uploadSupportData(Request request, Response response, User user, Server server) {
        var scheduleRequest = GSON.fromJson(request.body(), SupportDataRequest.class);

        String caseNumber = scheduleRequest.caseNumber();
        if (caseNumber == null || !caseNumber.matches("^\\d+$")) {
            badRequest(response, LOCAL.getMessage("system.details.support.invalid_case_number", caseNumber));
        }

        String region = scheduleRequest.region();
        if (Arrays.stream(UploadGeoType.values()).noneMatch(uploadGeo -> uploadGeo.getLabel().equals(region))) {
            badRequest(response, LOCAL.getMessage("system.details.support.invalid_region", region));
        }

        try {
            Action action = createSupportDataAction(user, server, scheduleRequest);
            taskomaticApi.scheduleActionExecution(action);

            return success(response, ResultJson.success(action.getId().intValue()));
        }
        catch (TaskomaticApiException ex) {
            LOG.error("Unable to schedule taskomatic execution for support data action", ex);
            HibernateFactory.rollbackTransaction();

            return internalServerError(response, LOCAL.getMessage("system.details.support.unable_to_schedule"));
        }
        catch (RuntimeException ex) {
            LOG.error("Unable to create and execute the support data action", ex);
            HibernateFactory.rollbackTransaction();

            return internalServerError(response, LOCAL.getMessage("system.details.support.unable_to_upload"));
        }
    }

    private static Action createSupportDataAction(User user, Server server, SupportDataRequest scheduleRequest) {
        return ActionManager.scheduleSupportDataAction(
            user,
            server.getId(),
            scheduleRequest.caseNumber(),
            scheduleRequest.parameters(),
            UploadGeoType.byLabel(scheduleRequest.region()),
            scheduleRequest.earliest()
        );
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
                return badRequest(response,
                        "Unable to schedule unknown action " + scheduleRequest.getActionType());
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
            ServerCoCoAttestationConfig updatedConfig = updateServerCoCoConfiguration(user, server, jsonConfig);

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

        long totalSize = attestationManager.countCoCoAttestationReportsForUserAndServer(user, server);

        List<CoCoAttestationReportJson> reportsJson =
            attestationManager.listCoCoAttestationReportsForUserAndServer(user, server, pc)
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

    private String setAllCoCoSettings(Request request, Response response, User user) {
        SystemsCoCoSettingsJson jsonConfig = GSON.fromJson(request.body(), SystemsCoCoSettingsJson.class);

        try {
            MinionServerFactory.lookupByIds(jsonConfig.getServerIds())
                .forEach(minionServer -> updateServerCoCoConfiguration(user, minionServer, jsonConfig));

            return json(GSON, response, ResultJson.success(jsonConfig,
                LOCAL.getMessage("system.audit.coco.configUpdated")), new TypeToken<>() { });
        }
        catch (RuntimeException ex) {
            return json(GSON, response, ResultJson.error(LOCAL.getMessage("system.audit.coco.configNotUpdated")),
                new TypeToken<>() { });
        }
    }

    private ServerCoCoAttestationConfig updateServerCoCoConfiguration(User user, Server server,
                                                                      CoCoSettingsJson jsonConfig) {
        return attestationManager.getConfig(user, server)
            .map(cfg -> {
                cfg.setEnabled(jsonConfig.isEnabled());
                cfg.setEnvironmentType(jsonConfig.getEnvironmentType());
                cfg.setAttestOnBoot(jsonConfig.isAttestOnBoot());

                attestationManager.saveConfig(user, cfg);

                return cfg;
            })
            .orElseGet(() -> attestationManager.createConfig(user, server,
                jsonConfig.getEnvironmentType(),
                jsonConfig.isEnabled(),
                jsonConfig.isAttestOnBoot()
            ));
    }

    private String scheduleAllCoCoAttestation(Request request, Response response, User user) {
        SystemScheduledRequestJson scheduleRequest = GSON.fromJson(request.body(), SystemScheduledRequestJson.class);
        Long result;

        Date earliestDate = MinionActionUtils.getScheduleDate(scheduleRequest.getEarliest());
        ActionChain chain = MinionActionUtils.getActionChain(scheduleRequest.getActionChain(), user);

        try {
            Set<MinionServer> minionsSet = MinionServerFactory.lookupByIds(scheduleRequest.getServerIds())
                .collect(Collectors.toSet());

            List<CoCoAttestationAction> scheduledActions =
                attestationManager.scheduleAttestationActionForSystems(user, minionsSet, earliestDate, chain);
            result = chain != null ? chain.getId() : scheduledActions.get(0).getId();
        }
        catch (TaskomaticApiException e) {
            LOG.error("Unable to schedule attestation action", e);
            return json(GSON, response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                ResultJson.error("Unable to schedule action"), new TypeToken<>() { });
        }

        return json(GSON, response, result.longValue());
    }


    private String computeMigrationChannels(Request request, Response response, User user) {
        MigrationChannelsRequest migrationChannelsRequest;

        try {
            migrationChannelsRequest = GSON.fromJson(request.body(), MigrationChannelsRequest.class);
        }
        catch (RuntimeException ex) {
            LOG.error("Unable to compute channels for the product migration", ex);
            return badRequest(response, ex.getMessage());
        }

        try {
            List<MinionServer> serverList = MinionServerFactory.lookupByIds(migrationChannelsRequest.serverIds())
                .toList();
            if (CollectionUtils.isEmpty(serverList)) {
                throw new IllegalArgumentException(LOCAL.getMessage("system.migration.noServersSelected"));
            }

            // Extract the common base product to use as source of the migration, if possible
            SUSEProductSet source = DistUpgradeManager.getCommonSourceProduct(serverList)
                .orElseThrow(() -> new IllegalArgumentException(LOCAL.getMessage("system.migration.noCommonProduct")));

            // Compute the targets from the common base, considering only the system with correct base installed
            SUSEProductSet target = DistUpgradeManager.getTargetProductSets(user, serverList, Optional.of(source))
                .stream()
                .filter(productSet -> migrationChannelsRequest.targetId().equals(productSet.getSerializedProductIDs()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(LOCAL.getMessage("system.migration.noTarget")));

            var channelsData = dataFactory.toMigrationChannelsSelection(serverList, user, target, source);
            return json(GSON, response, ResultJson.success(channelsData), new TypeToken<>() { });
        }
        catch (IllegalArgumentException ex) {
            LOG.error("Unable to compute migration channels", ex);
            return badRequest(response, ex.getMessage());
        }
        catch (RuntimeException ex) {
            LOG.error("Unable to compute migration channels", ex);
            return internalServerError(response, ex.getMessage());
        }
    }

    private String scheduleMigration(Request request, Response response, User user) {
        MigrationScheduleRequest scheduleRequest;

        try {
            scheduleRequest = GSON.fromJson(request.body(), MigrationScheduleRequest.class);
        }
        catch (RuntimeException ex) {
            LOG.error("Unable to schedule product migration action", ex);
            return badRequest(response, ex.getMessage());
        }

        try {
            List<Server> serverList = MinionServerFactory.lookupByIds(scheduleRequest.serverIds())
                .map(Server.class::cast)
                .toList();

            SUSEProductSet targetProductSet = dataFactory.toSUSEProductSet(scheduleRequest.targetProduct());
            List<Long> channelIds = dataFactory.toChannelIds(scheduleRequest.targetChannelTree());

            boolean dryRun = scheduleRequest.dryRun();
            boolean isPayg = cloudPaygManager.isPaygInstance();
            boolean allowVendorChange = scheduleRequest.allowVendorChange();

            Date earliestDate = scheduleRequest.getEarliestDate();
            ActionChain actionChain = scheduleRequest.getActionChain(user);

            List<DistUpgradeAction> scheduledActions = DistUpgradeManager.scheduleDistUpgrade(
                user, serverList, targetProductSet, channelIds,
                dryRun, allowVendorChange, isPayg,
                earliestDate, actionChain
            );

            Long result = actionChain != null ? actionChain.getId() : scheduledActions.get(0).getId();
            return success(response, ResultJson.success(result));
        }
        catch (RuntimeException | TaskomaticApiException | NoInstalledProductException | DistUpgradePaygException ex) {
            LOG.error("Unable to schedule product migration action", ex);
            return internalServerError(response, ex.getMessage());
        }
    }

}
