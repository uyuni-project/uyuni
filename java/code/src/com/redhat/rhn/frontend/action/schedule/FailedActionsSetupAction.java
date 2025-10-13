/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

import static com.redhat.rhn.manager.user.UserManager.ensureRoleBasedAccess;

import com.redhat.rhn.domain.access.Namespace;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ScheduledAction;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * FailedActionsSetupAction
 */
public class FailedActionsSetupAction extends BaseScheduledListAction {


    /**
     * {@inheritDoc}
     */
    @Override
    protected RhnSetDecl getSetDecl() {
        return RhnSetDecl.ACTIONS_FAILED;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public List<ScheduledAction> getResult(RequestContext context) {
        return ActionManager.failedActions(context.getCurrentUser(), null);
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    protected ActionForward handleSubmit(ActionMapping mapping,
                                         ActionForm formIn,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {


        User user = new RequestContext(request).getCurrentUser();
        ensureRoleBasedAccess(user, "schedule.failed", Namespace.AccessMode.W);
        return archiveAction(mapping, formIn, request, response);

    }
}
