/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.webui.controllers.admin.handlers;

import static com.suse.manager.webui.utils.SparkApplicationHelper.badGateway;
import static com.suse.manager.webui.utils.SparkApplicationHelper.badRequest;
import static com.suse.manager.webui.utils.SparkApplicationHelper.internalServerError;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.notFound;
import static com.suse.manager.webui.utils.SparkApplicationHelper.serviceUnavailable;
import static com.suse.manager.webui.utils.SparkApplicationHelper.success;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withProductAdmin;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.credentials.HubSCCCredentials;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.hub.HubManager;
import com.suse.manager.hub.InvalidResponseException;
import com.suse.manager.hub.PeripheralRegistrationException;
import com.suse.manager.hub.migration.IssMigrator;
import com.suse.manager.hub.migration.IssMigratorFactory;
import com.suse.manager.model.hub.AccessTokenDTO;
import com.suse.manager.model.hub.IssAccessToken;
import com.suse.manager.model.hub.IssRole;
import com.suse.manager.model.hub.IssServer;
import com.suse.manager.model.hub.TokenType;
import com.suse.manager.model.hub.UpdatableServerData;
import com.suse.manager.model.hub.migration.MigrationResult;
import com.suse.manager.model.hub.migration.MigrationResultCode;
import com.suse.manager.model.hub.migration.SlaveMigrationData;
import com.suse.manager.webui.controllers.ECMAScriptDateAdapter;
import com.suse.manager.webui.controllers.admin.beans.ChannelSyncModel;
import com.suse.manager.webui.controllers.admin.beans.CreateTokenRequest;
import com.suse.manager.webui.controllers.admin.beans.HubRegisterRequest;
import com.suse.manager.webui.controllers.admin.beans.MigrationEntryDto;
import com.suse.manager.webui.controllers.admin.beans.PeripheralListData;
import com.suse.manager.webui.controllers.admin.beans.SyncChannelRequest;
import com.suse.manager.webui.controllers.admin.beans.UpdateRootCARequest;
import com.suse.manager.webui.controllers.admin.beans.ValidityRequest;
import com.suse.manager.webui.utils.FlashScopeHelper;
import com.suse.manager.webui.utils.PageControlHelper;
import com.suse.manager.webui.utils.gson.PagedDataResultJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.utils.token.TokenBuildingException;
import com.suse.manager.webui.utils.token.TokenException;
import com.suse.manager.webui.utils.token.TokenParsingException;
import com.suse.utils.CertificateUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.net.ssl.SSLException;

import spark.Request;
import spark.Response;

public class HubApiController {
    private static final Logger LOGGER = LogManager.getLogger(HubApiController.class);

    private static final LocalizationService LOC = LocalizationService.getInstance();

    private final HubManager hubManager;

    private final IssMigratorFactory migratorFactory;

    private final TaskomaticApi taskomaticApi;

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
        .serializeNulls()
        .create();

    /**
     * Default constructor
     */
    public HubApiController() {
        this(new HubManager(), new IssMigratorFactory(), new TaskomaticApi());
    }

    /**
     * Creates an instance with the given dependencies
     * @param hubManagerIn the hub manager
     * @param migratorFactoryIn the migrator factory
     * @param taskomaticApiIn the taskomatic API
     */
    public HubApiController(HubManager hubManagerIn, IssMigratorFactory migratorFactoryIn,
                            TaskomaticApi taskomaticApiIn) {
        this.hubManager = hubManagerIn;
        this.migratorFactory = migratorFactoryIn;
        this.taskomaticApi = taskomaticApiIn;
    }

