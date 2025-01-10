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

package com.suse.manager.iss;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;

import com.redhat.rhn.frontend.security.AuthenticationServiceFactory;

import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.IssAccessToken;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.utils.token.Token;
import com.suse.manager.webui.utils.token.TokenParser;
import com.suse.manager.webui.utils.token.TokenParsingException;

import com.google.gson.reflect.TypeToken;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletResponse;

import spark.Route;
import spark.Spark;

public final class IssSparkHelper {

    private static final Logger LOGGER = LogManager.getLogger(IssSparkHelper.class);

    private static final HubFactory HUB_FACTORY = new HubFactory();

    private IssSparkHelper() {
        // Prevent instantiation
    }

    /**
     * Use in ISS routes to specify the method requires api key authentication
     *
     * @param route the route
     * @return the route
     */
    public static Route usingTokenAuthentication(RouteWithIssToken route) {
        return (request, response) -> {
            String authorization = request.headers("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer")) {
                Spark.halt(HttpServletResponse.SC_BAD_REQUEST);
            }

            String serializedToken = authorization.substring(7);
            Token token = parseToken(serializedToken);
            IssAccessToken issuedToken = HUB_FACTORY.lookupIssuedToken(serializedToken);

            if (issuedToken == null || token == null || issuedToken.isExpired() || !issuedToken.isValid()) {
                response.status(HttpServletResponse.SC_UNAUTHORIZED);
                return json(response, ResultJson.error("Invalid token provided"), new TypeToken<>() { });
            }

            try {
                String fqdn = token.getClaim("fqdn", String.class);
                return route.handle(request, response, token, fqdn);
            }
            catch (TokenParsingException ex) {
                response.status(HttpServletResponse.SC_BAD_REQUEST);
                return json(response, ResultJson.error("Invalid token provided: missing claim"), new TypeToken<>() { });
            }
            finally {
                var authenticationService = AuthenticationServiceFactory.getInstance().getAuthenticationService();
                authenticationService.invalidate(request.raw(), response.raw());
            }
        };
    }

    private static Token parseToken(String serializedToken) {
        try {
            return new TokenParser()
                .usingServerSecret()
                .verifyingNotBefore()
                .verifyingExpiration()
                .parse(serializedToken);
        }
        catch (TokenParsingException ex) {
            LOGGER.debug("Unable to parse token {}. Request will be rejected.", serializedToken, ex);
            return null;
        }
    }
}
