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

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.action.salt.build.ImageBuildAction;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageInfoCustomDataValue;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.image.ImageOverview;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.context.Context;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.reactor.messaging.ActionScheduledEventMessage;
import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.webui.utils.ViewHelper;
import com.suse.manager.webui.utils.gson.ImageInfoJson;
import com.suse.manager.webui.utils.gson.JsonResult;

import org.apache.commons.lang.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import spark.ModelAndView;
import spark.Request;
import spark.Response;

/**
 * Spark controller class for image building and listing.
 */
public class ImageBuildController {

    private static final Gson GSON = new GsonBuilder().create();
    private static final Role ADMIN_ROLE = RoleFactory.IMAGE_ADMIN;

    private static final ViewHelper VIEW_HELPER = ViewHelper.INSTANCE;

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

        // Parse optional query string parameters
        model.put("profileId", StringUtils.isNotBlank(req.queryParams("profile")) ?
                Long.parseLong(req.queryParams("profile")) : null);
        model.put("hostId", StringUtils.isNotBlank(req.queryParams("host")) ?
                Long.parseLong(req.queryParams("host")) : null);
        model.put("tag", StringUtils.isNotBlank(req.queryParams("tag")) ?
                req.queryParams("tag") : null);

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
        private LocalDateTime earliest;

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
            if (tag == null || tag.isEmpty()) {
                return "latest";
            }
            else {
                return tag;
            }
        }

        /**
         * @return the earliest
         */
        public LocalDateTime getEarliest() {
            return earliest;
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

        BuildRequest buildRequest = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
                .create().fromJson(request.body(), BuildRequest.class);

        Date scheduleDate = getScheduleDate(buildRequest);

        Long profileId = Long.parseLong(request.params("id"));
        Optional<ImageProfile> maybeProfile =
                ImageProfileFactory.lookupByIdAndOrg(profileId, user.getOrg());

        return maybeProfile.flatMap(ImageProfile::asDockerfileProfile).map(profile -> {
            scheduleBuild(buildRequest.buildHostId, buildRequest.getTag(), profile,
                    scheduleDate, user);
            //TODO: Add action ID as a message parameter
            return GSON.toJson(new JsonResult(true, "build_scheduled"));
        }).orElseGet(
                () -> GSON.toJson(new JsonResult(true, Collections.singletonList(
                        "unknown_error")))
        );
    }

    private static void scheduleBuild(long buildHostId, String tag, ImageProfile profile,
            Date earliest, User user) {
        MinionServer server = ServerFactory.lookupById(buildHostId).asMinionServer().get();

        if (!server.hasEntitlement(EntitlementManager.CONTAINER_BUILD_HOST)) {
            throw new IllegalArgumentException("Server is not a build host.");
        }

        // LOG.debug("Schedule image.build for " + server.getName() + ": " +
        // imageProfile.getLabel() + " " +
        // imageBuildEvent.getTag());

        // Schedule the build
        tag = tag.isEmpty() ? "latest" : tag;
        ImageBuildAction action = ActionManager.scheduleImageBuild(user,
                Collections.singletonList(server.getId()), tag, profile, earliest);
        MessageQueue.publish(new ActionScheduledEventMessage(action, false));

        // Create image info entry
        ImageInfoFactory
                .lookupByName(profile.getLabel(), tag, profile.getTargetStore().getId())
                .ifPresent(ImageInfoFactory::delete);

        ImageInfo info = new ImageInfo();
        info.setName(profile.getLabel());
        info.setVersion(tag);
        info.setStore(profile.getTargetStore());
        info.setOrg(server.getOrg());
        info.setAction(action);
        info.setProfile(profile);
        info.setBuildServer(server);
        info.setChannels(new HashSet<>(profile.getToken().getChannels()));

        // Image arch should be the same as the build host
        info.setImageArch(server.getServerArch());

        // Checksum will be available from inspect

        // Copy custom data values from image profile
        if (profile.getCustomDataValues() != null) {
            profile.getCustomDataValues().forEach(cdv -> info.getCustomDataValues()
                    .add(new ImageInfoCustomDataValue(cdv, info)));
        }

        ImageInfoFactory.save(info);
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

        String tab = req.queryParams("data");

        Optional<ImageOverview> imageInfo =
                ImageInfoFactory.lookupOverviewByIdAndOrg(id, user.getOrg());

        return json(res, imageInfo.map(ImageInfoJson::fromImageInfo).orElse(null));
    }

    /**
     * Gets patches list for a single image info object in JSON
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object getPatches(Request req, Response res, User user) {
        Long id = Long.parseLong(req.params("id"));

        Optional<ImageOverview> imageInfo =
                ImageInfoFactory.lookupOverviewByIdAndOrg(id, user.getOrg());

        return json(res, imageInfo.map(ImageBuildController::getImageInfoWithPatchlist)
                    .orElse(null));
    }

    /**
     * Gets packages list for a single image info object in JSON
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object getPackages(Request req, Response res, User user) {
        Long id = Long.parseLong(req.params("id"));

        Optional<ImageOverview> imageInfo =
                ImageInfoFactory.lookupOverviewByIdAndOrg(id, user.getOrg());

        return json(res, imageInfo.map(ImageBuildController::getImageInfoWithPackageList)
                    .orElse(null));
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
        json.addProperty("modified", VIEW_HELPER.renderDate(imageOverview.getModified()));

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

    private static JsonObject getImageInfoWithPackageList(ImageOverview imageOverview) {
        JsonArray list = new JsonArray();
        imageOverview.getPackages().stream().map(p -> {
            JsonObject json = new JsonObject();
            json.addProperty("name", PackageManager.getNevr(p.getName(), p.getEvr()));
            json.addProperty("arch", p.getArch().getName());
            json.addProperty("installed", VIEW_HELPER.renderDate(p.getInstallTime()));
            return json;
        }).forEach(list::add);

        JsonObject obj = GSON
                .toJsonTree(ImageInfoJson.fromImageInfo(imageOverview), ImageInfoJson.class)
                .getAsJsonObject();

        obj.add("packagelist", list);

        return obj;
    }

    private static JsonObject getImageInfoWithPatchlist(ImageOverview imageOverview) {
        JsonArray list = new JsonArray();
        imageOverview.getPatches().stream().map(p -> {
            JsonObject json = new JsonObject();
            json.addProperty("id", p.getId());
            json.addProperty("name", p.getAdvisoryName());
            json.addProperty("synopsis", p.getSynopsis());
            json.addProperty("type", p.getAdvisoryType());
            JsonArray keywords = new JsonArray();
            p.getKeywords().stream().map(k -> new JsonPrimitive(k.getKeyword()))
                    .forEach(keywords::add);
            json.add("keywords", keywords);
            json.addProperty("update", VIEW_HELPER.renderDate(p.getUpdateDate()));
            return json;
        }).forEach(list::add);

        JsonObject obj = GSON
                .toJsonTree(ImageInfoJson.fromImageInfo(imageOverview), ImageInfoJson.class)
                .getAsJsonObject();

        obj.add("patchlist", list);

        return obj;
    }

    private static Date getScheduleDate(BuildRequest json) {
        ZoneId zoneId = Context.getCurrentContext().getTimezone().toZoneId();
        return Date.from(json.getEarliest().atZone(zoneId).toInstant());
    }
}
