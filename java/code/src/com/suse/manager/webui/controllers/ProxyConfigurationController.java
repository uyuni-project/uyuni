/*
 * Copyright (c) 2025 SUSE LLC
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
import static com.suse.manager.webui.utils.SparkApplicationHelper.result;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withDocsLocale;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserAndServer;
import static com.suse.utils.Predicates.isAbsent;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.RhnGeneralException;
import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.api.ParseException;
import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.utils.gson.ProxyConfigUpdateJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.proxy.ProxyContainerImagesEnum;
import com.suse.proxy.ProxyRegistryUtils;
import com.suse.proxy.ProxyRegistryUtilsImpl;
import com.suse.proxy.RegistryUrl;
import com.suse.proxy.get.ProxyConfigGetFacade;
import com.suse.proxy.get.ProxyConfigGetFacadeImpl;
import com.suse.proxy.update.ProxyConfigUpdateFacade;
import com.suse.proxy.update.ProxyConfigUpdateFacadeImpl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller class providing backend code for proxy configuration specific pages.
 */
public class ProxyConfigurationController {

    private static final Logger LOG = LogManager.getLogger(ProxyConfigurationController.class);

    public static final String IS_EXACT_TAG = "isExact";
    public static final String REGISTRY_URL_TAG = "registryUrl";

    private final SystemManager systemManager;
    private final SaltApi saltApi;
    private final ProxyRegistryUtils proxyRegistryUtils;
    private final ProxyConfigUpdateFacade proxyConfigUpdateFacade;
    private final ProxyConfigGetFacade proxyConfigGetFacade;

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .serializeNulls()
            .create();

    /**
     * Create a new controller instance with default facades implementations
     *
     * @param systemManagerIn the system manager
     * @param saltApiIn       the salt API
     */
    public ProxyConfigurationController(SystemManager systemManagerIn, SaltApi saltApiIn) {
        this(
                systemManagerIn,
                saltApiIn,
                new ProxyRegistryUtilsImpl(),
                new ProxyConfigUpdateFacadeImpl(),
                new ProxyConfigGetFacadeImpl()
        );
    }

    /**
     * Create a new controller instance
     *
     * @param systemManagerIn the system manager
     * @param saltApiIn the salt API
     * @param proxyRegistryUtilsIn the proxy registry utils
     * @param proxyConfigUpdateFacadeIn the proxy config update facade
     * @param proxyConfigGetFacadeIn the proxy config get facade
     */
    public ProxyConfigurationController(
            SystemManager systemManagerIn,
            SaltApi saltApiIn,
            ProxyRegistryUtils proxyRegistryUtilsIn,
            ProxyConfigUpdateFacade proxyConfigUpdateFacadeIn,
            ProxyConfigGetFacade proxyConfigGetFacadeIn
    ) {
        this.systemManager = systemManagerIn;
        this.saltApi = saltApiIn;
        this.proxyRegistryUtils = proxyRegistryUtilsIn;
        this.proxyConfigUpdateFacade = proxyConfigUpdateFacadeIn;
        this.proxyConfigGetFacade = proxyConfigGetFacadeIn;
    }

    /**
     * Invoked from Router. Initialize routes for Proxy Views.
     *
     * @param proxyController instance to register.
     * @param jade            Jade template engine
     */
    public void initRoutes(ProxyConfigurationController proxyController, JadeTemplateEngine jade) {
        get("/manager/systems/details/proxy-config",
                withCsrfToken(withDocsLocale(withUserAndServer(this::getProxyConfiguration))),
                jade
        );
        post("/manager/systems/details/proxy-config", withUser(this::updateProxyConfiguration));
        post("/manager/systems/details/proxy-config/registry-url", withUser(this::checkRegistryUrl));
    }

    /**
     * Displays the form to create a new container-based proxy configuration
     *
     * @param request  the request object
     * @param response the response object
     * @param user     the current user
     * @param server   the current server
     * @return the ModelAndView object to render the page
     */
    public ModelAndView getProxyConfiguration(Request request, Response response, User user, Server server) {
        return new ModelAndView(proxyConfigGetFacade.getFormData(user, server), "templates/minion/proxy-config.jade");
    }

    /**
     * Check a given registry URL and return associated tags (or an error message).
     * The request is expected to contain a registry URL and a flag indicating if the URL is exact.
     *
     * @param request  the request object
     * @param response the response object
     * @param user     the user
     * @return the tags or an error message
     */
    public Object checkRegistryUrl(Request request, Response response, User user) {
        try {
            JsonObject jsonObject = new Gson().fromJson(request.body(), JsonObject.class);
            String registryUrl = jsonObject.get(REGISTRY_URL_TAG).getAsString();

            return jsonObject.has(IS_EXACT_TAG) && jsonObject.get(IS_EXACT_TAG).getAsBoolean() ?
                    getTagsFromRegistry(response, registryUrl) :
                    getCommonTagsFromRegistry(response, registryUrl);
        }
        catch (Exception e) {
            LOG.error("Failed to check registry URL", e);
            return result(response, ResultJson.error("Failed to check registry URL"));
        }
    }


