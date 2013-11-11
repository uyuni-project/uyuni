package com.redhat.rhn.frontend.action.kickstart;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerPowerOffCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerPowerOnCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerPowerSettingsUpdateCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerRebootCommand;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    /** Attribute name. */
    public static final String POWER_ADDITIONAL_ACTION = "powerAdditionalAction";

    /** Possible attribute value for POWER_ADDITIONAL_ACTION. */
    public static final String POWER_ON = "powerOn";

    /** Possible attribute value for POWER_ADDITIONAL_ACTION. */
    public static final String POWER_OFF = "powerOff";

    /** Possible attribute value for POWER_ADDITIONAL_ACTION. */
    public static final String REBOOT = "reboot";

    /** Possible attribute value for POWER_ADDITIONAL_ACTION. */
    public static final String GET_STATUS = "getStatus";

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
        User user = context.getCurrentUser();
        Long sid = context.getRequiredParam(RequestContext.SID);
        Server server = SystemManager.lookupByIdAndUser(sid, user);

        if (context.isSubmitted()) {
            CobblerPowerSettingsUpdateCommand command =
                new CobblerPowerSettingsUpdateCommand(
                user, server, form.getString(POWER_TYPE), form.getString(POWER_ADDRESS),
                form.getString(POWER_USERNAME), form.getString(POWER_PASSWORD),
                form.getString(POWER_ID));

            ValidatorError error = command.store();
            if (error == null) {
                addMessage(request, "kickstart.powermanagement.saved");
                log.debug("Power management settings saved for system " + sid);
                if (POWER_ON.equals(form.getString(POWER_ADDITIONAL_ACTION))) {
                    error = new CobblerPowerOnCommand(user, server).store();
                    if (error == null) {
                        log.debug("Power on succeded for system " + sid);
                        addMessage(request, "kickstart.powermanagement.powered_on");
                    }
                }
                if (POWER_OFF.equals(form.getString(POWER_ADDITIONAL_ACTION))) {
                    error = new CobblerPowerOffCommand(user, server).store();
                    if (error == null) {
                        log.debug("Power off succeded for system " + sid);
                        addMessage(request, "kickstart.powermanagement.powered_off");
                    }
                }
                if (REBOOT.equals(form.getString(POWER_ADDITIONAL_ACTION))) {
                    error = new CobblerRebootCommand(user, server).store();
                    if (error == null) {
                        log.debug("Reboot succeded for system " + sid);
                        addMessage(request, "kickstart.powermanagement.rebooted");
                    }
                }
                if (GET_STATUS.equals(form.getString(POWER_ADDITIONAL_ACTION))) {
                    try {
                        SystemRecord record = getSystemRecord(user, server);
                        request.setAttribute(POWER_STATUS_ON, record.getPowerStatus());
                    }
                    catch (XmlRpcException e) {
                        log.warn("Could not get power status from Cobbler for system " +
                            server.getId());
                        addMessage(request,
                            "kickstart.powermanagement.jsp.cannotGetPowerStatus");
                    }
                }
            }

            if (error != null) {
                ActionErrors errors = new ActionErrors();
                getStrutsDelegate().addError(errors, error.getKey(), error.getValues());
                getStrutsDelegate().saveMessages(request, errors);
            }
        }

        setAttributes(request, context, server, user);

        return getStrutsDelegate().forwardParams(
            mapping.findForward(RhnHelper.DEFAULT_FORWARD), request.getParameterMap());
    }

    /**
     * Sets the page attributes.
     *
     * @param request the request
     * @param context the context
     * @param server the server
     * @param user the user
     */
    private void setAttributes(HttpServletRequest request, RequestContext context,
        Server server, User user) {
        request.setAttribute(RequestContext.SID, server.getId());
        request.setAttribute(RequestContext.SYSTEM, server);

        List<String> types = new ArrayList<String>();
        String typeString = ConfigDefaults.get().getCobblerPowerTypes();
        if (typeString != null) {
            types.addAll(Arrays.asList(typeString.split(" *, *")));
        }
        request.setAttribute(TYPES, types);

        if (types.size() == 0) {
            ActionErrors errors = new ActionErrors();
            getStrutsDelegate().addError(errors, "kickstart.powermanagement.notypes",
                ConfigDefaults.POWER_MANAGEMENT_TYPES);
            getStrutsDelegate().saveMessages(request, errors);
        }
        else {
            SystemRecord record = getSystemRecord(user, server);

            if (record == null) {
                request.setAttribute(POWER_TYPE, types.get(0));
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
