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
 * todo conventions, conventions, conventions!!!!
 *  - jade template filenames
 *  - spark parameters
 *  - spark endpoints!
 *  todo polish the csrf machinery
 */
public class VirtualHostManagerController {

    // to separate gatherer modules config values in the form:
    // (VMWare<separator>host, SUSECloud<separator>port)
    public static final String SEPARATOR = "---";

    public static ModelAndView getAll(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("csrf_token", CSRFTokenValidator.getToken(request.session().raw()));
        data.put("virtualHostManagers", getFactory()
                .listVirtualHostManagers(user.getOrg()));
        data.put("info", FlashScopeHelper.flash(request));
        return new ModelAndView(data, "virtualhostmanager/all.jade");
    }

    public static ModelAndView get(Request request, Response response, User user) {
        String label = request.params("vhmlabel");

        Map<String, Object> data = new HashMap<>();
        data.put("virtualHostManager", getFactory().lookupByLabelAndOrg(label, user.getOrg()));
        data.put("csrf_token", CSRFTokenValidator.getToken(request.session().raw()));

        return new ModelAndView(data, "virtualhostmanager/detail.jade");
    }

    public static ModelAndView addForm(Request request, Response response) {
        Map<String, Object> data = makeModuleFormData("", null, null);
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
     * @param filledGathererModule
     * @param filledGMParams
     * @return
     */
    private static HashMap<String, Object> makeModuleFormData(String vhmLabel,
            String filledGathererModule, Map<String, String> filledGMParams)
    {
        HashMap<String, Object> data = new HashMap<>();
        Map<String, GathererModule> gathererModules = new GathererRunner().listModules();

        // the provided one or the first valid one
        String preselectedModule = (filledGathererModule == null)
                ? gathererModules.entrySet().iterator().next().getKey()
                : filledGathererModule;

        // pre-filled gatherer config values, if provided
        if (filledGathererModule != null && filledGMParams != null) {
            // add (possibly partly) already filled data
            gathererModules.get(filledGathererModule).getParameters().putAll(filledGMParams);
        }

        data.put("label", vhmLabel);
        data.put("selectedGathererModule", preselectedModule);
        data.put("gathererModules", gathererModules);
        return data;
    }

    // todo generalize the validation somehow (maybe wrap with sth that catches an exc...)
    public static ModelAndView add(Request request, Response response, User user) {
        List<String> errors = new LinkedList<>();

        String label = request.queryParams("label");
        String gathererModule = request.queryParams("gathererModule");
        Map<String, String> gathererModuleParams =
                createGathererModuleParams(gathererModule, request.queryMap().toMap());

        if (StringUtils.isEmpty(label)) {
            errors.add("Label can't be empty");
        }
        if (StringUtils.isEmpty(gathererModule)) {
            errors.add("Gatherer module must be selected");
        }
        if (VirtualHostManagerFactory.getInstance().lookupByLabel(label) != null) {
            errors.add("Virtual Host Manager with given name already exists");
        }

        try {
            if (errors.isEmpty()) {
                getFactory().createVirtualHostManager(
                        label,
                        user.getOrg(),
                        gathererModule,
                        gathererModuleParams);
            }
        } catch (InvalidGathererConfigException e) {
            errors.add("Invalid gatherer module configuration (did you fill all fields?)");
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

    private static Map<String,String> createGathererModuleParams(String gathererModule,
            Map<String, String[]> queryMap) {
        return queryMap.entrySet().stream()
                .filter(keyVal -> StringUtils.isNotEmpty(keyVal.getValue()[0]))
                .filter(keyVal -> keyVal.getKey().startsWith(withSeparator(gathererModule)))
                .collect(Collectors.toMap(
                        keyVal -> stripSeparator(keyVal.getKey()),
                        keyVal -> keyVal.getValue()[0]));
    }

    public static Object delete(Request request, Response response, User user) {
        // todo pass info about deleted
        String label = request.params("vhmlabel");
        VirtualHostManager virtualHostManager = getFactory().lookupByLabelAndOrg(label, user.getOrg());
        getFactory().delete(virtualHostManager);
        FlashScopeHelper.flash(request, "Virtual Host Manager with label '" + label +
                "' has been deleted");
        response.redirect("/rhn/manager/vhms");
        return "";
    }

    public static Object refresh(Request request, Response response, User user) {
        String label = request.params("vhmlabel");
        String message = null;
        Map<String, String> params = new HashMap<String, String>();
        params.put("vhmlabel", label);
        try {
            new TaskomaticApi().scheduleSingleSatBunch(user, "gatherer-bunch", params);
        }
        catch (TaskomaticApiException e) {
            message  = "Problem when running gatherer Taskomatic job: " + e.getMessage();
        }
        if (message == null) {
            message = "Gatherer Taskomatic job for Virtual Host Manager with label '"
                    + label + "' was triggered";
        }
        FlashScopeHelper.flash(request, message);
        response.redirect("/rhn/manager/vhms");
        return "";
    }

    private static VirtualHostManagerFactory getFactory() {
        return VirtualHostManagerFactory.getInstance();
    }

    private static String withSeparator(String gathererModule) {
        return gathererModule + SEPARATOR;
    }

    private static String stripSeparator(String enhanced) {
        return enhanced.replaceFirst(".+" + SEPARATOR, "");
    }
}
