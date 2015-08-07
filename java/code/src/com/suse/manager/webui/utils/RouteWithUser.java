/**
 * Copyright (c) 2015 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.utils;

import com.redhat.rhn.domain.user.User;

import spark.ModelAndView;
import spark.Request;
import spark.Response;

/**
 * A route that gets the user in addition to the request and response.
 */
public interface RouteWithUser {

    /**
     * Invoked when a request is made on this route's corresponding path.
     *
     * @param request the request object
     * @param response the response object
     * @param user the user associated with this request
     * @return the content to be set in the response
     */
    ModelAndView handle(Request request, Response response, User user);
}
