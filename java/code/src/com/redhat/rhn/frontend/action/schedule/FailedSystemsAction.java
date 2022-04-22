/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.schedule;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFormatter;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.ListSessionSetHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.Listable;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * FailedSystemsAction
 */
public class FailedSystemsAction extends RhnAction implements Listable {

    /** Logger instance */
    private static Logger log = LogManager.getLogger(FailedSystemsAction.class);

    protected void setup(HttpServletRequest request) {
        RequestContext context = new RequestContext(request);
        context.lookupAndBindAction();
    }

    /**
     * {@inheritDoc}
     */
    public String getDataSetName() {
        return RequestContext.PAGE_LIST;
    }

    /**
     * {@inheritDoc}
     */
    public String getListName() {
        return "failedSystemsList";
    }

    protected Map getParamsMap(HttpServletRequest request) {
        RequestContext context = new RequestContext(request);
        Map<String, Object> params = new HashMap<>();
        params.put(RequestContext.AID,
                context.getRequiredParam(RequestContext.AID));
        return params;
    }

    @Override
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm formIn,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        setup(request);
        ListSessionSetHelper helper = new ListSessionSetHelper(this,
                request, getParamsMap(request));
        processHelper(helper);
        helper.execute();

        Action action = (Action) request.getAttribute(RequestContext.ACTION);
        ActionFormatter af = action.getFormatter();
        request.setAttribute("actionname", af.getName());
        request.setAttribute("canEdit",
                String.valueOf(action.getPrerequisite() == null));

        if (Boolean.parseBoolean(request.getParameter("reschedule"))) {
            ActionForward forward =
                    handleDispatch(helper, mapping, formIn, request, response);
            processPostSubmit(helper);
            return forward;
        }

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    protected void processHelper(ListSessionSetHelper helper) {
        helper.setDataSetName(getDataSetName());
        helper.setListName(getListName());
    }

    protected void processPostSubmit(ListSessionSetHelper helper) {
        helper.destroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List getResult(RequestContext context) {
        Action action = context.lookupAndBindAction();
        PageControl pc = new PageControl();
        pc.setFilterColumn("earliest");
        return ActionManager.failedSystems(context.getCurrentUser(), action, pc);
    }

    /**
     * Resechedules the action whose id is found in the aid formvar.
     * @param helper list session set helper
     * @param mapping actionmapping
     * @param formIn form containing input
     * @param request HTTP request
     * @param response HTTP response
     * @return the confirmation page.
     */
    public ActionForward handleDispatch(ListSessionSetHelper helper,
                                        ActionMapping mapping,
                                        ActionForm formIn, HttpServletRequest request,
                                        HttpServletResponse response) {

        Action action = (Action) request.getAttribute(RequestContext.ACTION);
        try {
            ActionManager.rescheduleAction(action, true);

            ActionMessages msgs = new ActionMessages();
            msgs.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("message.actionrescheduled",
                            action.getActionType().getName()));
            getStrutsDelegate().saveMessages(request, msgs);
        }
        catch (TaskomaticApiException e) {
            log.error("Could not reschedule action:");
            log.error(e);
            ActionErrors errors = new ActionErrors();
            getStrutsDelegate().addError(errors, "taskscheduler.down");
            getStrutsDelegate().saveMessages(request, errors);
        }

        return getStrutsDelegate().forwardParam(
                mapping.findForward("scheduled"), "aid", String.valueOf(action.getId()));
    }
}
