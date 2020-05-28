/**
 * Copyright (c) 2012--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.systems.audit;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;

import com.redhat.rhn.common.util.DatePicker;
import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.action.scap.ScapAction;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.RhnValidationHelper;
import com.redhat.rhn.frontend.struts.StrutsDelegate;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

/**
 * ScheduleXccdfAction
 */
public class ScheduleXccdfAction extends ScapSetupAction {

    /** Logger instance */
    private static Logger log = Logger.getLogger(ScheduleXccdfAction.class);

    /**
     * {@inheritDoc}
     */
    public ActionForward execute(ActionMapping mapping, ActionForm formIn,
            HttpServletRequest request,
            HttpServletResponse response) {
        RequestContext context = new RequestContext(request);
        ActionForward forward = null;
        DynaActionForm form = (DynaActionForm) formIn;
        StrutsDelegate strutsDelegate = getStrutsDelegate();

        if (isSubmitted(form)) {
            Long sid = context.getRequiredParam("sid");
            User user = context.getCurrentUser();
            Server server = SystemManager.lookupByIdAndUser(sid, user);
            ActionErrors errors = RhnValidationHelper.validateDynaActionForm(this, form);

            if (errors.isEmpty()) {
                ActionMessages msgs = processForm(user, server, form);
                strutsDelegate.saveMessages(request, msgs);
                Map params = makeParamMap(request);
                params.put("sid", sid);
                forward = strutsDelegate.forwardParams(mapping.findForward("submit"),
                        params);
            }
            else {
                strutsDelegate.saveMessages(request, errors);
                forwardValuesOnError(form, strutsDelegate, request);
                forward = mapping.findForward("error");
            }
        }
        else {
            setupDefaultValues(request, form);
            forward = strutsDelegate.forwardParams(
                    mapping.findForward(RhnHelper.DEFAULT_FORWARD),
                    request.getParameterMap());
        }
        setupScapEnablementInfo(context);
        return forward;
    }

    private ActionMessages processForm(User user, Server server, DynaActionForm f) {
        String params = (String) f.get("params");
        String path = (String) f.get("path");
        Date earliest = getStrutsDelegate().readScheduleDate(f, "date",
                DatePicker.YEAR_RANGE_POSITIVE);
        try {
            ScapAction action = ActionManager.scheduleXccdfEval(user, server,
                path, params, earliest);

            ActionMessages msgs = new ActionMessages();
            msgs.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("message.xccdfeval",
                    action.getId().toString(), server.getId().toString(),
                    StringUtil.htmlifyText(server.getName())));
            return msgs;
        }
        catch (TaskomaticApiException e) {
            log.error("Could not schedule package refresh:");
            log.error(e);
            ActionErrors errors = new ActionErrors();
            getStrutsDelegate().addError(errors, "taskscheduler.down");
            return errors;
        }
    }

    private void forwardValuesOnError(DynaActionForm form, StrutsDelegate strutsDelegate,
            HttpServletRequest request) {
        request.setAttribute("path", form.get("path"));
        request.setAttribute("params", form.get("params"));
        Date earliest = strutsDelegate.readScheduleDate(form, "date",
                DatePicker.YEAR_RANGE_POSITIVE);
        DatePicker datePicker = strutsDelegate.prepopulateDatePicker(request,
                form, "date", DatePicker.YEAR_RANGE_POSITIVE);
    }

    private void setupDefaultValues(HttpServletRequest request, DynaActionForm form) {
        DatePicker date = getStrutsDelegate().prepopulateDatePicker(request,
                form, "date", DatePicker.YEAR_RANGE_POSITIVE);
    }
}
