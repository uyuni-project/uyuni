/*
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

import static com.suse.manager.webui.utils.SparkApplicationHelper.badRequest;
import static com.suse.manager.webui.utils.SparkApplicationHelper.internalServerError;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.notFound;
import static com.suse.manager.webui.utils.SparkApplicationHelper.result;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withImageAdmin;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static com.suse.utils.Json.GSON;
import static spark.Spark.post;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.salt.build.ImageBuildAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.image.ImageOverview;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.image.OSImageStoreUtils;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.gatherer.GathererRunner;
import com.suse.manager.kubernetes.KubernetesManager;
import com.suse.manager.model.gatherer.GathererModule;
import com.suse.manager.model.kubernetes.ImageUsage;
import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.controllers.utils.ImagesUtil;
import com.suse.manager.webui.errors.NotFoundException;
import com.suse.manager.webui.utils.MinionActionUtils;
import com.suse.manager.webui.utils.ViewHelper;
import com.suse.manager.webui.utils.gson.ImageInfoJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.utils.gson.ScheduledRequestJson;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.jade.JadeTemplateEngine;

/**
 * Spark controller class for image building and listing.
 */
public class ImageBuildController {

    private static final Role ADMIN_ROLE = RoleFactory.IMAGE_ADMIN;

    private static final ViewHelper VIEW_HELPER = ViewHelper.INSTANCE;
    private static Logger log = LogManager.getLogger(ImageBuildController.class);

    private final KubernetesManager kubernetesManager;

    /**
     * Spark controller class for image building and listing.
     * @param kubernetesManagerIn instance for getting information from a kubernetes cluster.
     */
    public ImageBuildController(KubernetesManager kubernetesManagerIn) {
        this.kubernetesManager = kubernetesManagerIn;
    }

    /**
     * Invoked from Router. Initialize routes for Systems Views.
     *
     * @param imageBuildController instance to register
     * @param jade the Jade engine to use to render the pages
     */
    public static void initRoutes(JadeTemplateEngine jade, ImageBuildController imageBuildController) {
        Spark.get("/manager/cm/build", withCsrfToken(withUser(imageBuildController::buildView)),
                jade);
        Spark.get("/manager/cm/import", withCsrfToken(withUser(imageBuildController::importView)),
                jade);
        Spark.get("/manager/cm/rebuild/:id",
                withCsrfToken(withUser(imageBuildController::rebuild)), jade);

        Spark.get("/manager/api/cm/build/hosts/:type", withUser(imageBuildController::getBuildHosts));
        post("/manager/api/cm/build/:id", withImageAdmin(imageBuildController::build));

        Spark.get("/manager/cm/images", withUserPreferences(withCsrfToken(withUser(imageBuildController::listView))),
                jade);

        Spark.get("/manager/api/cm/images", withUser(imageBuildController::list));
        Spark.get("/manager/api/cm/images/:id", withUser(imageBuildController::get));
        Spark.get("/manager/api/cm/clusters", withUser(imageBuildController::getClusterList));
        Spark.get("/manager/api/cm/runtime/:clusterId",
                withUser(imageBuildController::getRuntimeSummaryAll));
        Spark.get("/manager/api/cm/runtime/:clusterId/:id",
                withUser(imageBuildController::getRuntimeSummary));
        Spark.get("/manager/api/cm/runtime/details/:clusterId/:id",
                withUser(imageBuildController::getRuntimeDetails));
        Spark.get("/manager/api/cm/images/patches/:id",
                withUser(imageBuildController::getPatches));
        Spark.get("/manager/api/cm/images/packages/:id",
                withUser(imageBuildController::getPackages));
        Spark.get("/manager/api/cm/images/buildlog/:id",
                withUser(imageBuildController::getBuildLog));
        post("/manager/api/cm/images/inspect/:id",
                withImageAdmin(imageBuildController::inspect));
        post("/manager/api/cm/images/delete", withImageAdmin(ImageBuildController::delete));
        post("/manager/api/cm/images/import",
                withImageAdmin(imageBuildController::importImage));
    }

