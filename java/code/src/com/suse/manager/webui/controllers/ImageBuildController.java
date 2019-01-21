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
import static com.suse.utils.Json.GSON;

import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.salt.build.ImageBuildAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.image.ImageOverview;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.image.ImageStoreFactory;
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

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.suse.manager.gatherer.GathererRunner;
import com.suse.manager.kubernetes.KubernetesManager;
import com.suse.manager.model.gatherer.GathererModule;
import com.suse.manager.model.kubernetes.ImageUsage;
import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.controllers.utils.ImagesUtil;
import com.suse.manager.webui.errors.NotFoundException;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.MinionActionUtils;
import com.suse.manager.webui.utils.ViewHelper;
import com.suse.manager.webui.utils.gson.ImageInfoJson;
import com.suse.manager.webui.utils.gson.ResultJson;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

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

/**
 * Spark controller class for image building and listing.
 */
public class ImageBuildController {

    private static final Role ADMIN_ROLE = RoleFactory.IMAGE_ADMIN;

    private static final ViewHelper VIEW_HELPER = ViewHelper.INSTANCE;
    private static Logger log = Logger.getLogger(ImageBuildController.class);

    private static final KubernetesManager K8S_MANAGER = new KubernetesManager();

    private ImageBuildController() {
        K8S_MANAGER.setSaltService(SaltService.INSTANCE);
    }

