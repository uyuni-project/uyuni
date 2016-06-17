/**
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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.salt.netapi.calls.modules.Schedule;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.results.Result;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.SystemPendingEventDto;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.manager.action.ActionManager;

/**
 * SystemPendingEventsCancelAction
 * @version $Rev$
 */
public class SystemPendingEventsCancelAction extends RhnAction {

    /**
     * {@inheritDoc}
     */
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
            createSuccessMessage(request, "system.event.pending.canceled",
                    new Integer(set.size()).toString());

            Optional<List<SystemPendingEventDto>> systemPendingEventDtos =
                    server.asMinionServer().map(minionServer -> {
                List<SystemPendingEventDto> actions = new LinkedList<>();
                for (SystemPendingEventDto action : result) {
                    Map<String, Result<Schedule.Result>> stringResultMap =
                            SaltAPIService.INSTANCE.deleteSchedule(
                                    "scheduled-action-" + action.getId(),
                                    new MinionList(minionServer.getMinionId())
                            );
                    Schedule.Result result1 = stringResultMap
                            .get(minionServer.getMinionId()).result().get();
                    if (result1 != null && result1.getResult()) {
                        actions.add(action);
                    }
                }
                return actions;
            });

            for (SystemPendingEventDto action : systemPendingEventDtos.orElse(result)) {
                ActionFactory.removeActionForSystem(action.getId(), sid);
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
