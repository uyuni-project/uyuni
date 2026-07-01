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
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withDocsLocale;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.SystemsExistException;

import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.ssl.SSLCertManager;
import com.suse.manager.webui.utils.SparkApplicationHelper;
import com.suse.manager.webui.utils.gson.ProxyContainerConfigJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller class providing backend code for proxy specific pages.
 */
public class ProxyController {

    private final SystemManager systemManager;

    /**
     * Create a new controller instance
     *
     * @param systemManagerIn the system manager
     */
    public ProxyController(SystemManager systemManagerIn) {
        systemManager = systemManagerIn;
    }

    // Logger for this class
    private static final Logger LOG = LogManager.getLogger(ProxyController.class);

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .serializeNulls()
            .create();

    /**
     * Invoked from Router. Initialize routes for Proxy Views.
     * @param proxyController instance to register.
     * @param jade Jade template engine
     */
    public void initRoutes(ProxyController proxyController, JadeTemplateEngine jade) {
        get("/manager/proxy/container-config",
                withCsrfToken(withDocsLocale(withUser(proxyController::containerConfig))), jade);
        post("/manager/api/proxy/container-config", withUser(this::generateContainerConfig));
        get("/manager/api/proxy/container-config/:filename", withUser(proxyController::containerConfigFile));
    }

    /**
     * Displays the form to create a new container-based proxy configuration
     *
     * @param requestIn the request object
     * @param responseIn the response object
     * @param userIn the user
     * @return the ModelAndView object to render the page
     */
    public ModelAndView containerConfig(Request requestIn, Response responseIn, User userIn) {
        Map<String, Object> data = new HashMap<>();
        data.put("noSSL", !ConfigDefaults.get().isSsl());
        return new ModelAndView(data, "templates/proxy/container-config.jade");
    }

    /**
     * Create the proxy containers configuration.
     *
     * @param request the request object
     * @param response the response object
     * @param user the user
     *
     * @return the config file name
     */
    public String generateContainerConfig(Request request, Response response, User user) {
        ProxyContainerConfigJson data = GSON.fromJson(request.body(),
                new TypeToken<ProxyContainerConfigJson>() { }.getType());
        if (!data.isValid()) {
            return badRequest(response, "Invalid Input Data");
        }
        try {
            byte[] config = systemManager.createProxyContainerConfig(user, data.getProxyFqdn(),
                    data.getProxyPort(), data.getServerFqdn(), data.getMaxCache(), data.getEmail(),
                    data.getRootCA(), data.getIntermediateCAs(), data.getProxyCertPair(),
                    data.getCaPair(), data.getCaPassword(), data.getCertData(), new SSLCertManager());
            String filename = data.getProxyFqdn().split("\\.")[0];
            request.session().attribute(filename + "-config.tar.gz", config);

            return SparkApplicationHelper.json(response, filename + "-config.tar.gz");
        }
        catch (SystemsExistException e) {
            String msg = String.format("Cannot create proxy as an existing system has FQDN '%s'", data.getProxyFqdn());
            LOG.error(msg);
            return internalServerError(response, msg);
        }
        catch (RhnRuntimeException e) {
            LOG.error("Failed to generate proxy configuration", e);
            return badRequest(response, e.getMessage());
        }
    }

    /**
     * Return the config file stored in the session
     *
     *
     * @param request the request object
     * @param response the response object
     * @param user the user
     *
     * @return the config file
     */
    public byte[] containerConfigFile(Request request, Response response, User user) {
        String filename = request.params("filename");
        if (!request.session().attributes().contains(filename) || !filename.endsWith("-config.tar.gz")) {
            return badRequest(response, "Configuration file wasn't generated").getBytes();
        }
        Object config = request.session().attribute(filename);
        if (config instanceof byte[] data) {
            request.session().removeAttribute(filename);
            response.header("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            response.header("Content-Length", Integer.toString(data.length));
            response.type("application/gzip");
            return data;
        }
        return badRequest(response, "Invalid configuration file data").getBytes();
    }
}