    /**
     * initialize all the API Routes for the ISSv3 support
     */
    public void initRoutes() {
        get("/manager/api/admin/hub/peripherals", withProductAdmin(this::listPaginatedPeripherals));
        post("/manager/api/admin/hub/peripherals", withProductAdmin(this::registerPeripheral));
        delete("/manager/api/admin/hub/peripherals/:id", withProductAdmin(this::deletePeripheral));
        post("/manager/api/admin/hub/peripherals/:id/root-ca", withProductAdmin(this::updatePeripheralRootCA));
        delete("/manager/api/admin/hub/peripherals/:id/root-ca", withProductAdmin(this::removePeripheralRootCA));
        post("/manager/api/admin/hub/peripherals/:id/credentials", withProductAdmin(this::refreshCredentials));
        get("/manager/api/admin/hub/peripherals/:id/sync-channels",
                withProductAdmin(this::getPeripheralChannelSyncStatus));
        post("/manager/api/admin/hub/peripherals/:id/sync-channels",
                withProductAdmin(this::syncChannelsToPeripheral));
        delete("/manager/api/admin/hub/:id", withProductAdmin(this::deleteHub));
        post("/manager/api/admin/hub/:id/root-ca", withProductAdmin(this::updateHubRootCA));
        delete("/manager/api/admin/hub/:id/root-ca", withProductAdmin(this::removeHubRootCA));
        post("/manager/api/admin/hub/migrate/v1", withProductAdmin(this::migrateFromV1));
        post("/manager/api/admin/hub/migrate/v2", withProductAdmin(this::migrateFromV2));
        get("/manager/api/admin/hub/access-tokens", withProductAdmin(this::listTokens));
        post("/manager/api/admin/hub/access-tokens", withProductAdmin(this::createToken));
        post("/manager/api/admin/hub/access-tokens/:id/validity", withProductAdmin(this::setAccessTokenValidity));
        delete("/manager/api/admin/hub/access-tokens/:id", withProductAdmin(this::deleteAccessToken));
        post("/manager/api/admin/hub/sync-bunch", withProductAdmin(this::scheduleUpdateTask));
    }

    private String deleteHub(Request request, Response response, User user) {
        return deleteServer(request, response, user, IssRole.HUB);
    }

    private String deletePeripheral(Request request, Response response, User user) {
        return deleteServer(request, response, user, IssRole.PERIPHERAL);
    }

    private String updateHubRootCA(Request request, Response response, User user) {
        return updateServerRootCA(request, response, user, IssRole.HUB, true);
    }

    private String removeHubRootCA(Request request, Response response, User user) {
        return updateServerRootCA(request, response, user, IssRole.HUB, false);
    }

    private String updatePeripheralRootCA(Request request, Response response, User user) {
        return updateServerRootCA(request, response, user, IssRole.PERIPHERAL, true);
    }

    private String removePeripheralRootCA(Request request, Response response, User user) {
        return updateServerRootCA(request, response, user, IssRole.PERIPHERAL, false);
    }

    private String refreshCredentials(Request request, Response response, User user) {
        long peripheralId = Long.parseLong(request.params("id"));

        try {
            HubSCCCredentials newCredentials = hubManager.regenerateCredentials(user, peripheralId);
            return success(response, ResultJson.success(newCredentials.getUsername()));
        }
        catch (IllegalArgumentException ex) {
            LOGGER.warn("Unable to refresh credentials: {}", ex.getMessage());
            return notFound(response, LOC.getMessage("hub.cannot_find_server"));
        }
        catch (CertificateException ex) {
            LOGGER.error("Unexpected error while processing root certificate #{}", peripheralId, ex);
            return internalServerError(response, LOC.getMessage("hub.unexpected_error_root_ca"));
        }
        catch (IOException ex) {
            LOGGER.error("Error while attempting to connect to remote server #{}", peripheralId, ex);
            return internalServerError(response, LOC.getMessage("hub.error_connecting_remote"));
        }
    }

    private String getPeripheralChannelSyncStatus(Request request, Response response, User satAdmin) {
        long peripheralId = Long.parseLong(request.params("id"));
        try {
            ChannelSyncModel syncModel = hubManager.getChannelSyncModelForPeripheral(satAdmin, peripheralId);
            return json(response, GSON.toJson(syncModel));
        }
        catch (CertificateException eIn) {
            LOGGER.error("Unable to parse the specified root certificate for the peripheral {}", peripheralId, eIn);
            return badRequest(response, LOC.getMessage("hub.invalid_root_ca"));
        }
        catch (IOException eIn) {
            LOGGER.error("Error while attempting to connect to peripheral server {}", peripheralId, eIn);
            return internalServerError(response, LOC.getMessage("hub.error_connecting_remote"));
        }
    }

