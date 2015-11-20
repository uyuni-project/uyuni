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
import com.redhat.rhn.domain.server.virtualhostmanager.InvalidGathererConfigException;
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
 * todo all javadocs
 *  - jade template filenames
 *  - spark parameters
 *  - spark endpoints!
 *  todo polish the csrf machinery
 */
public class VirtualHostManagerController {

    private VirtualHostManagerController() { }

    public static ModelAndView list(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("csrf_token", CSRFTokenValidator.getToken(request.session().raw()));
        data.put("virtualHostManagers", getFactory()
                .listVirtualHostManagers(user.getOrg()));
        data.put("modules", new GathererRunner().listModules().keySet());
        data.put("info", FlashScopeHelper.flash(request));
        return new ModelAndView(data, "virtualhostmanager/list.jade");
    }

    public static ModelAndView show(Request request, Response response, User user) {
        String label = request.params("id");

        Map<String, Object> data = new HashMap<>();
        data.put("virtualHostManager", getFactory().lookupByLabelAndOrg(label,
                user.getOrg()));
        data.put("csrf_token", CSRFTokenValidator.getToken(request.session().raw()));

        return new ModelAndView(data, "virtualhostmanager/show.jade");
    }

    public static ModelAndView add(Request request, Response response) {
        String module = request.queryParams("module");
        Map<String, Object> data = makeModuleFormData("", module, null);
        data.put("csrf_token", CSRFTokenValidator.getToken(request.session().raw()));
        return new ModelAndView(data, "virtualhostmanager/add.jade");
    }

    /**
     * Creates a backing map for Virtual Host Manager creation form.
     * This form contains, among other things, configs (parameter names and values)
     * of all possible Virtual Host Gatherer modules.
     * The values of this backing map can be pre-populated with given data in arguments
     * (can be useful when we want to preserve user-entered values when form validation
     * fails.)
     *
     * todo this boilerplate should be definitely handled by some kind of framework!
     *
     * @param module
     * @param filledGMParams
     * @return
     */
    private static HashMap<String, Object> makeModuleFormData(String vhmLabel,
            String module, Map<String, String> filledGMParams) {
        HashMap<String, Object> data = new HashMap<>();
        GathererModule gathererModule = new GathererRunner().listModules().get(module);

        // pre-filled gatherer config values, if provided
        if (filledGMParams != null) {
            // add (possibly partly) already filled data
            gathererModule.getParameters().putAll(
                    filledGMParams);
        }

        data.put("label", vhmLabel);
        data.put("module", gathererModule);
        return data;
    }

    // todo generalize the validation somehow (maybe wrap with sth that catches an exc...)
    public static ModelAndView create(Request request, Response response, User user) {
        List<String> errors = new LinkedList<>();

        String label = request.queryParams("label");
        String gathererModule = request.queryParams("module");
        Map<String, String> gathererModuleParams =
                createGathererModuleParams(gathererModule, request.queryMap().toMap());

        if (StringUtils.isEmpty(label)) {
            errors.add("Label must be specified.");
        }
        if (VirtualHostManagerFactory.getInstance().lookupByLabel(label) != null) {
            errors.add("Virtual Host Manager with given label already exists.");
        }

        try {
            if (errors.isEmpty()) {
                getFactory().createVirtualHostManager(
                        label,
                        user.getOrg(),
                        gathererModule,
                        gathererModuleParams);
            }
        }
        catch (InvalidGathererConfigException e) {
            errors.add("All fields are mandatory.");
        }

        if (!errors.isEmpty()) {
            Map data = makeModuleFormData(label, gathererModule, gathererModuleParams);
            data.put("csrf_token", CSRFTokenValidator.getToken(request.session().raw()));
            data.put("errors", errors);
            return new ModelAndView(data, "virtualhostmanager/add.jade");
        }

        response.redirect("/rhn/manager/vhms");
        Spark.halt();
        return null;
    }

    private static Map<String, String> createGathererModuleParams(String gathererModule,
            Map<String, String[]> queryMap) {
        return queryMap.entrySet().stream()
                .filter(keyVal -> StringUtils.isNotEmpty(keyVal.getValue()[0]))
                .filter(keyVal -> keyVal.getKey().startsWith("module_"))
                .collect(Collectors.toMap(
                        keyVal -> keyVal.getKey().replaceFirst("module_", ""),
                        keyVal -> keyVal.getValue()[0]));
    }

    public static Object delete(Request request, Response response, User user) {
        String label = request.params("id");
        VirtualHostManager virtualHostManager = getFactory().lookupByLabelAndOrg(label,
                user.getOrg());
        String message;
        if (virtualHostManager == null) {
            message = "Virtual Host Manager with label '" + label + "' couldn't be found.";
        }
        else {
            getFactory().delete(virtualHostManager);
            message = "Virtual Host Manager with label '" + label + "' has been deleted.";
        }
        FlashScopeHelper.flash(request, message);
        response.redirect("/rhn/manager/vhms");
        return "";
    }

    public static Object refresh(Request request, Response response, User user) {
        String label = request.params("id");
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

    private static VirtualHostManagerFactory getFactory() {
        return VirtualHostManagerFactory.getInstance();
    }
}
