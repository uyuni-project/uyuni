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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.taskomatic.task.gatherer.GathererJob;
import com.suse.manager.gatherer.GathererRunner;
import com.suse.manager.model.gatherer.GathererModule;
import com.suse.manager.webui.utils.gson.ResultJson;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.reader.ReaderException;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import javax.persistence.NoResultException;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory.CONFIG_KUBECONFIG;
import static com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory.CONFIG_PASS;
import static com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory.CONFIG_USER;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;

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
        Map<String, Object> data = new HashMap<>();
        return new ModelAndView(data, "templates/virtualhostmanager/list.jade");
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
        return json(res,
                ResultJson.success(getJsonList(vhms)));
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
        return withVirtualHostManager(req, res, user,
                (vhm) -> getJsonDetails(vhm));
    }

    /**
     * Get the names of the installed gatherer modules.
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object getModules(Request request, Response response, User user) {
        List<String> gathererModuleNames = getGathererModules()
                .entrySet().stream()
                .map(e -> e.getKey())
                .collect(Collectors.toList());
        return json(response, gathererModuleNames);
    }

    /**
     * Get the parameters of the given gatherer module.
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static String getModuleParams(Request request, Response response, User user) {
        String module = request.params("name");
        Optional<GathererModule> gathererModule = getGathererModules()
                .entrySet().stream()
                .filter(e -> module.toLowerCase().equals(e.getKey().toLowerCase()))
                .map(e -> e.getValue())
                .findFirst();
        return json(response,
                ResultJson.success(
                        gathererModule.map(m -> m.getParameters())
                                .orElse(Collections.emptyMap())));
    }

    private static Map<String, GathererModule> getGathererModules() {
        if (gathererModules != null) {
            return gathererModules;
        }
        return gathererRunner.listModules();
    }

    /**
     * Get the nodes of a Virt Host Manager.
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static String getNodes(Request request, Response response, User user) {
        return withVirtualHostManager(request, response, user,
                (vhm) -> getJsonNodes(vhm));
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

        if (errors.isEmpty()) {
            String moduleName = modules.entrySet().stream()
                    .filter(entry -> moduleNameParam.equalsIgnoreCase(entry.getKey()))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);

            VirtualHostManager vhm = getFactory()
                    .createVirtualHostManager(
                            label,
                            user.getOrg(),
                            moduleName,
                            gathererModuleParams);
            getFactory().save(vhm);
            return json(response,
                    ResultJson.success());
        }
        else {
            return json(response,
                    ResultJson.error(errors));
        }
    }

    /**
     * Update an existing VHM.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the json response
     */
    public static String update(Request request, Response response, User user) {
        return withVirtualHostManager(request, response, user, (vhm) -> {
            List<String> errors = new LinkedList<>();
            Map<String, String> gathererModuleParams =
                    paramsFromQueryMap(request.queryMap().toMap());

            if (StringUtils.isBlank(request.queryParams("label")) ||
                    !getFactory().isConfigurationValid(
                            vhm.getGathererModule(),
                            gathererModuleParams,
                            CONFIG_USER, CONFIG_PASS)) {
                errors.add("All fields are mandatory.");
            }
            if (errors.isEmpty()) {
                getFactory().updateVirtualHostManager(vhm,
                        request.queryParams("label"),
                        gathererModuleParams);
                return ResultJson.success();
            }
            return ResultJson.error(errors);
        });
    }

    /**
     * Validates the uploaded kubeconfig file.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the json response
     */
    public static String validateKubeconfig(Request request, Response response,
                                            User user) {
        try {
            List<FileItem> items = parseMultipartRequest(request);

            Optional<FileItem> kubeconfigFile = findFileItem(items, CONFIG_KUBECONFIG);

            if (!kubeconfigFile.isPresent()) {
                throw new IllegalArgumentException("No kubeconfig file found in request");
            }

            Map<String, Object> map;
            try (InputStream fi = kubeconfigFile.get().getInputStream()) {
                map = new Yaml().loadAs(fi, Map.class);
                List<Map<String, Object>> contexts = null;
                if (map.get("contexts") != null && map.get("contexts") instanceof List) {
                    contexts = (List<Map<String, Object>>) map.get("contexts");
                    if (contexts.stream().map(m -> (String) m.get("name")).filter(StringUtils::isEmpty).count() > 1) {
                        throw new IllegalArgumentException(
                                "Only one default (unnamed) context is allowed in a kubeconfig file.");
                    }
                }

                String currentContext = null;
                if (map.get("current-context") instanceof String) {
                    currentContext = (String)map.get("current-context");
                }

                Map<String, Object> json = new HashedMap();
                json.put("contexts", contexts.stream()
                        .map(ctx -> ctx.get("name"))
                        .collect(Collectors.toList()));
                json.put("currentContext", currentContext);
                return json(response, ResultJson.success(json));
            }
        }
        catch (IllegalArgumentException e) {
            LOG.error("Invalid kubeconfig content", e);
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error(
                            "Invalid kubeconfig content: " + e.getMessage()));
        }
        catch (ReaderException e) {
            LOG.error("Invalid kubeconfig file syntax", e);
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error(
                            "Invalid kubeconfig file syntax: " + e.getMessage()));
        }
        catch (FileUploadException e) {
            LOG.error("Kubeconfig upload error", e);
            return json(response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ResultJson.error(e.getMessage()));
        }
        catch (IOException e) {
            LOG.error("Error reading the kubeconfig file", e);
            return json(response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ResultJson.error("Error reading the kubeconfig file"));
        }
    }

    /**
     * Gets the contexts available in the kubeconfig file of a
     * Kubernetes Virtual Host Manager.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the json response
     */
    public static String getKubeconfigContexts(Request request,
                                               Response response, User user) {
        return withVirtualHostManager(request, response, user, (vhm) -> {
            String kubeconfigPath = vhm.getConfigs().stream()
                    .filter(cfg -> CONFIG_KUBECONFIG.equals(cfg.getParameter()))
                    .map(kubeconfig -> kubeconfig.getValue())
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalArgumentException("kubeconfig param not present"));
            List<String> contextNames = Collections.emptyList();
            try {
                try (InputStream fi = new FileInputStream(kubeconfigPath)) {
                    Map<String, Object>  map = new Yaml().loadAs(fi, Map.class);
                    if (map.get("contexts") != null &&
                            map.get("contexts") instanceof List) {
                        List<Map<String, Object>> contexts =
                                (List<Map<String, Object>>)map.get("contexts");
                        contextNames = contexts.stream()
                                .map(ctx -> (String)ctx.get("name"))
                                .collect(Collectors.toList());
                    }
                }
            }
            catch (IOException e) {
                return ResultJson.error("Could not get contexts from file " +
                        kubeconfigPath + ": " + e.getMessage());
            }
            return ResultJson.success(contextNames);
        });
    }

    private static String withVirtualHostManager(Request request,
                                                 Response response,
                                                 User user,
                                                 Function<VirtualHostManager, ResultJson>
                                                         operation) {
        Long storeId;
        try {
            storeId = Long.parseLong(request.params("id"));
        }
        catch (NumberFormatException e) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error("Invalid id"));
        }
        try {
            VirtualHostManager vhm = getFactory().lookupByIdAndOrg(storeId, user.getOrg());
            if (vhm == null) {
                return json(response, HttpStatus.SC_NOT_FOUND,
                        ResultJson.error("Virtual Host Manager not found"));
            }
            ResultJson result = operation.apply(vhm);

            return json(response,
                    result.isSuccess() ?
                            HttpStatus.SC_OK : HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    result);
        }
        catch (IllegalArgumentException e) {
            LOG.error("Invalid parameter: ", e);
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error(e.getMessage()));
        }
    }

    /**
     * Creates a Kubernetes Virtual Host Manager.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the json response
     */
    public static String createKubernetes(Request request, Response response, User user) {
        try {
            List<FileItem> items = parseMultipartRequest(request);

            String label = findStringParam(items, "label")
                    .orElseThrow(() ->
                            new IllegalArgumentException("label param missing"));
            String context = findStringParam(items, "module_context")
                    .orElseThrow(() ->
                            new IllegalArgumentException("context param missing"));
            FileItem kubeconfig = findFileItem(items, "module_" + CONFIG_KUBECONFIG)
                    .orElseThrow(() ->
                            new IllegalArgumentException("kubeconfig param missing"));

            validateKubeconfig(context, kubeconfig);

            try (InputStream kubeconfigIn = kubeconfig.getInputStream()) {
                VirtualHostManagerFactory.getInstance()
                        .createKuberntesVirtualHostManager(
                            label,
                            user.getOrg(),
                            context,
                            kubeconfigIn);
                return json(response, ResultJson.success());
            }
        }
        catch (IllegalArgumentException e) {
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error(e.getMessage()));
        }
        catch (IOException | FileUploadException e) {
            LOG.error("Could not create Kuberentes Virt Host Mgr", e);
            return json(response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ResultJson.error(e.getMessage()));
        }
    }

    /**
     * Updates a Kubernetes Virtual Host Manager.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the json response
     */
    public static String updateKubernetes(Request request,
                                          Response response,
                                          User user) {
        try {
            List<FileItem> items = parseMultipartRequest(request);

            long id = Long.parseLong(findStringParam(items, "id")
                    .orElseThrow(() ->
                            new IllegalArgumentException("id param missing")));
            String label = findStringParam(items, "label")
                    .orElseThrow(() ->
                            new IllegalArgumentException("label param missing"));
            String context = findStringParam(items, "module_context")
                    .orElseThrow(() ->
                            new IllegalArgumentException("context param missing"));
            Optional<FileItem> kubeconfig = findFileItem(items,
                    "module_" + CONFIG_KUBECONFIG);
            Optional<InputStream> kubeconfigIn = Optional.empty();
            if (kubeconfig.isPresent()) {
                validateKubeconfig(context, kubeconfig.get());
                kubeconfigIn = Optional.of(kubeconfig.get().getInputStream());
            }

            VirtualHostManager vhm = getFactory().lookupByIdAndOrg(id, user.getOrg());
            VirtualHostManagerFactory.getInstance()
                    .updateKuberntesVirtualHostManager(vhm, label, context, kubeconfigIn);
            return json(response, ResultJson.success());
        }
        catch (IllegalArgumentException e) {
            LOG.error("Error updating Kubernetes Virtual host manage", e);
            return json(response, HttpStatus.SC_BAD_REQUEST,
                    ResultJson.error(e.getMessage()));
        }
        catch (IOException | FileUploadException e) {
            LOG.error("Error updating Kubernetes Virtual host manage", e);
            return json(response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ResultJson.error(e.getMessage()));
        }
        catch (NoResultException e) {
            LOG.error("Virtual host manager not found", e);
            return json(response, HttpStatus.SC_NOT_FOUND,
                    ResultJson.error("Virtual Host Manager not found"));
        }
    }

    private static List<FileItem> parseMultipartRequest(Request request)
            throws FileUploadException {
        DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
        ServletContext servletContext = request.raw().getServletContext();
        File repository = (File) servletContext
                .getAttribute("javax.servlet.context.tempdir");
        fileItemFactory.setRepository(repository);
        return new ServletFileUpload(fileItemFactory).parseRequest(request.raw());
    }

    private static void validateKubeconfig(String context,
                                           FileItem kubeconfig) throws IOException {
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
                .orElseThrow(() ->
                        new IllegalArgumentException("user missing for context " +
                                context));
        ((List<Map<String, Object>>)map.get("users"))
                .stream()
                .filter(usr -> currentUser.equals(usr.get("name")))
                .map(usr -> (Map<String, Object>)usr.get("user"))
                .filter(data -> data.get("client-certificate") != null ||
                        data.get("client-key") != null)
                .findAny()
                .ifPresent(data -> {
                    throw new IllegalStateException(
                            "client certificate and key must be embedded for user '" +
                            currentUser + "'");
                });
    }

    private static Optional<String> findStringParam(List<FileItem> items, String name) {
        return findParamItem(items, name)
                .map(item -> item.getString());
    }

    private static Optional<FileItem> findParamItem(List<FileItem> items, String name) {
        return items.stream()
                .filter(item -> item.isFormField())
                .filter(item -> name.equals(item.getFieldName()))
                .findFirst();
    }

    private static Optional<FileItem> findFileItem(List<FileItem> items, String name) {
        return items.stream()
                .filter(item -> !item.isFormField())
                .filter(item -> name.equals(item.getFieldName()))
                .filter(item -> item.getSize() > 0)
                .findFirst();
    }

    /**
     * Processes a DELETE request to delete multiple vhms
     *
     * @param req the request object
     * @param res the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static Object delete(Request req, Response res, User user) {
        return withVirtualHostManager(req, res, user, vhm -> {
            getFactory().delete(vhm);
            return ResultJson.success();
        });
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
        return withVirtualHostManager(request, response, user, (vhm) -> {
            String label = vhm.getLabel();
            String message = null;
            Map<String, String> params = new HashMap<>();
            params.put(GathererJob.VHM_LABEL, label);
            try {
                new TaskomaticApi()
                        .scheduleSingleSatBunch(user, "gatherer-matcher-bunch", params);
            }
            catch (TaskomaticApiException e) {
                message  = "Problem when message Taskomatic job: " + e.getMessage();
                LOG.error(message, e);
                return ResultJson.error(message);
            }

            return ResultJson.success();
        });
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

    private static ResultJson getJsonDetails(VirtualHostManager vhm) {
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
        return ResultJson.success(json);
    }


    private static ResultJson getJsonNodes(VirtualHostManager vhm) {
        JsonArray list = new JsonArray();

        vhm.getServers().stream().map(srv -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "server");
            obj.addProperty("id", srv.getId());
            obj.addProperty("name", srv.getName());
            obj.addProperty("cpuSockets", srv.getCpu().getNrsocket());
            obj.addProperty("memory", srv.getRam());
            obj.addProperty("os", srv.getOs() + " " + srv.getRelease());
            obj.addProperty("arch", srv.getServerArch().getName());
            return obj;
        })
                .forEach(json -> list.add(json));

        vhm.getNodes().stream().map(node -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "node");
            obj.addProperty("id", node.getId());
            obj.addProperty("name", node.getName());
            obj.addProperty("cpuSockets", node.getCpuSockets());
            obj.addProperty("memory", node.getRam());
            obj.addProperty("os", node.getOs() + " " + node.getOsVersion());
            obj.addProperty("arch", node.getNodeArch().getName());
            return obj;
        })
                .forEach(json -> list.add(json));

        return ResultJson.success(list);
    }
}
