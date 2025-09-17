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

import com.suse.manager.model.hub.IssAccessToken;

import spark.Request;
import spark.Response;

/**
 * A route that gets the authentication token in addition to the request and response.
 */
@FunctionalInterface
public interface RouteWithHubToken {

    /**
     * Invoked when a request is made on this route's corresponding path.
     *
     * @param request the request object
     * @param response the response object
     * @param token the access token granting access and identifying the caller
     * @return the content to be set in the response
     */
    Object handle(Request request, Response response, IssAccessToken token);
}
