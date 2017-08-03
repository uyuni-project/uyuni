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

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.taskomatic.task.gatherer.GathererJob;
import com.suse.manager.gatherer.GathererRunner;
import com.suse.manager.model.gatherer.GathererModule;
import com.suse.manager.webui.utils.FlashScopeHelper;
import com.suse.manager.webui.utils.gson.JsonResult;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.reader.ReaderException;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;

import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory.CONFIG_PASS;
import static com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory.CONFIG_USER;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.utils.Json.GSON;

/**
 * Controller class providing backend code for the VHM pages.
 */
public class VirtualHostManagerController {

    private static final Logger LOG = Logger.getLogger(VirtualHostManagerController.class);

    private VirtualHostManagerController() { }

    private static GathererRunner gathererRunner = new GathererRunner();

    private static VirtualHostManagerFactory factory;

    private static Map<String, GathererModule> gathererModules;

    /**
     * Displays a list of VHMs.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView list(Request request, Response response, User user) {
        return new ModelAndView(new HashMap<>(), "virtualhostmanager/list.jade");
    }

    /**
     * Processes a GET request to get a list of all vhms
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object get(Request req, Response res, User user) {
        List<VirtualHostManager> vhms =
                getFactory().listVirtualHostManagers(user.getOrg());
        return json(res, getJsonList(vhms));
    }

    /**
     * Processes a GET request to get a single image store object
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object getSingle(Request req, Response res, User user) {
        Long storeId;
        try {
            storeId = Long.parseLong(req.params("id"));
        }
        catch (NumberFormatException e) {
            Spark.halt(HttpStatus.SC_NOT_FOUND);
            return null;
        }

        try {
            VirtualHostManager vhm = getFactory().lookupByIdAndOrg(storeId, user.getOrg());
            return json(res, getJsonDetails(vhm));
        }
        catch (NoResultException e) {
            Spark.halt(HttpStatus.SC_NOT_FOUND);
            return null;
        }
    }

    public static String getModuleParams(Request request, Response response, User user) {
        String module = request.params("name");
        Optional<GathererModule> gathererModule = getGathererModules()
                .entrySet().stream()
                .filter(e -> module.toLowerCase().equals(e.getKey().toLowerCase()))
                .map(e -> e.getValue())
                .findFirst();
        return json(response, gathererModule.map(m -> m.getParameters())
                .orElse(Collections.emptyMap()));
    }


    private static Map<String, GathererModule> getGathererModules() {
        if (gathererModules != null) {
            return gathererModules;
        }
        return gathererRunner.listModules();
    }

    /**
     * Creates a new VHM.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return string containing the json response
     */
    public static String create(Request request, Response response, User user) {
        List<String> errors = new LinkedList<>();

        String label = request.queryParams("label");
        String moduleNameParam = request.queryParams("module");
        Map<String, String> gathererModuleParams =
                paramsFromQueryMap(request.queryMap().toMap());

        if (StringUtils.isEmpty(label)) {
            errors.add("Label must be specified.");
        }
        if (getFactory().lookupByLabel(label) != null) {
            errors.add("Virtual Host Manager with given label already exists.");
        }
        Map<String, GathererModule> modules = getGathererModules();
        if (!getFactory().isConfigurationValid(
                moduleNameParam, gathererModuleParams, modules)) {
            errors.add("All fields are mandatory.");
        }
        String moduleName = modules.entrySet().stream()
                .filter(entry -> moduleNameParam.equalsIgnoreCase(entry.getKey()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (errors.isEmpty()) {
            VirtualHostManager vhm = getFactory().createVirtualHostManager(
                    label, user.getOrg(), moduleName, gathererModuleParams);
            getFactory().save(vhm);
            return json(response, Collections.singletonMap("success", true));
        }
        else {
            return json(response, Collections.singletonMap("errors", errors));
        }
    }

    /**
     * Update an existing VHM.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return string containing the json response
     */
    public static String update(Request request, Response response, User user) {
        List<String> errors = new LinkedList<>();
        try {
            long id = Long.parseLong(request.params("id"));
            Optional<VirtualHostManager> vhmOpt = getFactory().lookupById(id);
            VirtualHostManager vhm = vhmOpt
                    .orElseThrow(() ->
                            new EntityNotFoundException(
                                    "No virtual host manager found for id=" + id));

            Map<String, String> gathererModuleParams =
                    paramsFromQueryMap(request.queryMap().toMap());

            if (StringUtils.isBlank(request.queryParams("label")) ||
                    !getFactory().isConfigurationValid(
                            vhm.getGathererModule(), gathererModuleParams,
                            CONFIG_USER, CONFIG_PASS)) {
                errors.add("All fields are mandatory.");
            }
            if (errors.isEmpty()) {
                getFactory().updateVirtualHostManager(vhm,
                        request.queryParams("label"),
                        gathererModuleParams);
                return json(response, Collections.singletonMap("success", true));
            }
        }
        catch (NumberFormatException e) {
            errors.add("Invalid id parameter.");
        }
        catch (EntityNotFoundException e) {
            errors.add(e.getMessage());
        }
        return json(response,
                Collections.singletonMap("errors",
                        errors));
    }

    public static String prevalidateKubeconfig(Request request, Response response,
                                               User user) {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // Configure a repository (to ensure a secure temp location is used)
        ServletContext servletContext = request.raw().getServletContext();
        File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
        factory.setRepository(repository);
        ServletFileUpload fileUpload = new ServletFileUpload(factory);
        try {
            List<FileItem> items = fileUpload.parseRequest(request.raw());
            Optional<FileItem> kubeconfigFile = items.stream()
                    .filter(item -> !item.isFormField() && "kubeconfig".equals(item.getFieldName()))
                    .findFirst();
            if (!kubeconfigFile.isPresent()) {
                throw new IllegalArgumentException("No kubeconfig file found in request");
            }
            Map<String, Object> map;
            try (InputStream fi = kubeconfigFile.get().getInputStream()) {
                map = new Yaml().loadAs(fi, Map.class);
                List<Map<String, Object>> contexts = null;
                String currentContext = null;
                if (map.get("contexts") != null && map.get("contexts") instanceof List) {
                    contexts = (List<Map<String, Object>>)map.get("contexts");
                    contexts.forEach(ctx -> {
                        if (ctx.get("name") == null) {
                            throw new IllegalArgumentException("'name' key missing from kube-config/contexts list");
                        }
                    });
                }
                if (map.get("current-context") instanceof String) {
                    currentContext = (String)map.get("current-context");
                }
                Map<String, Object> json = new HashedMap();
                json.put("contexts", contexts.stream()
                        .map(ctx -> ctx.get("name"))
                        .collect(Collectors.toList()));
                json.put("currentContext", currentContext);
                return json(response, json);
            }

            // TODO validate embedded certificate-authority client-certificate client-key
        }
        catch (FileUploadException e) {
            LOG.error(e);
            return json(response,
                    Collections.singletonMap("errors",
                            Arrays.asList(e.getMessage())));
        }
        catch (IOException e) {
            LOG.error("Error reading the kubeconfig file", e);
            return json(response,
                    Collections.singletonMap("errors",
                            Arrays.asList("Error reasing the kubeconfig file")));
        }
        catch (ReaderException e) {
            return json(response,
                    Collections.singletonMap("errors",
                            Arrays.asList("Invalid YAML syntax: " + e.getMessage())));
        }
        catch (IllegalArgumentException e) {
            return json(response,
                    Collections.singletonMap("errors",
                            Arrays.asList(e.getMessage())));
        }
    }

    public static String createKubernetes(Request request, Response response, User user) {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // Configure a repository (to ensure a secure temp location is used)
        ServletContext servletContext = request.raw().getServletContext();
        File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
        factory.setRepository(repository);
        ServletFileUpload fileUpload = new ServletFileUpload(factory);
        try {
            List<FileItem> items = fileUpload.parseRequest(request.raw());

            String label =  items.stream()
                    .filter(item -> item.isFormField())
                    .filter(item -> "label".equals(item.getFieldName()))
                    .map(item -> item.getString())
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("label param missing"));
            String context = items.stream()
                    .filter(item -> item.isFormField())
                    .filter(item -> "context".equals(item.getFieldName()))
                    .map(item -> item.getString())
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("context param missing"));
            FileItem kubeconfig = items.stream().filter(item -> !item.isFormField())
                    .filter(item -> "kubeconfig".equals(item.getFieldName()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("kubeconfig param missing"));

            Map<String, Object> map;
            try (InputStream fi = kubeconfig.getInputStream()) {
                map = new Yaml().loadAs(fi, Map.class);
            }
            List<Map<String, Object>> contexts = (List<Map<String, Object>>)map.get("contexts");
            String currentUser = contexts.stream()
                    .filter(ctx -> context.equals(ctx.get("name")))
                    .map(ctx -> (Map<String, String>)ctx.get("context"))
                    .map(attrs -> attrs.get("user"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("kubeconfig param missing"));
            ((List<Map<String, Object>>)map.get("users"))
                    .stream()
                    .filter(usr -> currentUser.equals(usr.get("name")))
                    .map(usr -> (Map<String, Object>)usr.get("user"))
                    .filter(data -> data.get("client-certificate") != null || data.get("client-key") != null)
                    .findAny()
                    .ifPresent(data -> {
                        throw new IllegalStateException("client certificate and key must be embedded for user '" + currentUser + "'");
                    });

            try (InputStream kubeconfigIn = kubeconfig.getInputStream()) {
                VirtualHostManager virtualHostManager = VirtualHostManagerFactory.getInstance().createKuberntesVirtualHostManager(
                        label,
                        user.getOrg(),
                        context,
                        kubeconfigIn
                        );
                getFactory().save(virtualHostManager);
                return json(response, Collections.singletonMap("success", true));
            }

        }
        catch (IOException | IllegalArgumentException | FileUploadException e) {
            LOG.error(e);
            return json(response, Collections.singletonMap("errors", Arrays.asList(e.getMessage())));
        }
    }

    /**
     * Processes a POST request to delete multiple vhms
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
            Spark.halt(HttpStatus.SC_BAD_REQUEST);
            return null;
        }

        List<VirtualHostManager> vhms =
                getFactory().lookupByIdsAndOrg(ids, user.getOrg());
        if (vhms.size() < ids.size()) {
            return json(res, new JsonResult<>(false, "not_found"));
        }

        vhms.forEach(getFactory()::delete);
        return json(res, new JsonResult<>(true, vhms.size()));
    }

    /**
     * Schedule a refresh to a VHM.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return dummy string to satisfy spark
     */
    public static Object refresh(Request request, Response response, User user) {
        Long id = Long.parseLong(request.params("id"));
        VirtualHostManager virtualHostManager = getFactory().lookupByIdAndOrg(id,
                user.getOrg());
        String label = virtualHostManager.getLabel();
        String message = null;
        Map<String, String> params = new HashMap<>();
        params.put(GathererJob.VHM_LABEL, label);
        try {
            new TaskomaticApi()
                    .scheduleSingleSatBunch(user, "gatherer-matcher-bunch", params);
        }
        catch (TaskomaticApiException e) {
            message  = "Problem when running Taskomatic job: " + e.getMessage();
        }
        if (message == null) {
            message = "Refresh for Virtual Host Manager with label '" +
                    label + "' was triggered.";
        }
        FlashScopeHelper.flash(request, message);
        response.redirect("/rhn/manager/vhms");
        return "";
    }

    /**
     * Creates VHM gatherer module params from the query map.
     *
     * @param queryMap the query map
     * @return the map
     */
    private static Map<String, String> paramsFromQueryMap(Map<String, String[]> queryMap) {
        return queryMap.entrySet().stream()
                .filter(keyVal -> keyVal.getKey().startsWith("module_"))
                .collect(Collectors.toMap(
                        keyVal -> keyVal.getKey().replaceFirst("module_", ""),
                        keyVal -> keyVal.getValue()[0]));
    }

    /**
     * Gets the VHM factory.
     *
     * @return the factory
     */
    private static VirtualHostManagerFactory getFactory() {
        if (factory != null) {
            return factory;
        }
        return VirtualHostManagerFactory.getInstance();
    }

    /**
     * For testing only!
     * @param mockFactory - the factory
     */
    public static void setMockFactory(VirtualHostManagerFactory mockFactory) {
        factory = mockFactory;
    }

    /**
     * For testing only!
     * @param gathererModulesIn to set
     */
    public static void setGathererModules(Map<String, GathererModule> gathererModulesIn) {
        gathererModules = gathererModulesIn;
    }

    /**
     * Changes the gatherer runner instance, used in tests.
     * @param gathererRunnerIn a new gatherer runner
     */
    public static void setGathererRunner(GathererRunner gathererRunnerIn) {
        gathererRunner = gathererRunnerIn;
    }

    /**
     * Creates a list of JSON objects for a list of {@link VirtualHostManager} instances
     *
     * @param vhmList the vhm list
     * @return the list of JSON objects
     */
    private static List<JsonObject> getJsonList(List<VirtualHostManager> vhmList) {
        return vhmList.stream().map(imageStore -> {
            JsonObject json = new JsonObject();
            json.addProperty("id", imageStore.getId());
            json.addProperty("label", imageStore.getLabel());
            json.addProperty("orgName", imageStore.getOrg().getName());
            json.addProperty("gathererModule", imageStore.getGathererModule());
            return json;
        }).collect(Collectors.toList());
    }

    private static JsonObject getJsonDetails(VirtualHostManager vhm) {
        JsonObject json = new JsonObject();
        json.addProperty("id", vhm.getId());
        json.addProperty("label", vhm.getLabel());
        json.addProperty("orgName", vhm.getOrg().getName());
        json.addProperty("gathererModule", vhm.getGathererModule());

        JsonObject config = new JsonObject();
        vhm.getConfigs().forEach(c -> config.addProperty(c.getParameter(), c.getValue()));
        json.add("config", config);
        if (vhm.getCredentials() != null) {
            JsonObject credentials = new JsonObject();
            credentials.addProperty("username", vhm.getCredentials().getUsername());
            json.add("credentials", credentials);
        }
        return json;
    }
}
