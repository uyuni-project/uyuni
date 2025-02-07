/*
 * Copyright (c) 2024--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.hub;

import static com.suse.manager.hub.HubSparkHelper.allowingOnlyHub;
import static com.suse.manager.hub.HubSparkHelper.allowingOnlyPeripheral;
import static com.suse.manager.hub.HubSparkHelper.allowingOnlyRegistered;
import static com.suse.manager.hub.HubSparkHelper.allowingOnlyUnregistered;
import static com.suse.manager.hub.HubSparkHelper.usingTokenAuthentication;
import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.badRequest;
import static com.suse.manager.webui.utils.SparkApplicationHelper.internalServerError;
import static com.suse.manager.webui.utils.SparkApplicationHelper.message;
import static com.suse.manager.webui.utils.SparkApplicationHelper.success;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.ConnectionManager;
import com.redhat.rhn.common.hibernate.ConnectionManagerFactory;
import com.redhat.rhn.common.hibernate.ReportDbHibernateFactory;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.taskomatic.task.ReportDBHelper;

import com.suse.manager.model.hub.ChannelInfoJson;
import com.suse.manager.model.hub.CustomChannelInfoJson;
import com.suse.manager.model.hub.IssAccessToken;
import com.suse.manager.model.hub.IssRole;
import com.suse.manager.model.hub.ManagerInfoJson;
import com.suse.manager.model.hub.OrgInfoJson;
import com.suse.manager.model.hub.RegisterJson;
import com.suse.manager.model.hub.SCCCredentialsJson;
import com.suse.manager.webui.controllers.ECMAScriptDateAdapter;
import com.suse.manager.webui.utils.token.TokenBuildingException;
import com.suse.manager.webui.utils.token.TokenParsingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import spark.Request;
import spark.Response;

public class HubController {

    private static final Logger LOGGER = LogManager.getLogger(HubController.class);

    private final HubManager hubManager;

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
        .serializeNulls()
        .create();

    /**
     * Default constructor
     */
    public HubController() {
        this(new HubManager());
    }

    /**
     * Builds an instance with the specified hub manager
     * @param hubManagerIn the hub manager
     */
    public HubController(HubManager hubManagerIn) {
        this.hubManager = hubManagerIn;
    }

    /**
     * Initialize the API routes
     */
    public void initRoutes() {
        post("/hub/ping", asJson(usingTokenAuthentication(this::ping)));
        post("/hub/sync/deregister", asJson(usingTokenAuthentication(allowingOnlyRegistered(this::deregister))));
        post("/hub/sync/registerHub", asJson(usingTokenAuthentication(allowingOnlyUnregistered(this::registerHub))));
        post("/hub/sync/replaceTokens", asJson(usingTokenAuthentication(allowingOnlyHub(this::replaceTokens))));
        post("/hub/sync/storeCredentials", asJson(usingTokenAuthentication(allowingOnlyHub(this::storeCredentials))));
        post("/hub/sync/setHubDetails", asJson(usingTokenAuthentication(allowingOnlyHub(this::setHubDetails))));
        get("/hub/managerinfo", asJson(usingTokenAuthentication(allowingOnlyHub(this::getManagerInfo))));
        post("/hub/storeReportDbCredentials",
                asJson(usingTokenAuthentication(allowingOnlyHub(this::setReportDbCredentials))));
        post("/hub/removeReportDbCredentials",
                asJson(usingTokenAuthentication(allowingOnlyHub(this::removeReportDbCredentials))));
        get("/hub/listAllPeripheralOrgs",
                asJson(usingTokenAuthentication(allowingOnlyPeripheral(this::listAllPeripheralOrgs))));
        get("/hub/listAllPeripheralChannels",
                asJson(usingTokenAuthentication(allowingOnlyPeripheral(this::listAllPeripheralChannels))));
        post("/hub/addVendorChannels",
                asJson(usingTokenAuthentication(allowingOnlyPeripheral(this::addVendorChannels))));
        post("/hub/addCustomChannels",
                asJson(usingTokenAuthentication(allowingOnlyPeripheral(this::addCustomChannels))));
    }

    private String setHubDetails(Request request, Response response, IssAccessToken accessToken) {
        Map<String, String> data = GSON.fromJson(request.body(), Map.class);

        try {
            hubManager.updateServerData(accessToken, accessToken.getServerFqdn(), IssRole.HUB, data);
        }
        catch (IllegalArgumentException ex) {
            LOGGER.error("Invalid data provided: ", ex);
            return badRequest(response, "Invalid data");
        }
        catch (Exception ex) {
            LOGGER.error("Internal Server Error: ", ex);
            return internalServerError(response, "Internal Server Error");
        }
        return success(response);
    }

    private String deregister(Request request, Response response, IssAccessToken accessToken) {
        // request to delete the local access for the requesting server.
        try {
            hubManager.deleteIssServerLocal(accessToken, accessToken.getServerFqdn());
        }
        catch (Exception ex) {
            LOGGER.error("Internal Server Error: ", ex);
            return internalServerError(response, "Internal Server Error");
        }
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

            return success(response);
        }
        catch (TokenParsingException ex) {
            LOGGER.error("Unable to parse the received token for server {}", token.getServerFqdn());
            return badRequest(response, "The specified token is not parseable");
        }
        catch (TaskomaticApiException ex) {
            LOGGER.error("Unable to schedule root CA certificate update {}", token.getServerFqdn());
            return internalServerError(response, "Unable to schedule root CA certificate update");
        }
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
                .map(org -> new OrgInfoJson(org.getId(), org.getName()))
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

    private String addVendorChannels(Request request, Response response, IssAccessToken token) {
        Map<String, String> requestList = GSON.fromJson(request.body(), Map.class);

        if ((null == requestList) || (!requestList.containsKey("vendorchannellabellist"))) {
            return badRequest(response, "Invalid data: missing vendorchannellabellist entry");
        }

        List<String> vendorChannelLabelList = GSON.fromJson(requestList.get("vendorchannellabellist"), List.class);
        if (vendorChannelLabelList == null || vendorChannelLabelList.isEmpty()) {
            LOGGER.error("Bad Request: invalid invalid vendor channel label list");
            return badRequest(response, "Invalid data: invalid vendor channel label list");
        }

        List<ChannelInfoJson> createdVendorChannelInfoList =
                hubManager.addVendorChannels(token, vendorChannelLabelList)
                        .stream()
                        .map(ch -> new ChannelInfoJson(ch.getId(), ch.getName(), ch.getLabel(), ch.getSummary(),
                                ((null == ch.getOrg()) ? null : ch.getOrg().getId()),
                                (null == ch.getParentChannel()) ? null : ch.getParentChannel().getId()))
                        .toList();
        return success(response, createdVendorChannelInfoList);
    }


    private String addCustomChannels(Request request, Response response, IssAccessToken token) {
        Map<String, String> requestList = GSON.fromJson(request.body(), Map.class);

        if ((null == requestList) || (!requestList.containsKey("customchannellist"))) {
            return badRequest(response, "Invalid data: missing customchannellist entry");
        }

        List<CustomChannelInfoJson> customChannelInfoList = Arrays.asList(
                GSON.fromJson(requestList.get("customchannellist"), CustomChannelInfoJson[].class));

        if (customChannelInfoList.isEmpty()) {
            LOGGER.error("Bad Request: invalid custom channels list");
            return badRequest(response, "Invalid data: invalid custom channels list");
        }

        List<ChannelInfoJson> createdCustomChannelsInfoList =
                hubManager.addCustomChannels(token, customChannelInfoList)
                        .stream()
                        .map(ch -> new ChannelInfoJson(ch.getId(), ch.getName(), ch.getLabel(), ch.getSummary(),
                                ((null == ch.getOrg()) ? null : ch.getOrg().getId()),
                                (null == ch.getParentChannel()) ? null : ch.getParentChannel().getId()))
                        .toList();
        return success(response, createdCustomChannelsInfoList);
    }
}
