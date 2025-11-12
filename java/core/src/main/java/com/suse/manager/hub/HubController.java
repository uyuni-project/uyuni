/*
 * Copyright (c) 2024--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.manager.hub;

import static com.suse.manager.hub.HubSparkHelper.onlyFromHub;
import static com.suse.manager.hub.HubSparkHelper.onlyFromRegistered;
import static com.suse.manager.hub.HubSparkHelper.onlyFromUnregistered;
import static com.suse.manager.hub.HubSparkHelper.usingTokenAuthentication;
import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.badRequest;
import static com.suse.manager.webui.utils.SparkApplicationHelper.internalServerError;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.message;
import static com.suse.manager.webui.utils.SparkApplicationHelper.success;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.ConnectionManager;
import com.redhat.rhn.common.hibernate.ConnectionManagerFactory;
import com.redhat.rhn.common.hibernate.ReportDbHibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.notification.types.HubRegistrationChanged;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.taskomatic.task.ReportDBHelper;

import com.suse.manager.model.hub.ChannelInfoDetailsJson;
import com.suse.manager.model.hub.ChannelInfoJson;
import com.suse.manager.model.hub.IssAccessToken;
import com.suse.manager.model.hub.IssRole;
import com.suse.manager.model.hub.ManagerInfoJson;
import com.suse.manager.model.hub.OrgInfoJson;
import com.suse.manager.model.hub.RegisterJson;
import com.suse.manager.model.hub.SCCCredentialsJson;
import com.suse.manager.model.hub.ServerInfoJson;
import com.suse.manager.model.hub.UpdatableServerData;
import com.suse.manager.webui.controllers.ECMAScriptDateAdapter;
import com.suse.manager.webui.utils.token.TokenBuildingException;
import com.suse.manager.webui.utils.token.TokenParsingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

import spark.Request;
import spark.Response;

public class HubController {

    private static final Logger LOGGER = LogManager.getLogger(HubController.class);

    private final HubManager hubManager;

    private final TaskomaticApi taskomaticApi;

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
        .serializeNulls()
        .create();

    /**
     * Default constructor
     */
    public HubController() {
        this(new HubManager(), new TaskomaticApi());
    }

    /**
     * Builds an instance with the specified hub manager
     * @param hubManagerIn the hub manager
     * @param taskomaticApiIn the taskomatic api
     */
    public HubController(HubManager hubManagerIn, TaskomaticApi taskomaticApiIn) {
        this.hubManager = hubManagerIn;
        this.taskomaticApi = taskomaticApiIn;
    }

    /**
     * Initialize the API routes
     */
    public void initRoutes() {
        post("/hub/ping", asJson(usingTokenAuthentication(this::ping)));
        post("/hub/sync/deregister", asJson(usingTokenAuthentication(onlyFromRegistered(this::deregister))));
        post("/hub/sync/registerHub", asJson(usingTokenAuthentication(onlyFromUnregistered(this::registerHub))));
        get("/hub/serverInfo", asJson(usingTokenAuthentication(this::getServerInfo)));
        post("/hub/sync/replaceTokens", asJson(usingTokenAuthentication(onlyFromHub(this::replaceTokens))));
        post("/hub/sync/storeCredentials", asJson(usingTokenAuthentication(onlyFromHub(this::storeCredentials))));
        post("/hub/sync/setHubDetails", asJson(usingTokenAuthentication(onlyFromHub(this::setHubDetails))));
        get("/hub/managerinfo", asJson(usingTokenAuthentication(onlyFromHub(this::getManagerInfo))));
        post("/hub/scheduleProductRefresh",
                asJson(usingTokenAuthentication(onlyFromHub(this::scheduleProductRefresh))));
        post("/hub/storeReportDbCredentials",
                asJson(usingTokenAuthentication(onlyFromHub(this::setReportDbCredentials))));
        post("/hub/removeReportDbCredentials",
                asJson(usingTokenAuthentication(onlyFromHub(this::removeReportDbCredentials))));
        get("/hub/listAllPeripheralOrgs",
                asJson(usingTokenAuthentication(onlyFromHub(this::listAllPeripheralOrgs))));
        get("/hub/listAllPeripheralChannels",
                asJson(usingTokenAuthentication(onlyFromHub(this::listAllPeripheralChannels))));
        post("/hub/syncChannels",
                asJson(usingTokenAuthentication(onlyFromHub(this::syncChannels))));
        post("/hub/sync/channelfamilies",
                asJson(usingTokenAuthentication(onlyFromHub(this::synchronizeChannelFamilies))));
        post("/hub/sync/products",
                asJson(usingTokenAuthentication(onlyFromHub(this::synchronizeProducts))));
        post("/hub/sync/repositories",
                asJson(usingTokenAuthentication(onlyFromHub(this::synchronizeRepositories))));
        post("/hub/sync/subscriptions",
                asJson(usingTokenAuthentication(onlyFromHub(this::synchronizeSubscriptions))));
        post("/hub/sync/migrate/v1/deleteMaster",
                asJson(usingTokenAuthentication(onlyFromHub(this::deleteV1Master))));
    }

    private String scheduleProductRefresh(Request request, Response response, IssAccessToken issAccessToken) {
        try {
            Date earliest = Date.from(Instant.now().plus(10, ChronoUnit.SECONDS));
            taskomaticApi.scheduleProductRefresh(earliest, false);
        }
        catch (TaskomaticApiException ex) {
            LOGGER.error("Scheduling a product refresh failed. ", ex);
            return internalServerError(response, "Scheduling product refresh failed");
        }
        return success(response);
    }

    private String setHubDetails(Request request, Response response, IssAccessToken accessToken) {
        Type mapType = new TypeToken<Map<String, String>>() { }.getType();
        UpdatableServerData data = new UpdatableServerData(GSON.fromJson(request.body(), mapType));

        try {
            hubManager.updateServerData(accessToken, accessToken.getServerFqdn(), IssRole.HUB, data);
        }
        catch (IllegalArgumentException ex) {
            LOGGER.error("Invalid data provided: ", ex);
            return badRequest(response, "Invalid data");
        }
        catch (TaskomaticApiException ex) {
            LOGGER.error("Unable to schedule Taskomatic execution to refresh the root ca: ", ex);
            return internalServerError(response, "Unable to schedule refresh of the root CA certificate");
        }
        return success(response);
    }

    private String deregister(Request request, Response response, IssAccessToken accessToken) {
        // request to delete the local access for the requesting server.
        IssRole remoteRole = hubManager.deleteIssServerLocal(accessToken, accessToken.getServerFqdn());

        // Add a notification to inform the user this server has been deregistered
        var notificationData = new HubRegistrationChanged(false, remoteRole, accessToken.getServerFqdn());
        var notification = UserNotificationFactory.createNotificationMessage(notificationData);
        UserNotificationFactory.storeNotificationMessageFor(notification, RoleFactory.SAT_ADMIN);

        return success(response);
    }

    private String replaceTokens(Request request, Response response, IssAccessToken currentAccessToken) {
        String newRemoteToken = GSON.fromJson(request.body(), String.class);
        if (newRemoteToken.isBlank()) {
            LOGGER.error("Bad Request: invalid data");
            return badRequest(response, "Invalid data");
        }
        try {
            String newLocalToken = hubManager.replaceTokens(currentAccessToken, newRemoteToken);
            return success(response, newLocalToken);
        }
        catch (TokenParsingException ex) {
            LOGGER.error("Unable to parse the received token for server {}", currentAccessToken.getServerFqdn());
            return badRequest(response, "The specified token is not parseable");
        }
        catch (TokenBuildingException ex) {
            LOGGER.error("Unable to build token");
            return badRequest(response, "The token could not be build");
        }
    }

    private String removeReportDbCredentials(Request request, Response response, IssAccessToken token) {
        Map<String, String> creds = GSON.fromJson(request.body(), Map.class);
        String dbname = Config.get().getString(ConfigDefaults.REPORT_DB_NAME, "");

        if (dbname.isBlank() || !creds.containsKey("username")) {
            LOGGER.error("Bad Request: Invalid Data");
            return badRequest(response, "Invalid data");
        }
        ConnectionManager localRcm = ConnectionManagerFactory.localReportingConnectionManager();
        try {
            ReportDbHibernateFactory localRh = new ReportDbHibernateFactory(localRcm);
            ReportDBHelper dbHelper = ReportDBHelper.INSTANCE;
            String username = creds.get("username");
            if (dbHelper.hasDBUser(localRh.getSession(), username)) {
                dbHelper.dropDBUser(localRh.getSession(), username);
                localRcm.commitTransaction();
                return success(response);
            }
            LOGGER.error("Bad Request: DB User '{}' does not exist", username);
        }
        catch (Exception e) {
            LOGGER.error("Bad Request: removing user failed", e);
        }
        localRcm.rollbackTransaction();
        return badRequest(response, "Request failed");
    }

    private String setReportDbCredentials(Request request, Response response, IssAccessToken token) {
        Map<String, String> creds = GSON.fromJson(request.body(), Map.class);
        String dbname = Config.get().getString(ConfigDefaults.REPORT_DB_NAME, "");

        if (dbname.isBlank() || !(creds.containsKey("username") && creds.containsKey("password"))) {
            LOGGER.error("Bad Request: Invalid Data");
            return badRequest(response, "Invalid data");
        }
        ConnectionManager localRcm = ConnectionManagerFactory.localReportingConnectionManager();
        try {
            ReportDbHibernateFactory localRh = new ReportDbHibernateFactory(localRcm);
            ReportDBHelper dbHelper = ReportDBHelper.INSTANCE;
            if (dbHelper.hasDBUser(localRh.getSession(), creds.get("username"))) {
                dbHelper.changeDBPassword(localRh.getSession(), creds.get("username"), creds.get("password"));
            }
            else {
                dbHelper.createDBUser(localRh.getSession(), dbname,
                        creds.get("username"), creds.get("password"));
            }
            localRcm.commitTransaction();
            return success(response);
        }
        catch (Exception e) {
            LOGGER.error("Bad Request: setting user/password failed", e);
        }
        localRcm.rollbackTransaction();
        return badRequest(response, "Request failed");
    }

    private String getManagerInfo(Request request, Response response, IssAccessToken token) {
        ManagerInfoJson managerInfo = hubManager.collectManagerInfo(token);
        return success(response, managerInfo);
    }

    // Basic ping to check if the system is up
    private String ping(Request request, Response response, IssAccessToken token) {
        return message(response, "Pinged from %s".formatted(token.getServerFqdn()));
    }

    private String registerHub(Request request, Response response, IssAccessToken token) {
        RegisterJson registerRequest = GSON.fromJson(request.body(), RegisterJson.class);

        String tokenToStore = registerRequest.getToken();
        if (StringUtils.isEmpty(tokenToStore)) {
            LOGGER.error("No token received in the request for server {}", token.getServerFqdn());
            return badRequest(response, "Required token is missing");
        }

        try {
            hubManager.storeAccessToken(token, tokenToStore);
            hubManager.saveNewServer(token, IssRole.HUB, registerRequest.getRootCA(), registerRequest.getGpgKey());

            // Add a notification to inform the user this server is now a peripheral
            var notificationData = new HubRegistrationChanged(true, IssRole.HUB, token.getServerFqdn());
            var notification = UserNotificationFactory.createNotificationMessage(notificationData);
            UserNotificationFactory.storeNotificationMessageFor(notification, RoleFactory.SAT_ADMIN);

            return success(response);
        }
        catch (TokenParsingException ex) {
            LOGGER.error("Unable to parse the received token for server {}", token.getServerFqdn(), ex);
            return badRequest(response, "The specified token is not parseable");
        }
        catch (TaskomaticApiException ex) {
            LOGGER.error("Unable to schedule root CA certificate update {}", token.getServerFqdn(), ex);
            return internalServerError(response, "Unable to schedule root CA certificate update");
        }
    }

    private String getServerInfo(Request request, Response response, IssAccessToken token) {
        ServerInfoJson serverInfoJson = hubManager.getServerInfo(token);
        return success(response, serverInfoJson);
    }

    private String storeCredentials(Request request, Response response, IssAccessToken token) {
        SCCCredentialsJson storeRequest = GSON.fromJson(request.body(), SCCCredentialsJson.class);

        try {
            hubManager.storeSCCCredentials(token, storeRequest.getUsername(), storeRequest.getPassword());
            return success(response);
        }
        catch (IllegalArgumentException ex) {
            // This should never happen, fqdn guaranteed be a hub after calling allowingOnlyHub() on route init.
            return badRequest(response, "Specified FQDN is not a known hub");
        }
    }

    private String listAllPeripheralOrgs(Request request, Response response, IssAccessToken token) {
        List<OrgInfoJson> allOrgsInfo = hubManager.collectAllOrgs(token)
                .stream()
                .map(org -> new OrgInfoJson(
                        org.getId(),
                        org.getName(),
                        org.getOwnedChannels().stream().map(Channel::getLabel).toList()))
                .toList();

        return success(response, allOrgsInfo);
    }

    private String listAllPeripheralChannels(Request request, Response response, IssAccessToken token) {
        List<ChannelInfoJson> allChannelsInfo = hubManager.collectAllChannels(token)
                .stream()
                .map(ch -> new ChannelInfoJson(ch.getId(), ch.getName(), ch.getLabel(), ch.getSummary(),
                        ((null == ch.getOrg()) ? null : ch.getOrg().getId()),
                        (null == ch.getParentChannel()) ? null : ch.getParentChannel().getId()))
                .toList();

        return success(response, allChannelsInfo);
    }

    private String syncChannels(Request request, Response response, IssAccessToken token) {
        Type listType = new TypeToken<List<ChannelInfoDetailsJson>>() { }.getType();
        List<ChannelInfoDetailsJson> channelInfoList = GSON.fromJson(request.body(), listType);

        if (channelInfoList.isEmpty()) {
            LOGGER.info("No action to take");
            return success(response);
        }

        try {
            hubManager.syncChannels(token, channelInfoList);
            Date earliest = Date.from(Instant.now().plus(10, ChronoUnit.SECONDS));
            taskomaticApi.scheduleProductRefresh(earliest, false);
            return success(response);
        }
        catch (IllegalArgumentException ex) {
            LOGGER.error("Illegal arguments in syncChannels", ex);
            return badRequest(response, ex.getMessage());
        }
        catch (TaskomaticApiException ex) {
            LOGGER.error("Scheduling a product refresh failed. ", ex);
            return internalServerError(response, "Scheduling product refresh failed");
        }
        catch (Exception e) {
            LOGGER.error("Internal server error in syncChannels", e);
            return internalServerError(response, e.getMessage());
        }
    }

    private String synchronizeChannelFamilies(Request request, Response response, IssAccessToken token) {
        return json(response, hubManager.synchronizeChannelFamilies(token));
    }

    private String synchronizeProducts(Request request, Response response, IssAccessToken token) {
        return json(response, hubManager.synchronizeProducts(token));
    }

    private String synchronizeRepositories(Request request, Response response, IssAccessToken token) {
        return json(response, hubManager.synchronizeRepositories(token));
    }

    private String synchronizeSubscriptions(Request request, Response response, IssAccessToken token) {
        return json(response, hubManager.synchronizeSubscriptions(token));
    }

    private String deleteV1Master(Request request, Response response, IssAccessToken token) {

        try {
            hubManager.deleteIssV1Master(token);
            return success(response);
        }
        catch (IllegalStateException ex) {
            LOGGER.error("Invalid fqdn {}", token.getServerFqdn(), ex);
            return badRequest(response, ex.getMessage());
        }
    }
}
