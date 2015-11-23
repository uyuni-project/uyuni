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

import com.redhat.rhn.common.security.CSRFTokenValidator;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.gatherer.GathererRunner;
import com.suse.manager.model.gatherer.GathererModule;
import com.suse.manager.webui.utils.FlashScopeHelper;

import org.apache.commons.lang.StringUtils;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller class providing backend code for the VHM pages.
 */
public class VirtualHostManagerController {

    private VirtualHostManagerController() { }

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
        data.put("csrf_token", CSRFTokenValidator.getToken(request.session().raw()));
        data.put("virtualHostManagers", getFactory()
                .listVirtualHostManagers(user.getOrg()));
        data.put("modules", new GathererRunner().listModules().keySet());
        data.put("info", FlashScopeHelper.flash(request));
        return new ModelAndView(data, "virtualhostmanager/list.jade");
    }

    /**
     * Displays a certain VHM.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView show(Request request, Response response, User user) {
        Long id = Long.parseLong(request.params("id"));

        Map<String, Object> data = new HashMap<>();
        data.put("virtualHostManager", getFactory().lookupByIdAndOrg(id,
                user.getOrg()));
        data.put("csrf_token", CSRFTokenValidator.getToken(request.session().raw()));
        data.put("satAdmin", user.hasRole(RoleFactory.SAT_ADMIN));

        return new ModelAndView(data, "virtualhostmanager/show.jade");
    }

    /**
     * Displays a page to add a VHM.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView add(Request request, Response response, User user) {
        String module = request.queryParams("module");
        GathererModule gathererModule = new GathererRunner().listModules().get(module);

        Map<String, Object> data = new HashMap<>();
        data.put("virtualHostManager",
                getFactory().createVirtualHostManager("", user.getOrg(), module,
                        gathererModule.getParameters()));
        data.put("csrf_token", CSRFTokenValidator.getToken(request.session().raw()));
        return new ModelAndView(data, "virtualhostmanager/add.jade");
    }

    /**
     * Creates a new VHM.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView create(Request request, Response response, User user) {
        List<String> errors = new LinkedList<>();

        String label = request.queryParams("label");
        String gathererModule = request.queryParams("module");
        Map<String, String> gathererModuleParams =
                paramsFromQueryMap(gathererModule, request.queryMap().toMap());

        if (StringUtils.isEmpty(label)) {
            errors.add("Label must be specified.");
        }
        if (getFactory().lookupByLabel(label) != null) {
            errors.add("Virtual Host Manager with given label already exists.");
        }
        if (!getFactory().isConfigurationValid(gathererModule,
                gathererModuleParams)) {
            errors.add("All fields are mandatory.");
        }

        VirtualHostManager vhm = getFactory().createVirtualHostManager(
                label, user.getOrg(), gathererModule, gathererModuleParams);

        if (errors.isEmpty()) {
            getFactory().save(vhm);
            response.redirect("/rhn/manager/vhms");
            Spark.halt();
            return null;
        }
        else {
            Map<String, Object> data = new HashMap<>();
            data.put("virtualHostManager", vhm);
            data.put("csrf_token", CSRFTokenValidator.getToken(request.session().raw()));
            data.put("errors", errors);
            return new ModelAndView(data, "virtualhostmanager/add.jade");
        }
    }

    /**
     * Deletes a VHM.
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return dummy string to satisfy spark
     */
    public static Object delete(Request request, Response response, User user) {
        Long id = Long.parseLong(request.params("id"));
        VirtualHostManager virtualHostManager = getFactory().lookupByIdAndOrg(id,
                user.getOrg());
        String message;
        if (virtualHostManager == null) {
            message = "Virtual Host Manager with id '" + id + "' couldn't be found.";
        }
        else {
            getFactory().delete(virtualHostManager);
            message = "Virtual Host Manager with id '" + id + "' has been deleted.";
        }
        FlashScopeHelper.flash(request, message);
        response.redirect("/rhn/manager/vhms");
        return "";
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
        Map<String, String> params = new HashMap<String, String>();
        params.put("vhmlabel", label);
        try {
            new TaskomaticApi().scheduleSingleSatBunch(user, "gatherer-bunch", params);
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
     * @param gathererModule the gatherer module
     * @param queryMap the query map
     * @return the map
     */
    private static Map<String, String> paramsFromQueryMap(String gathererModule,
            Map<String, String[]> queryMap) {
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
        return VirtualHostManagerFactory.getInstance();
    }
}
