/*
 * Copyright (c) 2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.iss;

import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.usingTokenAuthentication;
import static spark.Spark.post;

import com.suse.manager.webui.controllers.ECMAScriptDateAdapter;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.utils.token.Token;
import com.suse.manager.webui.utils.token.TokenParsingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;

import java.util.Date;

import spark.Request;
import spark.Response;
import spark.Spark;

public class SyncController {

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
        .serializeNulls()
        .create();

    /**
     * Initialize the API routes
     */
    public void initRoutes() {
        post("/iss/sync/ping", asJson(usingTokenAuthentication(this::ping)));
    }

    // Basic ping to check if the system is up
    private String ping(Request request, Response response, Token token) {
        try {
            String fqdn = token.getClaim("fqdn", String.class);
            ResultJson<Object> result = ResultJson.successMessage("Pinged from %s".formatted(fqdn));
            return json(GSON, response, result, new TypeToken<>() { });
        }
        catch (TokenParsingException ex) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Invalid token provided: missing required claim");
            return null;
        }
    }
}