    /**
     * rebuild image
     * @param req the request
     * @param res the response
     * @param user the user
     * @return ModelAndView for build page
     */
    public ModelAndView rebuild(Request req, Response res, User user) {
        Map<String, Object> model = new HashMap<>();
        MinionController.addActionChains(user, model);
        Optional<ImageInfo> imageInfo;
        try {
            imageInfo = ImageInfoFactory
                    .lookupByIdAndOrg(Long.parseLong(req.params("id")), user.getOrg());
        }
        catch (NumberFormatException e) {
            imageInfo = Optional.empty();
        }

        return imageInfo.map(i -> {
            String hostId = i.getBuildAction().getServerActions().stream().findFirst()
                    .map(ServerAction::getServerId).map(id -> "&host=" + id).orElse("");
            res.redirect("/rhn/manager/cm/build?version=" + i.getVersion() + hostId +
                    "&profile=" + i.getProfile().getProfileId());
            return new ModelAndView(model, "templates/content_management/build.jade");
        }).orElseGet(() -> {
            throw new NotFoundException();
        });
    }

    /**
     * Returns a view to display build page
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public ModelAndView buildView(Request req, Response res, User user) {
        Map<String, Object> model = new HashMap<>();
        MinionController.addActionChains(user, model);
        // Parse optional query string parameters
        try {
            model.put("profileId", StringUtils.isNotBlank(req.queryParams("profile")) ?
                    Long.parseLong(req.queryParams("profile")) :
                    null);
            model.put("hostId", StringUtils.isNotBlank(req.queryParams("host")) ?
                    Long.parseLong(req.queryParams("host")) :
                    null);
            model.put("version", StringUtils.isNotBlank(req.queryParams("version")) ?
                    req.queryParams("version") :
                    null);
        }
        catch (NumberFormatException e) {
            Spark.halt(HttpStatus.SC_BAD_REQUEST);
            return null;
        }

        return new ModelAndView(model, "templates/content_management/build.jade");
    }

    /**
     * Returns a view to display import image form page
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public ModelAndView importView(Request req, Response res, User user) {
        Map<String, Object> model = new HashMap<>();
        return new ModelAndView(model, "templates/content_management/import.jade");
    }

    /**
     * Gets a JSON list of Container Build Host entitled systems
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public Object getBuildHosts(Request req, Response res, User user) {
        ServerGroup sg;
        String buildType = req.params("type");

        if (EntitlementManager.CONTAINER_BUILD_HOST_ENTITLED.equals(buildType)) {
            sg = ServerGroupFactory.lookupEntitled(EntitlementManager.CONTAINER_BUILD_HOST, user.getOrg());
        }
        else if (EntitlementManager.OSIMAGE_BUILD_HOST_ENTITLED.equals(buildType)) {
            sg = ServerGroupFactory.lookupEntitled(EntitlementManager.OSIMAGE_BUILD_HOST, user.getOrg());
        }
        else {
            return badRequest(res, "invalid_build_type");
        }

        return json(res, getServerStreamJson(SystemManager.systemsInGroupShort(sg.getId())), new TypeToken<>() { });
    }

    /**
     * Returns a view to display image info list
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the model and view
     */
    public ModelAndView listView(Request req, Response res, User user) {
        Map<String, Object> model = new HashMap<>();

        // Parse optional image id
        if (StringUtils.isNotBlank(req.params("id"))) {
            try {
                Long profileId = Long.parseLong(req.params("id"));
                model.put("id", profileId);
            }
            catch (NumberFormatException e) {
                throw new NotFoundException();
            }
        }
        else {
            model.put("id", null);
        }

        if (new File(OSImageStoreUtils.getOSImageStorePathForOrg(user.getOrg())).exists()) {
            model.put("osImageStoreUrl", OSImageStoreUtils.getOSImageStoreRelativeURI(user.getOrg()));
        }

        model.put("isAdmin", user.hasRole(ADMIN_ROLE));
        Map<String, GathererModule> modules = new GathererRunner().listModules();
        model.put("isRuntimeInfoEnabled", ImagesUtil.isImageRuntimeInfoEnabled());
        return new ModelAndView(model, "templates/content_management/view.jade");
    }

