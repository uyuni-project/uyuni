/**
 * Copyright (c) 2019 SUSE LLC
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
package com.suse.manager.webui.controllers.contentmanagement.handlers;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;

import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.contentmgmt.ContentManager;

import com.suse.manager.webui.controllers.contentmanagement.mappers.ResponseMappers;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.utils.Json;

import com.google.gson.Gson;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import spark.Response;

/**
 * Utility class to all the api Controllers
 */
public class ControllerApiUtils {
    private static final Gson GSON = Json.GSON;

    private ControllerApiUtils() { }

    /**
     * Map the full project data to json and return as a Controller response
     * @param res the http response
     * @param projectLabel - the label
     * @param user - the user
     * @return return the full project as json
     */
    public static String fullProjectJsonResponse(Response res, String projectLabel, User user) {
        ContentProject dbContentProject = ContentManager.lookupProject(projectLabel, user).get();

        List<ContentEnvironment> dbContentEnvironments = ContentManager.listProjectEnvironments(projectLabel, user);

        return json(GSON, res, ResultJson.success(
                ResponseMappers.mapProjectFromDB(dbContentProject, dbContentEnvironments)
        ));
    }

    /**
     * Map the full filters list data to json and return as a Controller response
     * @param res the http response
     * @param user - the user
     * @return return the full project as json
     */
    public static String listFiltersJsonResponse(Response res, User user) {
        Map<ContentFilter, List<ContentProject>> filtersWithProjects = ContentManager.listFilters(user)
                .stream()
                .collect(Collectors.toMap(p -> p, p -> ContentProjectFactory.listFilterProjects(p)));

        return json(GSON, res, ResultJson.success(
                ResponseMappers.mapFilterListingFromDB(filtersWithProjects)
        ));
    }


}
