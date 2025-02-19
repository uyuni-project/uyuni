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
import static com.suse.manager.webui.utils.SparkApplicationHelper.notFound;
import static com.suse.manager.webui.utils.SparkApplicationHelper.success;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withProductAdmin;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.patch;
import static spark.Spark.post;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.hub.HubManager;
import com.suse.manager.hub.InvalidResponseException;
import com.suse.manager.model.hub.AccessTokenDTO;
import com.suse.manager.model.hub.IssAccessToken;
import com.suse.manager.model.hub.IssRole;
import com.suse.manager.model.hub.IssServer;
import com.suse.manager.model.hub.TokenType;
import com.suse.manager.webui.controllers.ECMAScriptDateAdapter;
import com.suse.manager.webui.controllers.admin.beans.CreateTokenRequest;
import com.suse.manager.webui.controllers.admin.beans.HubRegisterRequest;
import com.suse.manager.webui.controllers.admin.beans.IssV3PeripheralsResponse;
import com.suse.manager.webui.controllers.admin.beans.ValidityRequest;
import com.suse.manager.webui.utils.FlashScopeHelper;
import com.suse.manager.webui.utils.PageControlHelper;
import com.suse.manager.webui.utils.gson.PagedDataResultJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.utils.token.TokenBuildingException;
import com.suse.manager.webui.utils.token.TokenException;
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
        // Hub managementF
        get("/manager/api/admin/hub", withProductAdmin(this::pass));
        get("/manager/api/admin/hub/:id", withProductAdmin(this::pass));

        // Peripherals management
        get("/manager/api/admin/hub/peripherals/list", withProductAdmin(this::listPaginatedPeripherals));
        post("/manager/api/admin/hub/peripherals", withProductAdmin(this::registerPeripheral));
        get("/manager/api/admin/hub/peripheral/:id", withProductAdmin(this::pass));
        patch("/manager/api/admin/hub/peripheral/:id", withProductAdmin(this::pass));

        // Token management
        get("/manager/api/admin/hub/access-tokens", withProductAdmin(this::listTokens));
        post("/manager/api/admin/hub/access-tokens", withProductAdmin(this::createToken));
        post("/manager/api/admin/hub/access-tokens/:id/validity", withProductAdmin(this::setAccessTokenValidity));
        delete("/manager/api/admin/hub/access-tokens/:id", withProductAdmin(this::deleteAccessToken));
    }

    private String listPaginatedPeripherals(Request request, Response response, User satAdmin) {
        PageControlHelper pageHelper = new PageControlHelper(request);
        PageControl pc = pageHelper.getPageControl();
        long totalSize = hubManager.countRegisteredPeripherals(satAdmin);
        List<IssV3PeripheralsResponse> peripherals = hubManager.listRegisteredPeripherals(satAdmin, pc).stream()
                .map(IssV3PeripheralsResponse::fromIssEntity).toList();
        TypeToken<PagedDataResultJson<IssV3PeripheralsResponse, Long>> type = new TypeToken<>() { };
        return json(GSON, response, new PagedDataResultJson<>(peripherals, totalSize, Collections.emptySet()), type);
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

    private String pass(Request request, Response response, User user) {
        return success(response);
    }
}
