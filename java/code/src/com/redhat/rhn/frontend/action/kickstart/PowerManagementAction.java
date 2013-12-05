package com.redhat.rhn.frontend.action.kickstart;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.StrutsDelegate;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerPowerCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerPowerCommand.Operation;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerPowerSettingsUpdateCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.cobbler.SystemRecord;
import org.cobbler.XmlRpcException;

import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Saves power settings and boots machines.
 *
 * @version $Rev$
 */
public class PowerManagementAction extends RhnAction {

    /** The log. */
    private static Logger log = Logger.getLogger(PowerManagementAction.class);

    /** Attribute name. */
    public static final String TYPES = "types";

    /** Attribute name. */
    public static final String POWER_TYPE = "powerType";

    /** Attribute name. */
    public static final String POWER_ADDRESS = "powerAddress";

    /** Attribute name. */
    public static final String POWER_USERNAME = "powerUsername";

    /** Attribute name. */
    public static final String POWER_PASSWORD = "powerPassword";

    /** Attribute name. */
    public static final String POWER_ID = "powerId";

    /** Attribute name. */
    public static final String POWER_STATUS_ON = "powerStatusOn";

    /**
     * Runs this action.
     *
     * @param mapping action mapping
     * @param formIn form submitted values
     * @param request http request object
     * @param response http response object
     * @return an action forward object
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm formIn,
        HttpServletRequest request, HttpServletResponse response) {
        RequestContext context = new RequestContext(request);
        DynaActionForm form = (DynaActionForm) formIn;
        StrutsDelegate strutsDelegate = getStrutsDelegate();
        User user = context.getCurrentUser();
        Long sid = context.getRequiredParam(RequestContext.SID);
        Server server = SystemManager.lookupByIdAndUser(sid, user);

        if (context.isSubmitted()) {
            CobblerPowerSettingsUpdateCommand command = getPowerSettingsUpdateCommand(form,
                user, server);

            ValidatorError error = command.store();
            if (error == null) {
                log.debug("Power management settings saved for system " + sid);
                if (context.wasDispatched("kickstart.powermanagement.jsp.save_only")) {
                    addMessage(request, "kickstart.powermanagement.saved");
                }
                if (context.wasDispatched("kickstart.powermanagement.jsp.power_on")) {
                    error = new CobblerPowerCommand(user, server, Operation.PowerOn)
                        .store();
                    if (error == null) {
                        log.debug("Power on succeded for system " + sid);
                        addMessage(request, "kickstart.powermanagement.powered_on");
                    }
                }
                if (context.wasDispatched("kickstart.powermanagement.jsp.power_off")) {
                    error = new CobblerPowerCommand(user, server, Operation.PowerOff)
                        .store();
                    if (error == null) {
                        log.debug("Power off succeded for system " + sid);
                        addMessage(request, "kickstart.powermanagement.powered_off");
                    }
                }
                if (context.wasDispatched("kickstart.powermanagement.jsp.reboot")) {
                    error = new CobblerPowerCommand(user, server, Operation.Reboot).store();
                    if (error == null) {
                        log.debug("Reboot succeded for system " + sid);
                        addMessage(request, "kickstart.powermanagement.rebooted");
                    }
                }
                if (context.wasDispatched(
                    "kickstart.powermanagement.jsp.get_status")) {
                    try {
                        SystemRecord record = getSystemRecord(user, server);
                        request.setAttribute(POWER_STATUS_ON, record.getPowerStatus());
                        addMessage(request, "kickstart.powermanagement.saved");
                    }
                    catch (XmlRpcException e) {
                        log.warn("Could not get power status from Cobbler for system " +
                            server.getId());
                        addMessage(request,
                            "kickstart.powermanagement.jsp.power_status_failed");
                    }
                }
            }

            if (error != null) {
                ActionErrors errors = new ActionErrors();
                strutsDelegate.addError(errors, error.getKey(), error.getValues());
                strutsDelegate.saveMessages(request, errors);
            }
        }

        setAttributes(request, context, server, user, strutsDelegate);

        return strutsDelegate.forwardParams(
            mapping.findForward(RhnHelper.DEFAULT_FORWARD), request.getParameterMap());
    }

    /**
     * Returns a CobblerPowerSettingsUpdateCommand from form data.
     * @param form the form
     * @param user currently logged in user
     * @param server server to update
     * @return the command
     */
    public static CobblerPowerSettingsUpdateCommand getPowerSettingsUpdateCommand(
        DynaActionForm form, User user, Server server) {
        return new CobblerPowerSettingsUpdateCommand(
            user, server, form.getString(POWER_TYPE), form.getString(POWER_ADDRESS),
            form.getString(POWER_USERNAME), form.getString(POWER_PASSWORD),
            form.getString(POWER_ID));
    }

    /**
     * Sets the page attributes.
     *
     * @param request the request
     * @param context the context
     * @param server the server
     * @param user the user
     * @param strutsDelegate the Struts delegate
     */
    private void setAttributes(HttpServletRequest request, RequestContext context,
        Server server, User user, StrutsDelegate strutsDelegate) {
        request.setAttribute(RequestContext.SID, server.getId());
        request.setAttribute(RequestContext.SYSTEM, server);

        SortedMap<String, String> types = setUpPowerTypes(request, strutsDelegate);
        if (types.size() > 0) {
            SystemRecord record = getSystemRecord(user, server);

            if (record == null) {
                request.setAttribute(POWER_TYPE, types.get(types.firstKey()));
            }
            else {
                request.setAttribute(POWER_TYPE, record.getPowerType());
                request.setAttribute(POWER_ADDRESS, record.getPowerAddress());
                request.setAttribute(POWER_USERNAME, record.getPowerUsername());
                request.setAttribute(POWER_PASSWORD, record.getPowerPassword());
                request.setAttribute(POWER_ID, record.getPowerId());
            }
        }
    }

    /**
     * Sets up and returns a list of supported Cobbler power types.
     * @param request the current request
     * @param strutsDelegate the Struts delegate
     * @return the types
     */
    public static SortedMap<String, String> setUpPowerTypes(HttpServletRequest request,
        StrutsDelegate strutsDelegate) {
        SortedMap<String, String> types = new TreeMap<String, String>();
        String typeString = ConfigDefaults.get().getCobblerPowerTypes();
        if (typeString != null) {
            List<String> typeNames = Arrays.asList(typeString.split(" *, *"));
            for (String typeName : typeNames) {
                types.put(
                    LocalizationService.getInstance().getPlainText(
                        "cobbler.powermanagement." + typeName), typeName);
            }
        }
        request.setAttribute(TYPES, types);

        if (types.size() == 0) {
            ActionErrors errors = new ActionErrors();
            strutsDelegate.addError(errors, "kickstart.powermanagement.jsp.no_types",
                ConfigDefaults.POWER_MANAGEMENT_TYPES);
            strutsDelegate.saveMessages(request, errors);
        }
        return types;
    }

    /**
     * Return the Cobbler system record corresponding to the system
     * @param user current user
     * @param server server to look up
     * @return a Cobbler system record
     */
    private SystemRecord getSystemRecord(User user, Server server) {
        return SystemRecord.lookupById(
            CobblerXMLRPCHelper.getConnection(user), server.getCobblerId());
    }
}