    /**
     * rebuild image
     * @param req the request
     * @param res the response
     * @param user the user
     * @return ModelAndView for build page
     */
    public static ModelAndView rebuild(Request req, Response res, User user) {
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
    public static ModelAndView buildView(Request req, Response res, User user) {
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
    public static ModelAndView importView(Request req, Response res, User user) {
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
    public static Object getBuildHosts(Request req, Response res, User user) {
        ServerGroup sg;
        String buildType = req.params("type");

        if (EntitlementManager.CONTAINER_BUILD_HOST_ENTITLED.equals(buildType)) {
            sg = ServerGroupFactory.lookupEntitled(EntitlementManager.CONTAINER_BUILD_HOST, user.getOrg());
        }
        else if (EntitlementManager.OSIMAGE_BUILD_HOST_ENTITLED.equals(buildType)) {
            sg = ServerGroupFactory.lookupEntitled(EntitlementManager.OSIMAGE_BUILD_HOST, user.getOrg());
        }
        else {
            return json(res, HttpStatus.SC_BAD_REQUEST, ResultJson.error("invalid_build_type"));
        }

        return json(res, getServerStreamJson(SystemManager.systemsInGroupShort(sg.getId())));
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
    private static List<JsonObject> getServerStreamJson(List<SystemOverview> systemList) {
        return systemList.stream().map(s -> {
            JsonObject json = new JsonObject();
            json.addProperty("id", s.getId());
            json.addProperty("name", s.getName());
            return json;
        }).collect(Collectors.toList());
    }

    /**
     * Generic Schedule request
     */
    public static class ScheduleRequest {
        private LocalDateTime earliest;
        private Optional<String> actionChain = Optional.empty();

        /**
         * @return the earliest
         */
        public LocalDateTime getEarliest() {
            return earliest;
        }

        /**
         * @return actionChain to get
         */
        public Optional<String> getActionChain() {
            return actionChain;
        }
    }

    /**
     * Build request object
     */
    public static class BuildRequest extends ScheduleRequest {
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
    public static class InspectRequest extends ScheduleRequest {
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
    public static class ImportRequest extends ScheduleRequest {
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
    public static Object build(Request request, Response response, User user) {
        BuildRequest buildRequest;
        try {
            buildRequest = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
                    .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
                    .create().fromJson(request.body(), BuildRequest.class);
        }
        catch (JsonParseException e) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error());
        }

        Date scheduleDate = MinionActionUtils.getScheduleDate(buildRequest.getEarliest());

        ActionChain actionChain = buildRequest.getActionChain()
                .filter(StringUtils::isNotEmpty)
                .map(label -> ActionChainFactory.getOrCreateActionChain(label, user))
                .orElse(null);


        Long profileId = Long.parseLong(request.params("id"));
        Optional<ImageProfile> maybeProfile =
                ImageProfileFactory.lookupByIdAndOrg(profileId, user.getOrg());


        return maybeProfile.map(profile -> {
            try {
                ImageBuildAction action = ActionChainManager.scheduleImageBuild(buildRequest.buildHostId,
                        buildRequest.getVersion(), profile, scheduleDate, actionChain, user);
                return json(response, ResultJson.success(actionChain != null ?
                        actionChain.getId() : action.getId()));
            }
            catch (TaskomaticApiException e) {
                log.error("Could not schedule image build:", e);
                return json(response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ResultJson.error("taskomatic_error"));
            }
        }).orElseGet(
                () -> json(response, HttpStatus.SC_NOT_FOUND,
                        ResultJson.error("not_found"))
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
    public static Object inspect(Request req, Response res, User user) {
        InspectRequest inspectRequest;
        try {
            inspectRequest = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
                    .create().fromJson(req.body(), InspectRequest.class);
        }
        catch (JsonParseException e) {
            return json(res, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error());
        }

        Date scheduleDate = MinionActionUtils.getScheduleDate(inspectRequest.getEarliest());

        Long imageId = Long.parseLong(req.params("id"));

        return ImageInfoFactory.lookupByIdAndOrg(imageId, user.getOrg()).map(info -> {
            if (info.getImageType().equals("kiwi")) {
                // Manually scheduling inspect is not allowed for Kiwi images
                return json(res, HttpStatus.SC_BAD_REQUEST, ResultJson.error());
            }
            try {
                ImageInfoFactory.scheduleInspect(info, scheduleDate, user);
                return json(res, ResultJson.successMessage("inspect_scheduled"));
            }
            catch (TaskomaticApiException e) {
                log.error("Could not schedule image inspect:", e);
                return json(res, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ResultJson.error("taskomatic_error"));
            }
        }).orElseGet(
                () -> json(res, HttpStatus.SC_NOT_FOUND,
                    ResultJson.error("not_found"))
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
    public static Object importImage(Request req, Response res, User user) {
        ImportRequest data;
        try {
            data = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
                    .create().fromJson(req.body(), ImportRequest.class);
        }
        catch (JsonParseException e) {
            return json(res, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("invalid_id"));
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
                        return json(res, ResultJson.successMessage("import_scheduled"));
                    }
                    catch (TaskomaticApiException e) {
                        log.error("Could not schedule image import", e);
                        return json(res,
                                HttpStatus.SC_INTERNAL_SERVER_ERROR,
                                ResultJson.error("taskomatic_error"));
                    }
                    catch (IllegalArgumentException e) {
                        log.error("Could not schedule image import", e);
                        return json(res,
                                HttpStatus.SC_BAD_REQUEST,
                                ResultJson.error(e.getMessage()));
                    }
                }).orElseGet(() -> json(res, ResultJson.error("not_found")));
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
        Long id;
        try {
            id = Long.parseLong(req.params("id"));
        }
        catch (NumberFormatException e) {
            throw new NotFoundException();
        }

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
    public static Object getPackages(Request req, Response res, User user) {
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
     * Gets a list of registered clusters in JSON
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object getClusterList(Request req, Response res, User user) {
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
    public static Object getRuntimeSummary(Request req, Response res, User user) {
        Long id, clusterId;
        try {
            id = Long.parseLong(req.params("id"));
            clusterId = Long.parseLong(req.params("clusterId"));
        }
        catch (NumberFormatException e) {
            return json(res, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Invalid id"));
        }

        VirtualHostManager cluster = VirtualHostManagerFactory.getInstance()
                .lookupByIdAndOrg(clusterId, user.getOrg());

        try {
            Set<ImageUsage> usages = getImagesUsage(cluster);
            Optional<ImageOverview> imageInfo =
                    ImageInfoFactory.lookupOverviewByIdAndOrg(id, user.getOrg());

            ResultJson result = imageInfo
                    .map(overview -> ResultJson.success(
                            getRuntimeOverviewJson(usages, overview)))
                    .orElseGet(() -> {
                        log.error("ImageOverview id=" + id + " not found");
                        return ResultJson.error("image_overview_not_found");
                    });

            return json(res,
                    result.isSuccess() ? HttpStatus.SC_OK : HttpStatus.SC_NOT_FOUND,
                    result);
        }
        catch (NoSuchElementException e) {
            log.error("Could not retrieve cluster info", e);
            // Cluster is not available
            return json(res, HttpStatus.SC_NOT_FOUND,
                    ResultJson.error("cluster_info_err"));
        }
    }

    private static Set<ImageUsage> getImagesUsage(VirtualHostManager cluster) {
        return K8S_MANAGER.getImagesUsage(cluster);
    }

    /**
     * Gets runtime summary for all images in JSON
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object getRuntimeSummaryAll(Request req, Response res, User user) {
        Long clusterId;
        try {
            clusterId = Long.parseLong(req.params("clusterId"));
        }
        catch (NumberFormatException e) {
            return json(res, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Invalid id"));
        }

        JsonObject obj = new JsonObject();
        VirtualHostManager cluster = VirtualHostManagerFactory.getInstance()
                .lookupByIdAndOrg(clusterId, user.getOrg());
        try {
            Set<ImageUsage> usages = getImagesUsage(cluster);
            ImageInfoFactory.listImageOverviews(user.getOrg()).forEach(overview -> obj
                    .add(overview.getId().toString(),
                            getRuntimeOverviewJson(usages, overview)));
            return json(res, ResultJson.success(obj));
        }
        catch (NoSuchElementException e) {
            // Cluster is not available
            log.error("Could not retrieve cluster info", e);
            return json(res, HttpStatus.SC_NOT_FOUND,
                    ResultJson.error("cluster_info_err"));
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
    public static Object getRuntimeDetails(Request req, Response res, User user) {
        Long id, clusterId;
        try {
            id = Long.parseLong(req.params("id"));
            clusterId = Long.parseLong(req.params("clusterId"));
        }
        catch (NumberFormatException e) {
            return json(res, HttpStatus.SC_BAD_REQUEST, ResultJson.error(
                    "Invalid id"));
        }

        VirtualHostManager cluster = VirtualHostManagerFactory.getInstance()
                .lookupByIdAndOrg(clusterId, user.getOrg());
        try {
            Set<ImageUsage> usages = getImagesUsage(cluster);
            Optional<ImageOverview> imageInfo =
                    ImageInfoFactory.lookupOverviewByIdAndOrg(id, user.getOrg());

            ResultJson result = imageInfo
                    .map(overview -> ResultJson.success(
                            getRuntimeDetailsJson(usages, overview)))
                    .orElse(ResultJson.error("cluster_info_not_found"));

            return json(res,
                    result.isSuccess() ? HttpStatus.SC_OK : HttpStatus.SC_NOT_FOUND,
                    result);
        }
        catch (NoSuchElementException e) {
            // Cluster is not available
            log.error("Could not retrieve cluster info", e);
            return json(res, HttpStatus.SC_NOT_FOUND, ResultJson.error(
                    "cluster_info_err"));
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
            return json(res,
                    HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error());
        }

        List<ImageInfo> images = ImageInfoFactory.lookupByIdsAndOrg(ids, user.getOrg());
        if (images.size() < ids.size()) {
            return json(res, ResultJson.error("not_found"));
        }

        images.forEach(ImageInfoFactory::delete);
        return json(res, ResultJson.success(images.size()));
    }

    private static JsonObject getImageInfoSummary(ImageOverview imageOverview) {
        JsonObject json = new JsonObject();
        json.addProperty("id", imageOverview.getId());
        json.addProperty("name", imageOverview.getName());
        json.addProperty("version", imageOverview.getVersion());
        json.addProperty("type", imageOverview.getImageType());
        json.addProperty("external", imageOverview.isExternalImage());
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
                .ifPresent(ba -> json.addProperty("statusId", ba.getStatus().getId()));

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
