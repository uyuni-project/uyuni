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

import static com.suse.manager.webui.utils.SparkApplicationHelper.badRequest;
import static com.suse.manager.webui.utils.SparkApplicationHelper.internalServerError;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.success;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withProductAdmin;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.hub.HubManager;
import com.suse.manager.hub.InvalidResponseException;
import com.suse.manager.model.hub.AccessTokenDTO;
import com.suse.manager.model.hub.IssRole;
import com.suse.manager.model.hub.IssServer;
import com.suse.manager.webui.controllers.ECMAScriptDateAdapter;
import com.suse.manager.webui.controllers.admin.beans.HubRegisterRequest;
import com.suse.manager.webui.utils.FlashScopeHelper;
import com.suse.manager.webui.utils.PageControlHelper;
import com.suse.manager.webui.utils.gson.PagedDataResultJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.utils.token.TokenBuildingException;
import com.suse.manager.webui.utils.token.TokenParsingException;

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

import javax.net.ssl.SSLException;

import spark.Request;
import spark.Response;

public class HubApiController {
    private static final Logger LOGGER = LogManager.getLogger(HubApiController.class);

    private static final LocalizationService LOC = LocalizationService.getInstance();

    private final HubManager hubManager;

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
        .serializeNulls()
        .create();

    /**
     * Default constructor
     */
    public HubApiController() {
        this(new HubManager());
    }

    /**
     * Creates an instance with the given dependencies
     * @param hubManagerIn the hub manager
     */
    public HubApiController(HubManager hubManagerIn) {
        this.hubManager = hubManagerIn;
    }

    /**
     * initialize all the API Routes for the ISSv3 support
     */
    public void initRoutes() {
        post("/manager/api/admin/hub/peripherals", withProductAdmin(this::registerPeripheral));
        get("/manager/api/admin/hub/access-tokens", withProductAdmin(this::listTokens));
    }

    private String registerPeripheral(Request request, Response response, User satAdmin) {
        HubRegisterRequest issRequest;

        try {
            issRequest = validateRegisterRequest(GSON.fromJson(request.body(), HubRegisterRequest.class));
        }
        catch (JsonSyntaxException ex) {
            LOGGER.error("Unable to parse JSON request", ex);
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
        catch (IOException ex) {
            LOGGER.error("Error while attempting to connect to remote server {}", remoteServer, ex);
            return internalServerError(response, LOC.getMessage("hub.error_connecting_remote"));
        }
        catch (RuntimeException ex) {
            LOGGER.error("Unexpected error while registering remote server {}", remoteServer, ex);
            return internalServerError(response, LOC.getMessage("hub.unexpected_error"));
        }
    }

    private String listTokens(Request request, Response response, User user) {
        PageControlHelper pageHelper = new PageControlHelper(request);
        PageControl pc = pageHelper.getPageControl();

        long totalSize = hubManager.countAccessToken(user);

        List<AccessTokenDTO> accessTokens = hubManager.listAccessToken(user, pc);
        TypeToken<PagedDataResultJson<AccessTokenDTO, Long>> type = new TypeToken<>() { };
        return json(GSON, response, new PagedDataResultJson<>(accessTokens, totalSize, Collections.emptySet()), type);
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
}
