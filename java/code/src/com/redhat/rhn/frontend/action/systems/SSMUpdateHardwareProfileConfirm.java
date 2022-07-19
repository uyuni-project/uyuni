/*
 * Copyright (c) 2010--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.systems;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.rhnset.RhnSetElement;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.Listable;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * SSMUpdateHardwareProfileConfirm
 */
public class SSMUpdateHardwareProfileConfirm extends RhnAction implements Listable {
    /** Logger instance */
    private static Logger log = LogManager.getLogger(SSMUpdateHardwareProfileConfirm.class);

    private static final TaskomaticApi TASKOMATIC_API = new TaskomaticApi();

    /**
     * {@inheritDoc}
     */
    public ActionForward execute(ActionMapping mapping,
            ActionForm formIn,
            HttpServletRequest request,
            HttpServletResponse response) {
        RequestContext context = new RequestContext(request);

        User user = context.getCurrentUser();
        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        request.setAttribute("system_count", set.size());
        DynaActionForm daForm = (DynaActionForm)formIn;
        Map params = makeParamMap(request);

        if (isSubmitted(daForm)) {
            Iterator it = set.iterator();
            Set<Long> serverIds = new HashSet<>();
            while (it.hasNext()) {
                Long sid = ((RhnSetElement)it.next()).getElement();
                serverIds.add(sid);
            }
            Date now = new Date();

            Action a = ActionManager.scheduleHardwareRefreshAction(user, now, serverIds);
            ActionFactory.save(a);
            try {
                TASKOMATIC_API.scheduleActionExecution(a);
                ActionMessages msgs = new ActionMessages();
                ActionMessage msg = new ActionMessage("ssm.hw.systems.confirmmessage");

                if (set.size() != 1) {
                    msg = new ActionMessage("ssm.hw.systems.confirmmessage.multiple", set.size());
                }
                msgs.add(ActionMessages.GLOBAL_MESSAGE, msg);
                getStrutsDelegate().saveMessages(request, msgs);

                return getStrutsDelegate().forwardParams(
                        mapping.findForward("success"), params);
            }
            catch (TaskomaticApiException e) {
                log.error("Could not schedule hardware refresh:");
                log.error(e);
                ActionErrors errors = new ActionErrors();
                getStrutsDelegate().addError(errors, "taskscheduler.down");
                getStrutsDelegate().saveMessages(request, errors);
            }
        }

        return getStrutsDelegate().forwardParams(
                mapping.findForward(RhnHelper.DEFAULT_FORWARD), params);
    }

    /**
     * {@inheritDoc}
     */
    public List getResult(RequestContext contextIn) {
        return SystemManager.inSet(contextIn.getCurrentUser(),
                                        RhnSetDecl.SYSTEMS.getLabel());
    }

}
