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
 * SPDX-License-Identifier: GPL-2.0-only
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
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.UyuniGeneralException;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.utils.gson.ProxyConfigUpdateJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.proxy.get.ProxyConfigGetFacade;
import com.suse.proxy.get.ProxyConfigGetFacadeImpl;
import com.suse.proxy.update.ProxyConfigUpdateFacade;
import com.suse.proxy.update.ProxyConfigUpdateFacadeImpl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller class providing backend code for proxy configuration specific pages.
 */
public class ProxyConfigurationController {

    private static final Logger LOG = LogManager.getLogger(ProxyConfigurationController.class);

    private final SystemManager systemManager;
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
     */
    public ProxyConfigurationController(SystemManager systemManagerIn) {
        this(
                systemManagerIn,
                new ProxyConfigUpdateFacadeImpl(),
                new ProxyConfigGetFacadeImpl()
        );
    }

    /**
     * Create a new controller instance
     *
     * @param systemManagerIn the system manager
     * @param proxyConfigUpdateFacadeIn the proxy config update facade
     * @param proxyConfigGetFacadeIn the proxy config get facade
     */
    public ProxyConfigurationController(
            SystemManager systemManagerIn,
            ProxyConfigUpdateFacade proxyConfigUpdateFacadeIn,
            ProxyConfigGetFacade proxyConfigGetFacadeIn
    ) {
        this.systemManager = systemManagerIn;
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
        return new ModelAndView(
                proxyConfigGetFacade.getFormData(user, server, GlobalInstanceHolder.SYSTEM_ENTITLEMENT_MANAGER),
                "templates/minion/proxy-config.jade");
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
            proxyConfigUpdateFacade.update(data, systemManager, user);
            return result(response, ResultJson.success("Proxy configuration applied"));
        }
        catch (RhnRuntimeException e) {
            LOG.error("Failed to apply proxy configuration to minion", e);
            return internalServerError(response, e.getMessage());
        }
        catch (UyuniGeneralException e) {
            LOG.error("Failed to apply proxy configuration to minion", e);
            return badRequest(response, e.getErrorMessages());
        }

    }
}
