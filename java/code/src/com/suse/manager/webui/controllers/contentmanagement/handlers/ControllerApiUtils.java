/*
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


import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.contentmgmt.SoftwareProjectSource;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.contentmgmt.ContentManager;
import com.redhat.rhn.manager.contentmgmt.FilterTemplateManager;

import com.suse.manager.webui.controllers.UserLocalizationDateAdapter;
import com.suse.manager.webui.controllers.contentmanagement.mappers.ResponseMappers;
import com.suse.manager.webui.utils.SparkApplicationHelper;
import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import spark.Response;

/**
 * Utility class to all the api Controllers
 */
public class ControllerApiUtils {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new UserLocalizationDateAdapter())
            .serializeNulls()
            .create();

    public static final ContentManager CONTENT_MGR = new ContentManager();
    public static final FilterTemplateManager TEMPLATE_MGR = new FilterTemplateManager();


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

        Set<SoftwareProjectSource> swSourcesWithUnsyncedPatches =
                ContentManager.listActiveSwSourcesWithUnsyncedPatches(user, dbContentProject);
        Map<Long, Long> sourceTagetChannelIds = new HashMap<>();
        if (!dbContentEnvironments.isEmpty() && !swSourcesWithUnsyncedPatches.isEmpty()) {
            ContentEnvironment first = dbContentEnvironments.get(0);
            swSourcesWithUnsyncedPatches.forEach(swsource -> {
                Channel sChan = swsource.getChannel();
                Optional<Long> tgtChanId = ContentManager.lookupTargetByChannel(sChan, first, user)
                        .map(target -> target.getChannel().getId());
                sourceTagetChannelIds.put(sChan.getId(), tgtChanId.orElse(null));
            });
        }

        return SparkApplicationHelper.json(GSON, res, ResultJson.success(
                ResponseMappers.mapProjectFromDB(dbContentProject, dbContentEnvironments, swSourcesWithUnsyncedPatches,
                        sourceTagetChannelIds)
        ), new TypeToken<>() { });
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
                .collect(Collectors.toMap(p -> p, ContentProjectFactory::listFilterProjects));

        return SparkApplicationHelper.json(GSON, res, ResultJson.success(
                ResponseMappers.mapFilterListingFromDB(filtersWithProjects)
        ), new TypeToken<>() { });
    }


}
