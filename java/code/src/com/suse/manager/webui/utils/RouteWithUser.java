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
     * Invoked when a request is made on this route's corresponding path e.g. '/hello'
     *
     * @param request  The request object providing information about the HTTP request
     * @param response The response object providing functionality for modifying the response
     * @param user The user associated with this request
     * @return The content to be set in the response
     */
    ModelAndView handle(Request request, Response response, User user);

}