    /**
     * Get the tags from the registry when the URL for a specific image.
     * Eg:
     * - https://registry.opensuse.org/uyuni/proxy-httpd
     *
     * @param response            the response object
     * @param registryUrlAsString the registry URL for a specific image
     * @return the json with either the tags or with an error message
     */
    public Object getTagsFromRegistry(Response response, String registryUrlAsString) {
        try {
            RegistryUrl registryUrl = new RegistryUrl(registryUrlAsString);
            List<String> tags = proxyRegistryUtils.getTags(registryUrl);
            if (tags == null) {
                LOG.debug("No tags found on registry {}", registryUrlAsString);
                return result(response, ResultJson.error("No tags found on registry"));
            }
            return result(response, ResultJson.success(tags));
        }
        catch (Exception e) {
            LOG.error("Failed downloading tags from registry {} {}", registryUrlAsString, e);
            return result(response, ResultJson.error("Failed to download tags from registry"));
        }
    }

    /**
     * Retrieves the common tags among the proxy images from the given base registry URL.
     *
     * @param response        the response object
     * @param baseRegistryUrl the base registry URL
     * @return the json with either the list of common tags or with an error message
     */
    private Object getCommonTagsFromRegistry(Response response, String baseRegistryUrl)
            throws URISyntaxException, RhnRuntimeException, ParseException {
        RegistryUrl registryUrl = new RegistryUrl(baseRegistryUrl);

        List<String> repositories = proxyRegistryUtils.getRepositories(registryUrl);
        if (repositories.isEmpty()) {
            LOG.debug("No repositories found on registry {}", baseRegistryUrl);
            return result(response, ResultJson.error("No repositories found on registry"));
        }

        // Check if all proxy images are present in the catalog
        Set<String> repositorySet = new HashSet<>(repositories);
        Set<String> proxyImageList = new HashSet<>(ProxyContainerImagesEnum.values().length);
        String pathPrefix = registryUrl.getPath().substring(1);
        for (ProxyContainerImagesEnum proxyImage : ProxyContainerImagesEnum.values()) {
            proxyImageList.add(pathPrefix + "/" + proxyImage.getImageName());
        }

        if (!repositorySet.containsAll(proxyImageList)) {
            return result(response, ResultJson.error("Cannot find all images in catalog"));
        }

        // Collect common tags among proxy images
        Set<String> commonTags = null;
        for (ProxyContainerImagesEnum proxyImage : ProxyContainerImagesEnum.values()) {
            RegistryUrl imageRegistryUrl = new RegistryUrl(registryUrl.getUrl() + "/" + proxyImage.getImageName());
            List<String> tags = proxyRegistryUtils.getTags(imageRegistryUrl);

            if (tags == null || tags.isEmpty()) {
                LOG.debug("No tags found on registry {}", imageRegistryUrl);
                return result(response, ResultJson.error("No common tags found among proxy images"));
            }

            Set<String> tagSet = new HashSet<>(tags);
            if (commonTags == null) {
                commonTags = new HashSet<>(tagSet);
            }
            else {
                commonTags.retainAll(tagSet);
                if (commonTags.isEmpty()) {
                    break;
                }
            }
        }

        if (isAbsent(commonTags)) {
            LOG.debug("No common tags found among proxy images using registryUrl {}", baseRegistryUrl);
            return result(response, ResultJson.error("No common tags found among proxy images"));
        }

        List<String> commonTagsList = new ArrayList<>(commonTags);
        Collections.sort(commonTagsList);
        return result(response, ResultJson.success(commonTagsList));
    }

    /**
     * Convert a minion to a proxy.
     *
     * @param request  the request object
     * @param response the response object
     * @param user     the user
     * @return the result of the conversion
     */
    public String updateProxyConfiguration(Request request, Response response, User user) {
        ProxyConfigUpdateJson data =
                GSON.fromJson(request.body(), new TypeToken<ProxyConfigUpdateJson>() { }.getType());

        try {
            proxyConfigUpdateFacade.update(data, systemManager, saltApi, user);
            return result(response, ResultJson.success("Proxy configuration applied"));
        }
        catch (RhnRuntimeException e) {
            LOG.error("Failed to apply proxy configuration to minion", e);
            return internalServerError(response, e.getMessage());
        }
        catch (RhnGeneralException e) {
            LOG.error("Failed to apply proxy configuration to minion", e);
            return badRequest(response, e.getErrorMessages());
        }

    }
}
