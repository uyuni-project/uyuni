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

import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.image.ImageOverview;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.context.Context;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.webui.utils.ViewHelper;
import com.suse.manager.webui.utils.gson.ImageInfoJson;
import com.suse.manager.webui.utils.gson.JsonResult;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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
    private static Logger log = Logger.getLogger(ImageBuildController.class);

    private ImageBuildController() { }

    /**
     * rebuild image
     * @param req the request
     * @param res the response
     * @param user the user
     * @return ModelAndView for build page
     */
    public static ModelAndView rebuild(Request req, Response res, User user) {
        Map<String, Object> model = new HashMap<>();

        ImageInfo imageInfo = ImageInfoFactory.lookupByIdAndOrg(
                Long.parseLong(req.params("id")), user.getOrg()).get();

        // Parse optional query string parameters
        model.put("profileId", imageInfo.getProfile().getProfileId());
        model.put("hostId", imageInfo.getAction().getServerActions().stream().findFirst()
                .orElse(null));
        model.put("version", imageInfo.getVersion());

        return new ModelAndView(model, "content_management/build.jade");
    }

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
        model.put("version", StringUtils.isNotBlank(req.queryParams("version")) ?
                req.queryParams("version") : null);

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
     * redirect to activation key page
     * @param req the request
     * @param res the response
     * @param user the user
     * @return return Object
     */
    public static Object activationKey(Request req, Response res, User user) {
        long id = Long.parseLong(req.params("id"));
        ImageInfo imageInfo = ImageInfoFactory.lookupByIdAndOrg(id, user.getOrg()).get();
        res.redirect("/rhn/activationkeys/Edit.do?tid=" + imageInfo.getProfile()
            .getToken().getId());
        return "";
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
        private String version;
        private LocalDateTime earliest;

        /**
         * @return the build host id
         */
        public long getBuildHostId() {
            return buildHostId;
        }

        /**
         * @return the version
         */
        public String getVersion() {
            if (version == null || version.isEmpty()) {
                return "latest";
            }
            else {
                return version;
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
            try {
                ImageInfoFactory.scheduleBuild(buildRequest.buildHostId,
                        buildRequest.getVersion(), profile, scheduleDate, user);
                // TODO: Add action ID as a message parameter
                return GSON.toJson(new JsonResult(true, "build_scheduled"));
            }
            catch (TaskomaticApiException e) {
                log.error("could not schedule image build:");
                log.error(e);
                return GSON.toJson(new JsonResult(false, "taskomatic_error"));
            }
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
        if (imageOverview.getInstalledPackages() != null) {
            json.addProperty("installedPackages", imageOverview.getInstalledPackages());
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