    /**
     * Creates a list of JSON objects for a list of {@link SystemOverview}
     * instances, having only 'id' and 'name' properties
     *
     * @param systemList the system overview list
     * @return the list of JSON objects
     */
    private List<JsonObject> getServerStreamJson(List<SystemOverview> systemList) {
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
    public static class BuildRequest extends ScheduledRequestJson {
        private long buildHostId;
        private String version;

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
            return StringUtils.defaultIfBlank(version, "latest");
        }
    }

    /**
     * Inspect request object
     */
    public static class InspectRequest extends ScheduledRequestJson {
        private long imageId;

        /**
         * @return the image id
         */
        public long getImageId() {
            return imageId;
        }
    }

    /**
     * Import request object
     */
    public static class ImportRequest extends ScheduledRequestJson {
        private long buildHostId;
        private String name;
        private String version;
        private String activationKey;
        private long storeId;

        /**
         * @return the build host id
         */
        public long getBuildHostId() {
            return buildHostId;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the version
         */
        public String getVersion() {
            return StringUtils.defaultIfBlank(version, "latest");
        }

        /**
         * @return the activation key
         */
        public String getActivationKey() {
            return activationKey;
        }

        /**
         * @return the store id
         */
        public long getStoreId() {
            return storeId;
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
    public Object build(Request request, Response response, User user) {
        BuildRequest buildRequest;
        try {
            buildRequest = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
                    .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
                    .create().fromJson(request.body(), BuildRequest.class);
        }
        catch (JsonParseException e) {
            return badRequest(response, "");
        }

        Date scheduleDate = MinionActionUtils.getScheduleDate(buildRequest.getEarliest());
        ActionChain actionChain = MinionActionUtils.getActionChain(buildRequest.getActionChain(), user);

        Long profileId = Long.parseLong(request.params("id"));
        Optional<ImageProfile> maybeProfile =
                ImageProfileFactory.lookupByIdAndOrg(profileId, user.getOrg());


        return maybeProfile.map(profile -> {
            try {
                ImageBuildAction action = ActionChainManager.scheduleImageBuild(buildRequest.buildHostId,
                        buildRequest.getVersion(), profile, scheduleDate, actionChain, user);
                return result(response, ResultJson.success(actionChain != null ?
                        actionChain.getId() : action.getId()), new TypeToken<>() { });
            }
            catch (TaskomaticApiException e) {
                log.error("Could not schedule image build:", e);
                return internalServerError(response, "taskomatic_error");
            }
        }).orElseGet(
                () -> notFound(response, "not_found")
        );
    }

    /**
     * Schedules an (re)inspect for an image
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public Object inspect(Request req, Response res, User user) {
        InspectRequest inspectRequest;
        try {
            inspectRequest = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
                    .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
                    .create().fromJson(req.body(), InspectRequest.class);
        }
        catch (JsonParseException e) {
            return badRequest(res, "");
        }

        Date scheduleDate = MinionActionUtils.getScheduleDate(inspectRequest.getEarliest());

        Long imageId = Long.parseLong(req.params("id"));

        return ImageInfoFactory.lookupByIdAndOrg(imageId, user.getOrg()).map(info -> {
            if (info.getImageType().equals("kiwi")) {
                // Manually scheduling inspect is not allowed for Kiwi images
                return badRequest(res, "");
            }
            try {
                ImageInfoFactory.scheduleInspect(info, scheduleDate, user);
                return result(res, ResultJson.successMessage("inspect_scheduled"), new TypeToken<>() { });
            }
            catch (TaskomaticApiException e) {
                log.error("Could not schedule image inspect:", e);
                return internalServerError(res, "taskomatic_error");
            }
        }).orElseGet(
                () -> notFound(res, "not_found")
        );
    }

    /**
     * Schedules the import of an external image
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public Object importImage(Request req, Response res, User user) {
        ImportRequest data;
        try {
            data = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
                    .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
                    .create().fromJson(req.body(), ImportRequest.class);
        }
        catch (JsonParseException e) {
            return badRequest(res, "invalid_id");
        }

        Date scheduleDate = MinionActionUtils.getScheduleDate(data.getEarliest());
        return ImageStoreFactory.lookupByIdAndOrg(data.getStoreId(), user.getOrg())
                .map(store -> {
                    try {
                        Optional<ActivationKey> key = Optional.ofNullable(
                                ActivationKeyFactory.lookupByKey(data.getActivationKey()));

                        ImageInfoFactory.scheduleImport(data.getBuildHostId(),
                                data.getName(), data.getVersion(), store,
                                key.map(ActivationKey::getChannels), scheduleDate, user);
                        return result(res, ResultJson.successMessage("import_scheduled"), new TypeToken<>() { });
                    }
                    catch (TaskomaticApiException e) {
                        log.error("Could not schedule image import", e);
                        return internalServerError(res, "taskomatic_error");
                    }
                    catch (IllegalArgumentException e) {
                        log.error("Could not schedule image import", e);
                        return badRequest(res, e.getMessage());
                    }
                }).orElseGet(() -> result(res, ResultJson.error("not_found"), new TypeToken<>() { }));
    }

    /**
     * Lists image profiles in JSON
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public Object list(Request req, Response res, User user) {
        List<JsonObject> result =
                getImageInfoSummaryList(ImageInfoFactory.listImageOverviews(user.getOrg()));

        return json(res, result, new TypeToken<>() { });
    }

    /**
     * Gets a single image info object in JSON
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public Object get(Request req, Response res, User user) {
        Long id;
        try {
            id = Long.parseLong(req.params("id"));
        }
        catch (NumberFormatException e) {
            throw new NotFoundException();
        }

        Optional<ImageOverview> imageInfo =
                ImageInfoFactory.lookupOverviewByIdAndOrg(id, user.getOrg());

        return json(res, imageInfo.map(ImageInfoJson::fromImageInfo).orElse(null), new TypeToken<>() { });
    }

    /**
     * Gets patches list for a single image info object in JSON
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public Object getPatches(Request req, Response res, User user) {
        Long id;
        try {
            id = Long.parseLong(req.params("id"));
        }
        catch (NumberFormatException e) {
            throw new NotFoundException();
        }

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
    public Object getPackages(Request req, Response res, User user) {
        Long id;
        try {
            id = Long.parseLong(req.params("id"));
        }
        catch (NumberFormatException e) {
            throw new NotFoundException();
        }

        Optional<ImageOverview> imageInfo =
                ImageInfoFactory.lookupOverviewByIdAndOrg(id, user.getOrg());

        return json(res, imageInfo.map(ImageBuildController::getImageInfoWithPackageList)
                    .orElse(null));
    }

    /**
     * Gets build log for single image info object in JSON
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public Object getBuildLog(Request req, Response res, User user) {
        Long id;
        try {
            id = Long.parseLong(req.params("id"));
        }
        catch (NumberFormatException e) {
            throw new NotFoundException();
        }

        Optional<ImageOverview> imageInfo =
                ImageInfoFactory.lookupOverviewByIdAndOrg(id, user.getOrg());

        return json(res, imageInfo.map(ImageBuildController::getBuildLogJson)
                    .orElse(null));
    }

    private static JsonObject getBuildLogJson(ImageOverview imageOverview) {
        JsonObject json = GSON
                .toJsonTree(ImageInfoJson.fromImageInfo(imageOverview), ImageInfoJson.class)
                .getAsJsonObject();
        ImageInfoFactory.lookupById(imageOverview.getId()).ifPresent(imageInfo -> {
            json.addProperty("buildlog", imageInfo.getBuildLog());
        });

        return json;
    }

    /**
     * Gets a list of registered clusters in JSON
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public Object getClusterList(Request req, Response res, User user) {
        JsonArray clusterList = new JsonArray();
        VirtualHostManagerFactory.getInstance().listVirtualHostManagers(user.getOrg())
                .stream().filter(vhm -> VirtualHostManagerFactory.KUBERNETES
                        .equals(vhm.getGathererModule()))
                .forEach(vhm -> {
                    JsonObject cluster = new JsonObject();
                    cluster.addProperty("id", vhm.getId());
                    cluster.addProperty("label", vhm.getLabel());
                    clusterList.add(cluster);
                });

        // JSON structure: [{ id: cluster_id, label: cluster_label }, ...]
        return json(res, clusterList);
    }

    /**
     * Gets runtime summary for a single image in JSON
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public Object getRuntimeSummary(Request req, Response res, User user) {
        Long id, clusterId;
        try {
            id = Long.parseLong(req.params("id"));
            clusterId = Long.parseLong(req.params("clusterId"));
        }
        catch (NumberFormatException e) {
            return badRequest(res, "Invalid id");
        }

        VirtualHostManager cluster = VirtualHostManagerFactory.getInstance()
                .lookupByIdAndOrg(clusterId, user.getOrg());

        try {
            Set<ImageUsage> usages = kubernetesManager.getImagesUsage(cluster);
            Optional<ImageOverview> imageInfo =
                    ImageInfoFactory.lookupOverviewByIdAndOrg(id, user.getOrg());

            return imageInfo
                    .map(overview -> result(res, HttpStatus.SC_OK,
                            ResultJson.success(getRuntimeOverviewJson(usages, overview)), new TypeToken<>() { }))
                    .orElseGet(() -> {
                        log.error("ImageOverview id={} not found", id);
                        return result(res, HttpStatus.SC_NOT_FOUND, ResultJson.error("image_overview_not_found"),
                                new TypeToken<>() { });
                    });
        }
        catch (NoSuchElementException e) {
            log.error("Could not retrieve cluster info", e);
            // Cluster is not available
            return notFound(res, "cluster_info_err");
        }
    }

    /**
     * Gets runtime summary for all images in JSON
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public Object getRuntimeSummaryAll(Request req, Response res, User user) {
        Long clusterId;
        try {
            clusterId = Long.parseLong(req.params("clusterId"));
        }
        catch (NumberFormatException e) {
            return badRequest(res, "Invalid id");
        }

        JsonObject obj = new JsonObject();
        VirtualHostManager cluster = VirtualHostManagerFactory.getInstance()
                .lookupByIdAndOrg(clusterId, user.getOrg());
        try {
            Set<ImageUsage> usages = kubernetesManager.getImagesUsage(cluster);
            ImageInfoFactory.listImageOverviews(user.getOrg()).forEach(overview -> obj
                    .add(overview.getId().toString(),
                            getRuntimeOverviewJson(usages, overview)));
            return result(res, ResultJson.success(obj), new TypeToken<>() { });
        }
        catch (NoSuchElementException e) {
            // Cluster is not available
            log.error("Could not retrieve cluster info", e);
            return notFound(res, "cluster_info_err");
        }
    }

    /**
     * Gets detailed runtime info for a single image info object in JSON
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public Object getRuntimeDetails(Request req, Response res, User user) {
        Long id, clusterId;
        try {
            id = Long.parseLong(req.params("id"));
            clusterId = Long.parseLong(req.params("clusterId"));
        }
        catch (NumberFormatException e) {
            return badRequest(res, "Invalid id");
        }

        VirtualHostManager cluster = VirtualHostManagerFactory.getInstance()
                .lookupByIdAndOrg(clusterId, user.getOrg());
        try {
            Set<ImageUsage> usages = kubernetesManager.getImagesUsage(cluster);
            Optional<ImageOverview> imageInfo =
                    ImageInfoFactory.lookupOverviewByIdAndOrg(id, user.getOrg());

            return imageInfo
                    .map(overview -> result(res, ResultJson.success(getRuntimeDetailsJson(usages, overview)),
                            new TypeToken<>() { }))
                    .orElse(notFound(res, "cluster_info_not_found"));
        }
        catch (NoSuchElementException e) {
            // Cluster is not available
            log.error("Could not retrieve cluster info", e);
            return notFound(res, "cluster_info_err");
        }
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
        List<Long> ids;
        try {
            ids = Arrays.asList(GSON.fromJson(req.body(), Long[].class));
        }
        catch (JsonParseException e) {
            return badRequest(res, "");
        }

        List<ImageInfo> images = ImageInfoFactory.lookupByIdsAndOrg(ids, user.getOrg());
        if (images.size() < ids.size()) {
            return result(res, ResultJson.error("not_found"), new TypeToken<>() { });
        }

        images.forEach(info -> ImageInfoFactory.deleteWithObsoletes(info, GlobalInstanceHolder.SALT_API));
        return result(res, ResultJson.success(images.size()), new TypeToken<>() { });
    }

    private static JsonObject getImageInfoSummary(ImageOverview imageOverview) {
        JsonObject json = new JsonObject();
        json.addProperty("id", imageOverview.getId());
        json.addProperty("name", imageOverview.getName());
        json.addProperty("version", imageOverview.getVersion());
        json.addProperty("type", imageOverview.getImageType());
        json.addProperty("external", imageOverview.isExternalImage());
        json.addProperty("obsolete", imageOverview.isObsolete());
        json.addProperty("revision", imageOverview.getCurrRevisionNum());
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

        imageOverview.getBuildServerAction()
                .ifPresentOrElse(ba -> json.addProperty("statusId", ba.getStatus().getId()),
                                 () -> json.addProperty("statusId",
                                            imageOverview.isBuilt() ? ActionFactory.STATUS_COMPLETED.getId() :
                                                                      ActionFactory.STATUS_FAILED.getId())
                                 );

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

    private static JsonObject getRuntimeDetailsJson(Set<ImageUsage> imagesUsage,
                                                      ImageOverview overview) {
        JsonObject obj = new JsonObject();
        Optional<ImageUsage> usage = getImageUsage(imagesUsage, overview);
        Map<String, JsonArray> clusterInfo = new HashMap<>();

        usage.ifPresent(u -> u.getContainerInfos().forEach(c -> {
            String vhmLabel = c.getVirtualHostManager().getLabel();

            JsonArray podList = clusterInfo.get(vhmLabel);
            if (podList == null) {
                podList = new JsonArray();
                clusterInfo.put(vhmLabel, podList);
            }

            JsonObject podInfo = new JsonObject();
            podInfo.addProperty("name", c.getPodName());
            podInfo.addProperty("namespace", c.getPodNamespace());
            podInfo.addProperty("statusId",
                    c.getRuntimeStatus(overview.getCurrRevisionNum()));

            podList.add(podInfo);
        }));

        JsonObject instanceInfo = new JsonObject();
        clusterInfo.forEach(instanceInfo::add);
        obj.add("clusters", instanceInfo);

        return obj;
    }

    private static JsonObject getRuntimeOverviewJson(Set<ImageUsage> imagesUsage,
                                                     ImageOverview overview) {
        Map<String, Integer> clusterCounts = new HashMap<>();

        // Specifies the accumulated most severe status
        // across all containers for an image
        final int[] imageStatus = {ImageUsage.RUNTIME_NOINSTANCE};

        Optional<ImageUsage> usage = getImageUsage(imagesUsage, overview);

        usage.ifPresent(u -> u.getContainerInfos().forEach(c -> {
            String vhmLabel = c.getVirtualHostManager().getLabel();
            Integer count = clusterCounts.get(vhmLabel);
            if (count == null) {
                count = 0;
            }
            clusterCounts.put(vhmLabel, count + 1);

            int containerStatus = c.getRuntimeStatus(overview.getCurrRevisionNum());
            imageStatus[0] =
                    imageStatus[0] > containerStatus ? imageStatus[0] : containerStatus;
        }));

        JsonObject obj = new JsonObject();
        JsonObject instanceInfo = new JsonObject();
        obj.addProperty("runtimeStatus", imageStatus[0]);
        clusterCounts.forEach(instanceInfo::addProperty);
        obj.add("instances", instanceInfo);

        return obj;
    }

    /**
     * Gets usage info for a single image.
     *
     * @param overview the overview
     * @return the image usage
     */
    private static Optional<ImageUsage> getImageUsage(Set<ImageUsage> imagesUsage,
                                                      ImageOverview overview) {
        return imagesUsage.stream()
                .filter(u -> u.getImageInfo().getId().equals(overview.getId())).findFirst();
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
}
