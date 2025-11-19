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

package com.redhat.rhn.frontend.action.systems.groups;

import static com.redhat.rhn.manager.user.UserManager.ensureRoleBasedAccess;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.domain.access.Namespace;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.StrutsDelegate;
import com.redhat.rhn.frontend.taglibs.list.helper.ListSessionSetHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.Listable;
import com.redhat.rhn.manager.system.ServerGroupManager;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * @author paji
 * ListRemoveGroupsAction
 */
public class ListRemoveGroupsAction extends BaseListAction implements Listable<ManagedServerGroup> {

    private static final ServerGroupManager SERVER_GROUP_MANAGER = GlobalInstanceHolder.SERVER_GROUP_MANAGER;

    /** {@inheritDoc} */
    @Override
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm formIn,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        setup(request);
        RequestContext context = new RequestContext(request);
        Map<String, Object> params = new HashMap<>();
                params.put(RequestContext.SID,
                        context.getRequiredParam(RequestContext.SID));
        ListSessionSetHelper helper = new ListSessionSetHelper(this, request, params);
        helper.execute();
        if (helper.isDispatched()) {
            return handleDispatch(helper, mapping, formIn, request, response);
        }
        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    /** {@inheritDoc} */
    public ActionForward handleDispatch(
            ListSessionSetHelper helper,
            ActionMapping mapping,
            ActionForm formIn, HttpServletRequest request,
            HttpServletResponse response) {
        RequestContext context = new RequestContext(request);
        User user = context.getCurrentUser();
        ensureRoleBasedAccess(user, "systems.groups.list", Namespace.AccessMode.W);
        Server server = context.lookupAndBindServer();
        List<Server> servers = new LinkedList<>();
        servers.add(server);
        Set<String> set = helper.getSet();

        for (String id : set) {
            Long sgid = Long.valueOf(id);
            ServerGroup group = SERVER_GROUP_MANAGER.lookup(sgid, user);
            SERVER_GROUP_MANAGER.removeServers(group, servers, user);
        }
        helper.destroy();
        getStrutsDelegate().saveMessage(
                    "systems.groups.jsp.removed",
                        new String [] {String.valueOf(set.size())}, request);

        Map<String, Object> params = new HashMap<>();
        params.put(RequestContext.SID, server.getId().toString());
        StrutsDelegate strutsDelegate = getStrutsDelegate();
        return strutsDelegate.forwardParams
                        (mapping.findForward("success"), params);
    }

    /** {@inheritDoc} */
    @Override
    public List<ManagedServerGroup> getResult(RequestContext context) {
        return context.lookupAndBindServer().getManagedGroups();
    }
}
