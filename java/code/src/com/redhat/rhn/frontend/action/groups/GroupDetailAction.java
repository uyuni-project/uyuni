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

package com.redhat.rhn.frontend.action.groups;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.manager.system.ServerGroupManager;

import com.suse.manager.utils.MinionServerUtils;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * GroupDetailAction
 */
public class GroupDetailAction extends RhnAction {

    private final ServerGroupManager serverGroupManager = GlobalInstanceHolder.SERVER_GROUP_MANAGER;

    /** {@inheritDoc} */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) {
        RequestContext rctx = new RequestContext(request);

        User user = rctx.getCurrentUser();
        ManagedServerGroup sg = rctx.lookupAndBindServerGroup();
        Map<String, String> errataCounts = serverGroupManager.errataCounts(user, sg);
        errataCounts.putIfAbsent("se", "0");
        errataCounts.putIfAbsent("be", "0");
        errataCounts.putIfAbsent("ee", "0");
        errataCounts.putIfAbsent("op", "0");
        long minionCount = MinionServerUtils.filterSaltMinions(sg.getServers()).count();

        request.setAttribute("id", sg.getId());
        request.setAttribute("errata_counts", errataCounts);
        request.setAttribute("admin_count", sg.getAssociatedAdminsCount());
        request.setAttribute("system_count", sg.getCurrentMembers());
        request.setAttribute("minion_count", minionCount);
        request.setAttribute("name", sg.getName());
        request.setAttribute("description", sg.getDescription());

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }
}
