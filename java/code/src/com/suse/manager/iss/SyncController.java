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

package com.suse.manager.iss;

import static com.suse.manager.iss.IssSparkHelper.allowingOnlyUnregistered;
import static com.suse.manager.iss.IssSparkHelper.usingTokenAuthentication;
import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static spark.Spark.post;

import com.suse.manager.model.hub.HubManager;
import com.suse.manager.webui.controllers.ECMAScriptDateAdapter;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.utils.token.Token;
import com.suse.manager.webui.utils.token.TokenParsingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import spark.Request;
import spark.Response;

public class SyncController {

    private static final Logger LOGGER = LogManager.getLogger(SyncController.class);

    private final HubManager hubManager;

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
        .serializeNulls()
        .create();

    /**
     * Default constructor
     */
    public SyncController() {
        this(new HubManager());
    }

    /**
     * Builds an instance with the specified hub manager
     * @param hubManagerIn the hub manager
     */
    public SyncController(HubManager hubManagerIn) {
        this.hubManager = hubManagerIn;
    }

    /**
     * Initialize the API routes
     */
    public void initRoutes() {
        post("/iss/sync/ping", asJson(usingTokenAuthentication(this::ping)));
        post("/iss/sync/register", asJson(usingTokenAuthentication(allowingOnlyUnregistered(this::register))));
    }

    // Basic ping to check if the system is up
    private String ping(Request request, Response response, Token token, String fqdn) {
        ResultJson<Object> result = ResultJson.successMessage("Pinged from %s".formatted(fqdn));
        return json(GSON, response, result, new TypeToken<>() { });
    }

    private String register(Request request, Response response, Token token, String fqdn) {
        RegisterJson registerRequest = GSON.fromJson(request.body(), RegisterJson.class);

        String tokenToStore = registerRequest.getToken();
        if (StringUtils.isEmpty(tokenToStore)) {
            LOGGER.error("No token received in the request for server {}", fqdn);
            response.status(HttpServletResponse.SC_BAD_REQUEST);
            return json(response, ResultJson.error("Required token is missing"), new TypeToken<>() { });
        }

        try {
            hubManager.storeAccessToken(fqdn, tokenToStore);
            hubManager.saveNewServer(registerRequest.getRole(), fqdn, registerRequest.getRootCA());

            return json(GSON, response, ResultJson.success(), new TypeToken<>() { });
        }
        catch (TokenParsingException ex) {
            LOGGER.error("Unable to parse the received token for server {}", fqdn);
            response.status(HttpServletResponse.SC_BAD_REQUEST);
            return json(response, ResultJson.error("The specified token is not parseable"), new TypeToken<>() { });
        }
    }
}