    /**
     * Unified method to sync and desync channels to/from a peripheral
     */
    private String syncChannelsToPeripheral(Request request, Response response, User satAdmin) {
        try {
            long peripheralId = Long.parseLong(request.params("id"));
            SyncChannelRequest syncRequest = GSON.fromJson(request.body(), SyncChannelRequest.class);
            if (syncRequest == null) {
                return badRequest(response, LOC.getMessage("hub.invalid_request"));
            }
            hubManager.syncChannelsByLabelForPeripheral(
                                satAdmin,
                                peripheralId,
                    syncRequest.getChannelsToAdd(),
                    syncRequest.getChannelsToRemove()
            );
            return success(response);
        }
        catch (NumberFormatException e) {
            LOGGER.error("Invalid peripheral id provided");
            return badRequest(response, LOC.getMessage("hub.invalid_id"));
        }
        catch (CertificateException e) {
            LOGGER.error("Unable to parse the specified root certificate for the peripheral {}",
                    request.params("id"), e);
            return badRequest(response, LOC.getMessage("hub.invalid_root_ca"));
        }
        catch (IOException e) {
            LOGGER.error("Error while attempting to connect to peripheral server {}",
                    request.params("id"), e);
            return internalServerError(response, LOC.getMessage("hub.error_connecting_remote"));
        }
    }

    private String registerPeripheral(Request request, Response response, User satAdmin) {
        HubRegisterRequest issRequest;

        try {
            issRequest = validateRegisterRequest(GSON.fromJson(request.body(), HubRegisterRequest.class));
        }
        catch (JsonSyntaxException ex) {
            LOGGER.error("Unable to parse JSON request {}", request, ex);
            return badRequest(response, LOC.getMessage("hub.invalid_request"));
        }

        String remoteServer = issRequest.getFqdn();

        try {
            String rootCA = issRequest.getRootCA();
            String token = issRequest.getToken();

            if (StringUtils.isNotEmpty(token)) {
                hubManager.register(satAdmin, remoteServer, token, rootCA);
            }
            else {
                String username = issRequest.getUsername();
                String password = issRequest.getPassword();

                hubManager.register(satAdmin, remoteServer, username, password, rootCA);
            }

            FlashScopeHelper.flash(request, LOC.getMessage("hub.register_peripheral_success", remoteServer));

            // Lookup the registered peripheral and return the id
            IssServer peripheral = hubManager.findServer(satAdmin, remoteServer, IssRole.PERIPHERAL);
            return success(response, ResultJson.success(peripheral.getId()));
        }
        catch (CertificateException ex) {
            LOGGER.error("Unable to parse the specified root certificate {}", issRequest.getRootCA(), ex);
            return badRequest(response, LOC.getMessage("hub.invalid_root_ca"));
        }
        catch (TaskomaticApiException ex) {
            LOGGER.error("Unexpected error while processing root certificate {}", remoteServer, ex);
            return internalServerError(response, LOC.getMessage("hub.unexpected_error_root_ca"));
        }
        catch (TokenBuildingException ex) {
            LOGGER.error("Unable issue a token for remote peripheral {}", remoteServer, ex);
            return internalServerError(response, LOC.getMessage("hub.unable_issue_token"));
        }
        catch (TokenParsingException ex) {
            LOGGER.error("Unable to parse the token received from peripheral {}", remoteServer, ex);
            return internalServerError(response, LOC.getMessage("hub.unable_parse_token"));
        }
        catch (SSLException ex) {
            LOGGER.error("Unable to establish a secure connection to {}", remoteServer, ex);
            return internalServerError(response, LOC.getMessage("hub.unable_establish_secure_connection"));
        }
        catch (InvalidResponseException ex) {
            LOGGER.error("Invalid response received from the remote server {}", remoteServer, ex);
            return internalServerError(response, LOC.getMessage("hub.invalid_remote_response"));
        }
        catch (PeripheralRegistrationException ex) {
            LOGGER.error("{} cannot be registered as a peripheral from this hub: {}",
                    remoteServer, ex.getMessage());
            return internalServerError(response, LOC.getMessage("hub.error_peripheral_not_registrable",
                    remoteServer));
        }
        catch (IOException ex) {
            LOGGER.error("Error while attempting to connect to remote server {}", remoteServer, ex);
            return internalServerError(response, LOC.getMessage("hub.error_connecting_remote"));
        }
        catch (RuntimeException ex) {
            LOGGER.error("Unexpected error while registering remote server {}", remoteServer, ex);
            return internalServerError(response, LOC.getMessage("hub.unexpected_error"));
        }
    }

