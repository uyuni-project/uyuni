/**
 * Copyright (c) 2017 SUSE LLC
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.image.ImageOverview;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.suse.manager.reactor.messaging.ImageBuildEventMessage;
import com.suse.manager.webui.utils.ViewHelper;
import com.suse.manager.webui.utils.gson.ImageInfoJson;
import com.suse.manager.webui.utils.gson.JsonResult;
import org.apache.commons.lang.StringUtils;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;

/**
 * Spark controller class for image building and listing.
 */
public class ImageBuildController {

    private static final Gson GSON = new GsonBuilder().create();
    private static final Role ADMIN_ROLE = RoleFactory.IMAGE_ADMIN;

    private ImageBuildController() { }

    /**
     * Returns a view to display build page
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public static ModelAndView buildView(Request req, Response res, User user) {
        Map<String, Object> model = new HashMap<>();

        // Parse optional profile id
        if (StringUtils.isNotBlank(req.params("profileId"))) {
            Long profileId = Long.parseLong(req.params("profileId"));
            model.put("profileId", profileId);
        }
        else {
            model.put("profileId", null);
        }

        return new ModelAndView(model, "content_management/build.jade");
    }

    /**
     * Gets a JSON list of Container Build Host entitled systems
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object getBuildHosts(Request req, Response res, User user) {
        ServerGroup sg = ServerGroupFactory
                .lookupEntitled(EntitlementManager.CONTAINER_BUILD_HOST, user.getOrg());

        return json(res,
                getServerStreamJson(SystemManager.systemsInGroupShort(sg.getId())));
    }

    /**
     * Returns a view to display image info list
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public static ModelAndView listView(Request req, Response res, User user) {
        Map<String, Object> model = new HashMap<>();

        // Parse optional image id
        if (StringUtils.isNotBlank(req.params("id"))) {
            Long profileId = Long.parseLong(req.params("id"));
            model.put("id", profileId);
        }
        else {
            model.put("id", null);
        }

        model.put("pageSize", user.getPageSize());
        model.put("isAdmin", user.hasRole(ADMIN_ROLE));
        return new ModelAndView(model, "content_management/view.jade");
    }

    /**
     * Creates a list of JSON objects for a list of {@link SystemOverview}
     * instances, having only 'id' and 'name' properties
     *
     * @param systemList the system overview list
     * @return the list of JSON objects
     */
    private static List<JsonObject> getServerStreamJson(List<SystemOverview> systemList) {
        return systemList.stream().map(s -> {
            JsonObject json = new JsonObject();
            json.addProperty("id", s.getId());
            json.addProperty("name", s.getName());
            return json;
        }).collect(Collectors.toList());
    }

    /**
     * Build request object
     */
    public static class BuildRequest {
        private long buildHostId;
        private String tag;

        /**
         * @return the build host id
         */
        public long getBuildHostId() {
            return buildHostId;
        }

        /**
         * @return the tag
         */
        public String getTag() {
            return tag;
        }
    }

    /**
     * Schedules a build for some image profile
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object build(Request request, Response response, User user) {
        response.type("application/json");

        BuildRequest buildRequest = GSON.fromJson(request.body(), BuildRequest.class);

        Long profileId = Long.parseLong(request.params("id"));
        Optional<ImageProfile> maybeProfile =
                ImageProfileFactory.lookupByIdAndOrg(profileId, user.getOrg());

        return maybeProfile.flatMap(ImageProfile::asDockerfileProfile).map(profile -> {
            MessageQueue.publish(new ImageBuildEventMessage(
                    buildRequest.getBuildHostId(), user.getId(), buildRequest.getTag(),
                    profile.getProfileId()
            ));
            //TODO: Add action ID as a message parameter
            return GSON.toJson(new JsonResult(true, "build_scheduled"));
        }).orElseGet(
                () -> GSON.toJson(new JsonResult(true, Collections.singletonList(
                        "unknown_error")))
        );
    }

    /**
     * Lists image profiles in JSON
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object list(Request req, Response res, User user) {
        List<JsonObject> result =
                getImageInfoSummaryList(ImageInfoFactory.listImageOverviews(user.getOrg()));

        return json(res, result);
    }

    /**
     * Gets a single image info object in JSON
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object get(Request req, Response res, User user) {
        Long id = Long.parseLong(req.params("id"));

        Optional<ImageOverview> imageInfo =
                ImageInfoFactory.lookupOverviewByIdAndOrg(id, user.getOrg());

        return json(res, imageInfo.map(ImageInfoJson::fromImageInfo).orElseGet(null));
    }

    private static List<JsonObject> getImageInfoSummaryList(
            List<ImageOverview> imageInfos) {
        return imageInfos.stream().map(ImageBuildController::getImageInfoSummary)
                .collect(Collectors.toList());
    }

    /**
     * Processes a DELETE request
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object delete(Request req, Response res, User user) {
        Long id = Long.parseLong(req.params("id"));

        JsonResult result =
                ImageInfoFactory.lookupByIdAndOrg(id, user.getOrg()).map(info -> {
                    ImageInfoFactory.delete(info);
                    return new JsonResult(true,
                            Collections.singletonList("delete_success"));
                }).orElseGet(() -> new JsonResult(false,
                        Collections.singletonList("not_found")));

        return json(res, result);
    }

    private static JsonObject getImageInfoSummary(ImageOverview imageOverview) {
        JsonObject json = new JsonObject();
        json.addProperty("id", imageOverview.getId());
        json.addProperty("name", imageOverview.getName());
        json.addProperty("version", imageOverview.getVersion());
        json.addProperty("modified",
                ViewHelper.getInstance().renderDate(imageOverview.getModified()));

        if (imageOverview.getOutdatedPackages() != null) {
            json.addProperty("packages", imageOverview.getOutdatedPackages());
        }

        if (imageOverview.getSecurityErrata() != null) {
            JsonObject patches = new JsonObject();
            patches.addProperty("critical", imageOverview.getSecurityErrata());
            if (imageOverview.getBugErrata() != null &&
                    imageOverview.getEnhancementErrata() != null) {
                patches.addProperty("noncritical", imageOverview.getBugErrata() +
                        imageOverview.getEnhancementErrata());
            }
            json.add("patches", patches);
        }

        if (imageOverview.getAction() != null) {
            json.addProperty("statusId", imageOverview.getAction().getStatus().getId());
        }
        return json;
    }
}
