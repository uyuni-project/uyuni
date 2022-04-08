/*
 * Copyright (c) 2021 SUSE LLC
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

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.post;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.taglibs.DWRItemSelector;

import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.errors.NotFoundException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import spark.Request;
import spark.Response;

/**
 * Controller exposing an API to manipulate rhnSets
 */
public class SetController {

    private static final Logger LOG = LogManager.getLogger(SetController.class);

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .serializeNulls()
            .create();

    private SetController() {
    }

    /**
     * Invoked from Router. Initialize routes for Systems Views.
     */
    public static void initRoutes() {
        post("/manager/api/sets/:label",
                withUser(SetController::updateSet));
    }

    /**
     * Add selected systems to SSM
     *
     * @param request the http request
     * @param response the http response
     * @param user the user
     * @return the json response
     */
    public static String updateSet(Request request, Response response, User user) {
        String setLabel = request.params("label");
        Map<String, Boolean> data = GSON.fromJson(request.body(), new TypeToken<Map<String, Boolean>>() { }.getType());
        List<Integer> results = Stream.of(true, false).map(add -> {
            List<String> changes = data.keySet().stream()
                    .filter(item -> data.get(item) == add)
                    .collect(Collectors.toList());
            try {
                return DWRItemSelector.updateSetFromRequest(request.raw(), setLabel,
                        changes.toArray(new String[0]), add, user);
            }
            catch (Exception e) {
                LOG.error("Failed to change set {}", setLabel);
                return null;
            }
        }).collect(Collectors.toList());
        Integer newCount = results.get(results.size() - 1);
        if (newCount == null) {
            throw new NotFoundException("Failed to change set: " + setLabel);
        }
        return json(GSON, response, newCount);
    }
}