    private String listPaginatedPeripherals(Request request, Response response, User satAdmin) {
        PageControlHelper pageHelper = new PageControlHelper(request);
        PageControl pc = pageHelper.getPageControl();
        long totalSize = hubManager.countRegisteredPeripherals(satAdmin, pc);

        List<PeripheralListData> peripherals = hubManager.listRegisteredPeripherals(satAdmin, pc).stream()
            .map(PeripheralListData::new)
            .toList();

        TypeToken<PagedDataResultJson<PeripheralListData, Long>> type = new TypeToken<>() { };
        return json(GSON, response, new PagedDataResultJson<>(peripherals, totalSize, Collections.emptySet()), type);
    }

    private String listTokens(Request request, Response response, User user) {
        PageControlHelper pageHelper = new PageControlHelper(request);
        PageControl pc = pageHelper.getPageControl();

        long totalSize = hubManager.countAccessToken(user);

        List<AccessTokenDTO> accessTokens = hubManager.listAccessToken(user, pc);
        TypeToken<PagedDataResultJson<AccessTokenDTO, Long>> type = new TypeToken<>() { };
        return json(GSON, response, new PagedDataResultJson<>(accessTokens, totalSize, Collections.emptySet()), type);
    }

    private String createToken(Request request, Response response, User user) {
        CreateTokenRequest tokenRequest;

        try {
            tokenRequest = validateCreationRequest(GSON.fromJson(request.body(), CreateTokenRequest.class));
        }
        catch (JsonSyntaxException ex) {
            LOGGER.error("Unable to parse JSON request", ex);
            return badRequest(response, LOC.getMessage("hub.invalid_request"));
        }

        switch (tokenRequest.getType()) {
            case ISSUED -> {
                try {
                    String serializedToken = hubManager.issueAccessToken(user, tokenRequest.getFqdn());
                    return success(response, ResultJson.success(serializedToken));
                }
                catch (TokenException | RuntimeException ex) {
                    LOGGER.error("Error while attempting to issue a token for {}", tokenRequest.getFqdn(), ex);
                    return internalServerError(response, "hub.unable_to_issue_token");
                }
            }
            case CONSUMED -> {
                try {
                    hubManager.storeAccessToken(user, tokenRequest.getFqdn(), tokenRequest.getToken());
                    return success(response, ResultJson.success(tokenRequest.getToken()));
                }
                catch (TokenParsingException ex) {
                    LOGGER.error("Unable to parse the token of request {}", tokenRequest, ex);
                    return badRequest(response, "hub.unable_to_parse_token");
                }
                catch (RuntimeException ex) {
                    LOGGER.error("Unable to process the store token request {}", tokenRequest, ex);
                    return internalServerError(response, "hub.unable_to_store_token");
                }
            }
            default -> {
                return badRequest(response, "hub.invalid_request");
            }
        }
    }

