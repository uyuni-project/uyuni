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

package com.suse.manager.webui.controllers;

import com.redhat.rhn.domain.matcher.MatcherRunData;
import com.redhat.rhn.domain.matcher.MatcherRunDataFactory;
import com.redhat.rhn.domain.server.PinnedSubscription;
import com.redhat.rhn.domain.server.PinnedSubscriptionFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suse.manager.matcher.MatcherJsonIO;
import com.suse.manager.webui.services.subscriptionmatching.SubscriptionMatchProcessor;

import java.util.Date;
import java.util.HashMap;

import spark.ModelAndView;
import spark.Request;
import spark.Response;

/**
 * Controller class providing backend code for subscription-matcher pages.
 */
public class SubscriptionMatchingController {

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
        .serializeNulls()
        .create();

    private SubscriptionMatchingController() { }

    /**
     * Displays the subscription-matcher report page
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView show(Request request, Response response, User user) {
        return new ModelAndView(new HashMap<>(), "templates/subscription-matching/show.jade");
    }

    /**
     * Returns JSON data from subscription-matcher
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return JSON result of the API call
     */
    public static String data(Request request, Response response, User user) {
        MatcherJsonIO matcherJsonIO = new MatcherJsonIO();
        Object data = new SubscriptionMatchProcessor().getData(
                matcherJsonIO.getLastMatcherInput(),
                matcherJsonIO.getLastMatcherOutput());
        response.type("application/json");
        return GSON.toJson(data);
    }

    /**
     * Invokes download of a csv from the filename given in the request.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the contents of the given csv file
     */
    public static String csv(Request request, Response response, User user) {
        String filename = request.params("filename");
        response.raw().setContentType("application/csv");
        MatcherRunData data = MatcherRunDataFactory.getSingle();
        if (data == null) {
            throw new IllegalStateException("File with subscription matcher data not" +
                    " found.");
        }
        return data.getCSVContentsByFilename(filename);
    }

    /**
     * Schedule run of gatherer-matcher-bunch.
     * @param request the request
     * @param response the response
     * @param user the user
     * @return null
     */
    public static String scheduleMatcherRun(Request request, Response response, User user) {
        try {
            new TaskomaticApi().scheduleSingleSatBunch(user, "gatherer-matcher-bunch",
                    new HashMap<>());
        }
        catch (TaskomaticApiException e) {
            new RuntimeException(e);
        }
        return "";
    }

    /**
     * Adds a pin.
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the new pin table data as json
     */
    public static String createPin(Request request, Response response, User user) {
        PinnedSubscription pin = new PinnedSubscription();
        Long subscriptionId = Long.parseLong(request.queryParams("subscription_id"));
        Long systemId = Long.parseLong(request.queryParams("system_id"));
        pin.setSubscriptionId(subscriptionId);
        pin.setSystemId(systemId);
        if (PinnedSubscriptionFactory.getInstance()
                .lookupBySystemIdAndSubscriptionId(systemId, subscriptionId) == null) {
            PinnedSubscriptionFactory.getInstance().save(pin);
        }

        MatcherJsonIO matcherJsonIO = new MatcherJsonIO();
        Object data = new SubscriptionMatchProcessor().pinnedMatches(
                matcherJsonIO.getLastMatcherInput().get(),
                matcherJsonIO.getLastMatcherOutput().get());

        response.type("application/json");
        return GSON.toJson(data);
    }

    /**
     * Delete a pin.
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the new pin table data as json
     */
    public static String deletePin(Request request, Response response, User user) {
        Long pinId = Long.parseLong(request.params("id"));
        PinnedSubscription pin = PinnedSubscriptionFactory.getInstance().lookupById(pinId);
        if (pin != null) {
            PinnedSubscriptionFactory.getInstance().remove(pin);
        }

        MatcherJsonIO matcherJsonIO = new MatcherJsonIO();
        Object data = new SubscriptionMatchProcessor().pinnedMatches(
                matcherJsonIO.getLastMatcherInput().get(),
                matcherJsonIO.getLastMatcherOutput().get());

        response.type("application/json");
        return GSON.toJson(data);
    }
}
