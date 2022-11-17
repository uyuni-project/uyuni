/*
 * Copyright (c) 2012--2015 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.systems.sdc;

import static java.util.Collections.singleton;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.SystemPendingEventDto;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * SystemPendingEventsCancelAction
 */
public class SystemPendingEventsCancelAction extends RhnAction {

    private static final Logger LOG =
            LogManager.getLogger(SystemPendingEventsCancelAction.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm formIn,
            HttpServletRequest request,
            HttpServletResponse response) {
        RequestContext context = new RequestContext(request);
        Long sid = context.getRequiredParam("sid");
        User user =  context.getCurrentUser();
        Server server = context.lookupAndBindServer();

        RhnSet set = RhnSetDecl.PENDING_ACTIONS_TO_DELETE.get(user);
        DataResult<SystemPendingEventDto> result = ActionManager
                .pendingActionsToDeleteInSet(context.getCurrentUser(), null,
                RhnSetDecl.PENDING_ACTIONS_TO_DELETE.getLabel(), sid);
        request.setAttribute(ListTagHelper.PARENT_URL, request.getRequestURI());
        request.setAttribute(RequestContext.PAGE_LIST, result);

        if (context.wasDispatched("system.event.pending.cancel")) {
            try {
                for (SystemPendingEventDto dto : result) {
                    Action action = ActionFactory.lookupById(dto.getId());
                    ActionManager.cancelActions(user, singleton(action), singleton(sid));
                }
                createSuccessMessage(request, "system.event.pending.canceled",
                        Integer.toString(result.size()));
            }
            catch (TaskomaticApiException e) {
                LOG.error(e);
                createErrorMessage(request,
                        "system.event.pending.canceled.taskscheduler.down",
                        StringUtils.EMPTY);
            }

            set.clear();
            RhnSetManager.store(set);
            Map params = makeParamMap(request);
            params.put("sid", server.getId());
            return getStrutsDelegate().forwardParams(
                    mapping.findForward(RhnHelper.CONFIRM_FORWARD), params);
        }
        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

}