    private String setAccessTokenValidity(Request request, Response response, User user) {
        ValidityRequest validityRequest = GSON.fromJson(request.body(), ValidityRequest.class);
        long tokenId = Long.parseLong(request.params("id"));

        IssAccessToken issAccessToken = hubManager.lookupAccessTokenById(user, tokenId).orElse(null);
        if (issAccessToken == null) {
            return notFound(response, LOC.getMessage("hub.invalid_token_id"));
        }

        if (issAccessToken.isValid() == validityRequest.isValid()) {
            return badRequest(response, LOC.getMessage("hub.invalid_token_state"));
        }

        issAccessToken.setValid(validityRequest.isValid());
        hubManager.updateToken(user, issAccessToken);

        return success(response, ResultJson.success(issAccessToken.getModified()));
    }

    private String deleteAccessToken(Request request, Response response, User user) {
        long tokenId = Long.parseLong(request.params("id"));

        boolean result = hubManager.deleteAccessToken(user, tokenId);
        if (!result) {
            return badRequest(response, LOC.getMessage("hub.unable_delete_token"));
        }

        return success(response);
    }

    private String deleteServer(Request request, Response response, User user, IssRole issRole) {
        long serverId = Long.parseLong(request.params("id"));
        boolean onlyLocal = Boolean.parseBoolean(request.queryParams("only_local"));
        IssServer server = hubManager.findServer(user, serverId, issRole);
        if (server == null) {
            return notFound(response, LOC.getMessage("hub.cannot_find_server"));
        }
        try {
            hubManager.deregister(user, server.getFqdn(), issRole, onlyLocal);
        }
        catch (IOException ex) {
            LOGGER.error("Unable to deregister: error to connect with the remote server {}", server.getFqdn(), ex);
            return serviceUnavailable(response, LOC.getMessage("hub.unable_to_deregister"));
        }
        catch (CertificateException ex) {
            LOGGER.error("Unable to deregister: error with Certificate when connecting to the remote server {}",
                    server.getFqdn(), ex);
            return badGateway(response, LOC.getMessage("hub.unable_to_deregister"));
        }
        return success(response);
    }

    private String updateServerRootCA(Request request, Response response, User user, IssRole role, boolean isUpdate) {
        String rootCA;

        if (isUpdate) {
            // If we are performing an update we need to parse the request
            try {
                UpdateRootCARequest updateRequest = GSON.fromJson(request.body(), UpdateRootCARequest.class);
                rootCA = validateUpdateRootCARequest(updateRequest).getRootCA();
            }
            catch (CertificateException ex) {
                LOGGER.error("Unable to parse the specified certificate", ex);
                return badRequest(response, LOC.getMessage("hub.invalid_root_ca"));
            }
            catch (JsonSyntaxException ex) {
                LOGGER.error("Unable to parse JSON request", ex);
                return badRequest(response, LOC.getMessage("hub.invalid_request"));
            }
        }
        else {
            // If it's a deletion, just set the value to null
            rootCA = null;
        }

        long serverId = Long.parseLong(request.params("id"));
        IssServer server = hubManager.findServer(user, serverId, role);
        if (server == null) {
            return notFound(response, LOC.getMessage("hub.cannot_find_server"));
        }

        try {
            // Collections.singletonMap() is used in place of Map.of() because it allows null as value
            UpdatableServerData data = new UpdatableServerData(Collections.singletonMap("root_ca", rootCA));
            hubManager.updateServerData(user, server.getFqdn(), role, data);
        }
        catch (TaskomaticApiException e) {
            return internalServerError(response, LOC.getMessage("hub.cannot_refresh_certificate"));
        }

        return success(response);
    }

    private String migrateFromV1(Request request, Response response, User user) {
        List<MigrationEntryDto> migrationEntries = GSON.fromJson(request.body(),
            new TypeToken<List<MigrationEntryDto>>() { }.getType());

        // Map the entries to a map of SlaveMigrationData
        Map<String, SlaveMigrationData> migrationData = migrationEntries.stream()
            .filter(entry -> entry.isSelected() && !entry.isDisabled())
            .map(entry -> new SlaveMigrationData(entry.getFqdn(), entry.getAccessToken(), entry.getRootCA()))
            .collect(Collectors.toMap(SlaveMigrationData::fqdn, Function.identity()));

        // Run migration from v1
        IssMigrator migrator = migratorFactory.createFor(user);
        return performMigration(request, response, migrationData, 1, migrator::migrateFromV1);
    }

