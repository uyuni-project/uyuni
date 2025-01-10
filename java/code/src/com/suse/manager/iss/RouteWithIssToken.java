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

import com.suse.manager.webui.utils.token.Token;

import spark.Request;
import spark.Response;

/**
 * A route that gets the authentication token in addition to the request and response.
 */
@FunctionalInterface
public interface RouteWithIssToken {

    /**
     * Invoked when a request is made on this route's corresponding path.
     *
     * @param request the request object
     * @param response the response object
     * @param token the token with this request
     * @param serverFqdn the FQDN of the remote server
     * @return the content to be set in the response
     */
    Object handle(Request request, Response response, Token token, String serverFqdn);
}
