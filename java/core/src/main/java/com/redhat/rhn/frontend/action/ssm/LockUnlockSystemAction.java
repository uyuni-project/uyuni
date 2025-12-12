/*
 * Copyright (c) 2013 SUSE LLC
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
package com.redhat.rhn.frontend.action.ssm;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.RhnListAction;
import com.redhat.rhn.frontend.struts.RhnListSetHelper;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.frontend.taglibs.list.TagHelper;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author bo
 */
public class LockUnlockSystemAction extends RhnListAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm actionForm,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        ActionMessages actionMessages = new ActionMessages();
        ActionErrors actionErrors = new ActionErrors();
        RequestContext context = new RequestContext(request);
        DynaActionForm form = (DynaActionForm) actionForm;
        RhnListSetHelper listHelper = new RhnListSetHelper(request);
        RhnSet set = RhnSetDecl.SSM_SYSTEMS_SET_LOCK.get(context.getCurrentUser());

        if (request.getParameter("dispatch") != null) {
            int numLockedSystems = 0;
            int numUnlockedSystems = 0;
            boolean isLocked = context.wasDispatched("ssm.misc.lockunlock.dispatch.lock");
            boolean isUnlocked = context.wasDispatched("ssm.misc.lockunlock.dispatch.unlock");
            if (isLocked || isUnlocked) {
                String reason = StringUtil.nullIfEmpty(form.getString("lock_reason"));
                for (Long longIn : set.getElementValues()) {
                    Server server = SystemManager.lookupByIdAndUser(longIn, context.getCurrentUser());
                    if (isLocked && (server.getLock() == null)) {
                        if (reason == null) {
                            reason = LocalizationService.
                                    getInstance().getMessage("sdc.details.overview.lock.reason");
                        }

                        SystemManager.lockServer(context.getCurrentUser(), server, reason);
                        numLockedSystems++;
                    }
                    else if (isUnlocked && (server.getLock() != null)) {
                        SystemManager.unlockServer(context.getCurrentUser(), server);
                        numUnlockedSystems++;
                    }
                }

                if (isLocked) {
                    addLockUnlockActionMessage(true, set.size(), numLockedSystems, actionMessages);
                }
                else {
                    addLockUnlockActionMessage(false, set.size(), numUnlockedSystems, actionMessages);
                }
            }
        }

        this.bindData(listHelper, set, request);
        this.getStrutsDelegate().saveMessages(request, actionMessages);
        this.getStrutsDelegate().saveMessages(request, actionErrors);

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    private void addLockUnlockActionMessage(boolean isLocked, int setSize, int numSystems,
                                            ActionMessages actionMessages) {
        String keyPrefix = isLocked ? "ssm.misc.lockunlock.message.locked" : "ssm.misc.lockunlock.message.unlocked";
        String allPostfix = (setSize == numSystems ? ".all" : "");
        String successPostfix = (numSystems > 0 ? ".success" : ".skipped");
        actionMessages.add(ActionMessages.GLOBAL_MESSAGE,
                new ActionMessage(keyPrefix + allPostfix + successPostfix, setSize, numSystems));
    }

    private void bindData(RhnListSetHelper listHelper,
                          RhnSet set, HttpServletRequest request) {
        List<SystemOverview> result = this.getResult(new RequestContext(request));
        if (ListTagHelper.getListAction("systemList", request) != null) {
            listHelper.execute(set, "systemList", result);
        }

        if (!set.isEmpty()) {
            listHelper.syncSelections(set, result);
            ListTagHelper.setSelectedAmount("systemList", set.size(), request);
        }

        ListTagHelper.bindSetDeclTo("systemList",
                                    RhnSetDecl.SSM_SYSTEMS_SET_LOCK,
                                    request);
        request.setAttribute(RequestContext.PAGE_LIST, result);
        request.setAttribute(ListTagHelper.PARENT_URL, request.getRequestURI());
        TagHelper.bindElaboratorTo("systemList",
                                   ((DataResult) result).getElaborator(), request);
    }


    /**
     * {@inheritDoc}
     */
    public List<SystemOverview> getResult(RequestContext context) {
        List<SystemOverview> systems = SystemManager.inSet(context.getCurrentUser(),
                                                           RhnSetDecl.SYSTEMS.getLabel());
        for (SystemOverview systemOverview : systems) {
            systemOverview.setSelectable(1);
            Channel channel = SystemManager.lookupByIdAndUser(systemOverview.getId(),
                    context.getCurrentUser()).getBaseChannel();
            if (channel != null) {
                systemOverview.setChannelLabels(channel.getName());
            }
        }

        return systems;
    }

}