    private String migrateFromV2(Request request, Response response, User user) {
        List<MigrationEntryDto> migrationEntries = GSON.fromJson(request.body(),
            new TypeToken<List<MigrationEntryDto>>() { }.getType());

        // Map the entries to a list of SlaveMigrationData
        List<SlaveMigrationData> migrationData = migrationEntries.stream()
            .filter(entry -> entry.isSelected() && !entry.isDisabled())
            .map(entry -> new SlaveMigrationData(entry.getFqdn(), entry.getAccessToken(), entry.getRootCA()))
            .toList();

        // Run migration from v2
        IssMigrator migrator = migratorFactory.createFor(user);
        return performMigration(request, response, migrationData, 2, migrator::migrateFromV2);
    }

    private <T> String performMigration(Request request, Response response, T migrationData, int version,
                                        Function<T, MigrationResult> migration) {
        try {
            MigrationResult result = migration.apply(migrationData);

            if (result.getResultCode() == MigrationResultCode.SUCCESS) {
                FlashScopeHelper.flash(request, LOC.getMessage("hub.migration_success", version));
            }

            return success(response, result);
        }
        catch (Exception ex) {
            LOGGER.error("Unexpected error while migrating the servers", ex);
            return internalServerError(response, LOC.getMessage("hub.unexpected_error_migrating", ex.getMessage()));
        }
    }

    private String scheduleUpdateTask(Request request, Response response, User user) {
        try {
            Map<String, String> params = Map.of("noRepoSync", "false");
            taskomaticApi.scheduleSingleSatBunch(user, "mgr-sync-refresh-bunch", params);
            return success(response);
        }
        catch (TaskomaticApiException ex) {
            LOGGER.error("Failed to schedule mgr-sync-refresh job: {}", ex.getMessage());
            return internalServerError(response, LOC.getMessage("hub.unexpected_error_migrating", ex.getMessage()));
        }
    }

    private static HubRegisterRequest validateRegisterRequest(HubRegisterRequest parsedRequest) {
        if (StringUtils.isEmpty(parsedRequest.getFqdn())) {
            throw new JsonSyntaxException("Missing required server FQDN in the request");
        }

        boolean missingToken = parsedRequest.getToken() == null;
        boolean missingUserPassword = parsedRequest.getUsername() == null || parsedRequest.getPassword() == null;
        if (missingToken && missingUserPassword) {
            throw new JsonSyntaxException("Either token or username/password must be provided");
        }

        return parsedRequest;
    }

    private static CreateTokenRequest validateCreationRequest(CreateTokenRequest request) {
        if (request == null) {
            throw new JsonSyntaxException("Request is empty");
        }

        if (StringUtils.isEmpty(request.getFqdn()) || request.getType() == null) {
            throw new JsonSyntaxException("Missing required field");
        }

        // Check if the token field is consistent with the type requested
        boolean tokenPresent = StringUtils.isNotEmpty(request.getToken());

        if (request.getType() == TokenType.ISSUED && tokenPresent) {
            throw new JsonSyntaxException("Token must be null when creating an ISSUED token");
        }

        if (request.getType() == TokenType.CONSUMED && !tokenPresent) {
            throw new JsonSyntaxException("Token is required when creating a CONSUMED token");
        }

        return request;
    }

    private UpdateRootCARequest validateUpdateRootCARequest(UpdateRootCARequest request) throws CertificateException {
        if (request == null) {
            throw new JsonSyntaxException("Request is empty");
        }

        CertificateUtils.parse(request.getRootCA())
            .orElseThrow(() -> new JsonSyntaxException("rootCA is empty"));

        return request;
    }
}
