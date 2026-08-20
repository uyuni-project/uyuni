/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.xmlrpc.iss;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;
import com.suse.manager.model.hub.ChannelInfoJson;
import com.suse.manager.model.hub.ManagerInfoJson;
import com.suse.manager.model.hub.OrgInfoJson;
import com.suse.manager.model.hub.migration.MigrationResult;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link HubHandler}.
 */
@Tag(name = "sync.hub", description = "Contains methods to set up and manage Hub Inter-Server synchronization")
public interface HubHandlerApi {

    /**
     * Generates a new access token for ISS.
     *
     * @param loggedInUser the current user
     * @param fqdn the FQDN of the peripheral or hub that will use the token
     * @return the serialized form of the token
     */
    @ApiEndpointDoc(
        summary = "Generate a new access token for ISS for accessing this system",
        requestClass = GenerateAccessTokenRequest.class,
        responseClass = TokenResponse.class,
        legacyDocResponse = @LegacyDocResponse(type = "string", name = "The serialized form of the token")
    )
    String generateAccessToken(User loggedInUser, String fqdn);

    /**
     * Stores an access token issued by a remote server.
     *
     * @param loggedInUser the current user
     * @param fqdn the FQDN of the peripheral or hub that generated the token
     * @param token the access token
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Generate a new access token for ISS for accessing this system",
        requestClass = StoreAccessTokenRequest.class,
        isIntegerResponse = true
    )
    int storeAccessToken(User loggedInUser, String fqdn, String token);

    /**
     * Replaces the authentication tokens between this hub and a peripheral server.
     *
     * @param loggedInUser the current user
     * @param fqdn the FQDN of the remote peripheral server
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Replace the auth tokens for connections between this hub and the given peripheral server",
        requestClass = ReplaceTokensRequest.class,
        isIntegerResponse = true
    )
    int replaceTokens(User loggedInUser, String fqdn);

    /**
     * Registers a remote server with the specified ISS role.
     *
     * @param loggedInUser the current user
     * @param fqdn the FQDN of the remote server to register
     * @param username the name of the user on the remote server
     * @param password the password of the user on the remote server
     * @param rootCA the root CA certificate
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Registers automatically a remote server with the specified ISS role.",
        requestClass = RegisterPeripheralRequest.class,
        isIntegerResponse = true
    )
    int registerPeripheral(User loggedInUser, String fqdn, String username, String password, String rootCA);

    /**
     * Registers a remote server with the specified ISS role using an existing access token.
     *
     * @param loggedInUser the current user
     * @param fqdn the FQDN of the remote server to register
     * @param token the token used to authenticate on the remote server
     * @param rootCA the root CA certificate
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Registers a remote server with the specified ISS role using an existing specified access token.",
        requestClass = RegisterPeripheralWithTokenRequest.class,
        isIntegerResponse = true
    )
    int registerPeripheralWithToken(User loggedInUser, String fqdn, String token, String rootCA);

    /**
     * De-registers the server identified by the given FQDN.
     *
     * @param loggedInUser the current user
     * @param fqdn the FQDN of the remote server to de-register
     * @param onlyLocal whether the de-registration is performed on this server only
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "De-register the server identified by the fqdn.",
        requestClass = DeregisterRequest.class,
        isIntegerResponse = true
    )
    int deregister(User loggedInUser, String fqdn, boolean onlyLocal);

    /**
     * Sets the details of a hub or peripheral server.
     *
     * @param loggedInUser the current user
     * @param fqdn the FQDN of the hub or peripheral server
     * @param role the role which should be updated
     * @param data the new data
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Set server details. All arguments are optional and will only be modified\nif included in the " +
                "struct.",
        requestClass = SetDetailsRequest.class,
        isIntegerResponse = true
    )
    int setDetails(User loggedInUser, String fqdn, String role, Map<String, String> data);

    /**
     * Collects data about this Manager server.
     *
     * @param loggedInUser the current user
     * @return the manager information
     */
    @ApiEndpointDoc(
        summary = "Get manager info.",
        method = HttpMethod.get,
        responseClass = ManagerInfoResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "manager info")
    )
    ManagerInfoJson getManagerInfo(@Parameter(hidden = true) User loggedInUser);

    /**
     * Remotely collects data about the organizations of a peripheral server.
     *
     * @param loggedInUser the current user
     * @param fqdn the FQDN identifying the peripheral server
     * @return the peripheral organizations
     */
    @ApiEndpointDoc(
        summary = "Remotely collect data about peripheral organizations",
        method = HttpMethod.get,
        responseClass = OrgInfoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "org info")
    )
    List<OrgInfoJson> getAllPeripheralOrgs(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "fqdn", description = "The FQDN identifying the peripheral server",
                in = ParameterIn.QUERY, required = true) String fqdn);

    /**
     * Remotely collects data about the channels of a peripheral server.
     *
     * @param loggedInUser the current user
     * @param fqdn the FQDN identifying the peripheral server
     * @return the peripheral channels
     */
    @ApiEndpointDoc(
        summary = "Remotely collect data about peripheral channels",
        method = HttpMethod.get,
        responseClass = ChannelInfoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "channel info")
    )
    List<ChannelInfoJson> getAllPeripheralChannels(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "fqdn", description = "The FQDN identifying the peripheral server",
                in = ParameterIn.QUERY, required = true) String fqdn);

    /**
     * Lists all peripheral servers.
     *
     * @param loggedInUser the current user
     * @return the peripheral servers
     */
    @ApiEndpointDoc(
        summary = "Lists all peripheral servers.",
        method = HttpMethod.get,
        responseClass = PeripheralServerListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "peripheral_server")
    )
    List<Map<String, Object>> listPeripheralServers(@Parameter(hidden = true) User loggedInUser);

    /**
     * Adds peripheral channels to synchronize on a peripheral server.
     *
     * @param loggedInUser the current user
     * @param fqdn the FQDN identifying the peripheral server
     * @param channelLabels the labels of the channels to add
     * @param peripheralOrgIdWhenCustomChannel the peripheral org to set in custom channels
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Add peripheral channels to synchronize on a peripheral server, forcing the peripheral org in " +
                "custom channels",
        requestClass = AddPeripheralChannelsRequest.class,
        isIntegerResponse = true
    )
    int addPeripheralChannelsToSync(User loggedInUser, String fqdn, List<String> channelLabels,
                                    Integer peripheralOrgIdWhenCustomChannel);

    /**
     * Removes peripheral channels to synchronize on a peripheral server.
     *
     * @param loggedInUser the current user
     * @param fqdn the FQDN identifying the peripheral server
     * @param channelLabels the labels of the channels to remove
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Remove peripheral channels to synchronize on a peripheral server",
        requestClass = RemovePeripheralChannelsRequest.class,
        isIntegerResponse = true
    )
    int removePeripheralChannelsToSync(User loggedInUser, String fqdn, List<String> channelLabels);

    /**
     * Lists the peripheral channels currently set to synchronize on a peripheral server.
     *
     * @param loggedInUser the current user
     * @param fqdn the FQDN identifying the peripheral server
     * @return the channel labels
     */
    @ApiEndpointDoc(
        summary = "Lists current peripheral channel to synchronize on a peripheral server",
        method = HttpMethod.get,
        responseClass = ChannelLabelListResponse.class,
        responseDescription = "Label of a peripheral channel to sync"
    )
    List<String> listPeripheralChannelsToSync(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "fqdn", description = "The FQDN identifying the peripheral server",
                in = ParameterIn.QUERY, required = true) String fqdn);

    /**
     * Synchronizes the peripheral channels of a peripheral server.
     *
     * @param loggedInUser the current user
     * @param fqdn the FQDN identifying the peripheral server
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Synchronize peripheral channels on a peripheral server",
        requestClass = PeripheralServerRequest.class,
        isIntegerResponse = true
    )
    int syncPeripheralChannels(User loggedInUser, String fqdn);

    /**
     * Regenerates the credentials of an existing peripheral.
     *
     * @param loggedInUser the current user
     * @param fqdn the FQDN identifying the peripheral server
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Regenerate the username and the password for an existing peripheral.",
        requestClass = PeripheralServerRequest.class,
        isIntegerResponse = true
    )
    int regenerateSCCCredentials(User loggedInUser, String fqdn);

    /**
     * Migrates the existing ISSv1 slaves to Hub Online Synchronization peripherals.
     *
     * @param loggedInUser the current user
     * @param migrationData the peripheral migration data
     * @return the migration result
     */
    @ApiEndpointDoc(
        summary = "Migrate the existing ISSv1 slaves to Hub Online Synchronization peripherals.",
        requestClass = MigrateFromISSv1Request.class,
        responseClass = MigrationResultResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "result")
    )
    MigrationResult migrateFromISSv1(User loggedInUser, List<Map<String, String>> migrationData);

    /**
     * Migrates the existing ISSv2 peripherals to Hub Online Synchronization peripherals.
     *
     * @param loggedInUser the current user
     * @param migrationData the peripheral migration data
     * @return the migration result
     */
    @ApiEndpointDoc(
        summary = "Migrate the existing ISSv2 peripherals to Hub Online Synchronization peripherals.",
        requestClass = MigrateFromISSv2Request.class,
        responseClass = MigrationResultResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "result")
    )
    MigrationResult migrateFromISSv2(User loggedInUser, List<Map<String, String>> migrationData);

    /**
     * Checks whether this server is configured as a peripheral server.
     *
     * @param loggedInUser the current user
     * @return true if this is an ISS peripheral
     */
    @ApiEndpointDoc(
        summary = "Check if this server is configured as peripheral server and read data from a Hub",
        method = HttpMethod.get,
        responseClass = BooleanResponse.class,
        responseDescription = "True if this is an ISS peripheral, false otherwise",
        legacyDocResponse = @LegacyDocResponse(name = "peripheral")
    )
    boolean isISSPeripheral(@Parameter(hidden = true) User loggedInUser);

    /**
     * Schedules an mgr-sync refresh with reposync on a peripheral server.
     *
     * @param loggedInUser the current user
     * @param earliest the earliest time the task will be executed
     * @param withReposync whether reposync runs after the mgr-sync refresh
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Schedules mgr-sync refresh with reposync on peripheral server",
        requestClass = ScheduleUpdateTaskRequest.class,
        isIntegerResponse = true
    )
    int scheduleUpdateTask(User loggedInUser, Date earliest, boolean withReposync);

    @Schema(name = "HubGenerateAccessTokenRequest")
    interface GenerateAccessTokenRequest {

        /**
         * @return the FQDN of the peripheral or hub that will use the token
         */
        @Schema(description = "FQDN of the peripheral/hub that will be using this access token",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getFqdn();
    }

    @Schema(name = "HubStoreAccessTokenRequest")
    @JsonPropertyOrder({"fqdn", "token"})
    interface StoreAccessTokenRequest {

        /**
         * @return the FQDN of the peripheral or hub that generated the token
         */
        @Schema(description = "the FQDN of the peripheral/hub that generated this access token",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getFqdn();

        /**
         * @return the access token
         */
        @Schema(description = "the access token", requiredMode = Schema.RequiredMode.REQUIRED)
        String getToken();
    }

    @Schema(name = "HubReplaceTokensRequest")
    interface ReplaceTokensRequest {

        /**
         * @return the FQDN of the remote peripheral server
         */
        @Schema(description = "the FQDN of the remote peripheral server to replace the tokens",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getFqdn();
    }

    @Schema(name = "HubRegisterPeripheralRequest")
    @JsonPropertyOrder({"fqdn", "username", "password", "rootCA"})
    interface RegisterPeripheralRequest {

        /**
         * @return the FQDN of the remote server to register
         */
        @Schema(description = "the FQDN of the remote server to register",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getFqdn();

        /**
         * @return the name of the user on the remote server
         */
        @Schema(description = "the name of the user, needed to access the remote server It must have the sat " +
                "admin role", requiredMode = Schema.RequiredMode.REQUIRED)
        String getUsername();

        /**
         * @return the password of the user on the remote server
         */
        @Schema(description = "the password of the user, needed to access the remote server",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getPassword();

        /**
         * @return the root CA certificate
         */
        @Schema(description = "the root CA certificate, in case it's needed to establish a secure connection")
        String getRootCA();
    }

    @Schema(name = "HubRegisterPeripheralWithTokenRequest")
    @JsonPropertyOrder({"fqdn", "token", "rootCA"})
    interface RegisterPeripheralWithTokenRequest {

        /**
         * @return the FQDN of the remote server to register
         */
        @Schema(description = "the FQDN of the remote server to register",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getFqdn();

        /**
         * @return the token used to authenticate on the remote server
         */
        @Schema(description = "the token used to authenticate on the remote server.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getToken();

        /**
         * @return the root CA certificate
         */
        @Schema(description = "the root CA certificate, in case it's needed to establish a secure connection")
        String getRootCA();
    }

    @Schema(name = "HubDeregisterRequest")
    @JsonPropertyOrder({"fqdn", "onlyLocal"})
    interface DeregisterRequest {

        /**
         * @return the FQDN of the remote server to de-register
         */
        @Schema(description = "the FQDN of the remote server to de-register",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getFqdn();

        /**
         * @return whether the de-registration is performed on this server only
         */
        @Schema(description = "true if the de - registration has to be performed only this server, false to " +
                "instead fully deregister on both sides")
        Boolean getOnlyLocal();
    }

    @Schema(name = "HubServerDetails")
    @JsonPropertyOrder({"rootCa", "gpgKey"})
    interface ServerDetailsDoc {

        /**
         * @return the root ca
         */
        @Schema(name = "root_ca", description = "The root ca")
        String getRootCa();

        /**
         * @return the root gpg key
         */
        @Schema(name = "gpg_key", description = "The root gpg key - only for role HUB")
        String getGpgKey();
    }

    @Schema(name = "HubSetDetailsRequest")
    @JsonPropertyOrder({"fqdn", "role", "data"})
    interface SetDetailsRequest {

        /**
         * @return the FQDN of the hub or peripheral server
         */
        @Schema(description = "The FQDN of Hub or Peripheral server to lookup details for.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getFqdn();

        /**
         * @return the role which should be updated
         */
        @Schema(description = "The role which should be updated. Either 'HUB' or 'PERIPHERAL'.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getRole();

        /**
         * @return the new data
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "data")
        ServerDetailsDoc getData();
    }

    @Schema(name = "HubPeripheralServerRequest")
    interface PeripheralServerRequest {

        /**
         * @return the FQDN identifying the peripheral server
         */
        @Schema(description = "The FQDN identifying the peripheral server",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getFqdn();
    }

    @Schema(name = "HubAddPeripheralChannelsRequest")
    @JsonPropertyOrder({"fqdn", "channelLabels", "peripheralOrgIdWhenCustomChannel"})
    interface AddPeripheralChannelsRequest {

        /**
         * @return the FQDN identifying the peripheral server
         */
        @Schema(description = "The FQDN identifying the peripheral server",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getFqdn();

        /**
         * @return the labels of the channels to add
         */
        @Schema(description = "The channel label", requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getChannelLabels();

        /**
         * @return the peripheral org to set in custom channels
         */
        @Schema(description = "ID of the peripheral Org to be set in custom channels")
        Integer getPeripheralOrgIdWhenCustomChannel();
    }

    @Schema(name = "HubRemovePeripheralChannelsRequest")
    @JsonPropertyOrder({"fqdn", "channelLabels"})
    interface RemovePeripheralChannelsRequest {

        /**
         * @return the FQDN identifying the peripheral server
         */
        @Schema(description = "The FQDN identifying the peripheral server",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getFqdn();

        /**
         * @return the labels of the channels to remove
         */
        @Schema(description = "The channel label", requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getChannelLabels();
    }

    @Schema(name = "HubMigrationData")
    @JsonPropertyOrder({"fqdn", "token", "rootCa"})
    interface MigrationDataDoc {

        /**
         * @return the fully qualified domain name of the remote server
         */
        @Schema(description = "The fully qualified domain name of the remote slave server.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getFqdn();

        /**
         * @return the token used to authenticate on the remote server
         */
        @Schema(description = "The token used to authenticate on the remote server.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getToken();

        /**
         * @return the root ca of the remote server
         */
        @Schema(name = "root_ca",
                description = "The root ca needed to establish a secure connection to the remote server.")
        String getRootCa();
    }

    @Schema(name = "HubMigrateFromISSv1Request")
    interface MigrateFromISSv1Request {

        /**
         * @return the peripheral migration data
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "migration data")
        List<MigrationDataDoc> getMigrationData();
    }

    @Schema(name = "HubMigrationDataV2")
    @JsonPropertyOrder({"fqdn", "token", "rootCa"})
    interface MigrationDataV2Doc {

        /**
         * @return the fully qualified domain name of the remote server
         */
        @Schema(description = "The fully qualified domain name of the remote peripheral server.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getFqdn();

        /**
         * @return the token used to authenticate on the remote server
         */
        @Schema(description = "The token used to authenticate on the remote server.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getToken();

        /**
         * @return the root ca of the remote server
         */
        @Schema(name = "root_ca",
                description = "The root ca needed to establish a secure connection to the remote server.")
        String getRootCa();
    }

    @Schema(name = "HubMigrateFromISSv2Request")
    interface MigrateFromISSv2Request {

        /**
         * @return the peripheral migration data
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "migration data")
        List<MigrationDataV2Doc> getMigrationData();
    }

    @Schema(name = "HubScheduleUpdateTaskRequest")
    @JsonPropertyOrder({"earliest", "withReposync"})
    interface ScheduleUpdateTaskRequest {

        /**
         * @return the earliest time the task will be executed
         */
        @Schema(description = "earliest time the task will be executed.")
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getEarliest();

        /**
         * @return whether reposync runs after the mgr-sync refresh
         */
        @Schema(description = "if true reposync will be run after mgr-sync refresh")
        Boolean getWithReposync();
    }

    @Schema(name = "HubManagerInfo")
    @JsonPropertyOrder({"version", "reportDb", "reportDbName", "reportDbHost", "reportDbPort"})
    interface ManagerInfoDoc {

        /**
         * @return the version
         */
        @Schema(description = "version", requiredMode = Schema.RequiredMode.REQUIRED)
        String getVersion();

        /**
         * @return whether there is a report database
         */
        @Schema(name = "report_db", description = "true if there is a report database",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean getReportDb();

        /**
         * @return the name of the report database
         */
        @Schema(name = "report_db_name", description = "name of the report database",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getReportDbName();

        /**
         * @return the hostname of the report database
         */
        @Schema(name = "report_db_host", description = "hostname of the report database",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getReportDbHost();

        /**
         * @return the port of the report database
         */
        @Schema(name = "report_db_port", description = "port of the report database",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getReportDbPort();
    }

    @Schema(name = "HubOrgInfo")
    @JsonPropertyOrder({"orgId", "orgName"})
    interface OrgInfoDoc {

        /**
         * @return the org identifier
         */
        @Schema(name = "org_id", description = "org identifier", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "long")
        Long getOrgId();

        /**
         * @return the org name
         */
        @Schema(name = "org_name", description = "org name", requiredMode = Schema.RequiredMode.REQUIRED)
        String getOrgName();
    }

    @Schema(name = "HubChannelInfo")
    @JsonPropertyOrder({"id", "name", "label", "summary", "orgId", "parentChannelId"})
    interface ChannelInfoDoc {

        /**
         * @return the id of the channel
         */
        @Schema(description = "the id of the channel", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "long")
        Long getId();

        /**
         * @return the name of the channel
         */
        @Schema(description = "the name of the channel", requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the label of the channel
         */
        @Schema(description = "the label of the channel", requiredMode = Schema.RequiredMode.REQUIRED)
        String getLabel();

        /**
         * @return the summary of the channel
         */
        @Schema(description = "the summary of the channel", requiredMode = Schema.RequiredMode.REQUIRED)
        String getSummary();

        /**
         * @return the organization id of the channel
         */
        @Schema(name = "org_id", description = "the organization id of the channel",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @LegacyDocResponse(type = "long")
        Long getOrgId();

        /**
         * @return the parent channel id of the channel
         */
        @Schema(name = "parent_channel_id", description = "the parent channel ID of the channel",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @LegacyDocResponse(type = "long")
        Long getParentChannelId();
    }

    @Schema(name = "HubPeripheralServer")
    @JsonPropertyOrder({"fqdn", "id", "rootCa"})
    interface PeripheralServerDoc {

        /**
         * @return the FQDN of the peripheral server
         */
        @Schema(description = "The FQDN of the peripheral server", requiredMode = Schema.RequiredMode.REQUIRED)
        String getFqdn();

        /**
         * @return the system id of the peripheral server
         */
        @Schema(description = "The system ID of the peripheral server", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the root CA certificate of the peripheral server
         */
        @Schema(name = "root_ca", description = "The root CA certificate of the peripheral server",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String getRootCa();
    }

    @Schema(name = "HubMigrationMessage")
    @JsonPropertyOrder({"severity", "message"})
    interface MigrationMessageDoc {

        /**
         * @return the severity of the message
         */
        @Schema(description = "the severity of the message", requiredMode = Schema.RequiredMode.REQUIRED)
        String getSeverity();

        /**
         * @return the message
         */
        @Schema(description = "the message", requiredMode = Schema.RequiredMode.REQUIRED)
        String getMessage();
    }

    @Schema(name = "HubMigrationResult")
    @JsonPropertyOrder({"messages", "resultCode"})
    interface MigrationResultDoc {

        /**
         * @return the migration messages
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(name = "message")
        List<MigrationMessageDoc> getMessages();

        /**
         * @return the result code
         */
        @Schema(name = "result_code", requiredMode = Schema.RequiredMode.REQUIRED)
        String getResultCode();
    }

    @Schema(name = "ApiResponseString")
    interface TokenResponse extends ApiResponseWrapper<String> { }

    @Schema(name = "ApiResponseBoolean")
    interface BooleanResponse extends ApiResponseWrapper<Boolean> { }

    @Schema(name = "ApiResponseHubManagerInfo")
    interface ManagerInfoResponse extends ApiResponseWrapper<ManagerInfoDoc> { }

    @Schema(name = "ApiResponseHubOrgInfoList")
    interface OrgInfoListResponse extends ApiResponseWrapper<List<OrgInfoDoc>> { }

    @Schema(name = "ApiResponseHubChannelInfoList")
    interface ChannelInfoListResponse extends ApiResponseWrapper<List<ChannelInfoDoc>> { }

    @Schema(name = "ApiResponseHubPeripheralServerList")
    interface PeripheralServerListResponse extends ApiResponseWrapper<List<PeripheralServerDoc>> { }

    @Schema(name = "ApiResponseHubChannelLabelList")
    interface ChannelLabelListResponse extends ApiResponseWrapper<List<String>> { }

    @Schema(name = "ApiResponseHubMigrationResult")
    interface MigrationResultResponse extends ApiResponseWrapper<MigrationResultDoc> { }
}
