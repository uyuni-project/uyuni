/**
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

import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.StrutsDelegate;
import com.redhat.rhn.frontend.taglibs.list.helper.ListSessionSetHelper;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author paji
 * @version $Rev$
 */
public class ListRemoveSystemsAction extends BaseListAction {

    /** {@inheritDoc} */
    @Override
    public ActionForward handleDispatch(ListSessionSetHelper helper,
            ActionMapping mapping,
            ActionForm formIn, HttpServletRequest request,
            HttpServletResponse response) {
        RequestContext context = new RequestContext(request);
        ManagedServerGroup sg = context.lookupAndBindServerGroup();
        User user = context.getCurrentUser();
        Set <String> set = helper.getSet();
        List<Server> servers = new LinkedList<Server>();
        for (String id : set) {
            Long sid = Long.valueOf(id);
            servers.add(SystemManager.lookupByIdAndUser(sid, user));
        }
        ServerGroupManager manager = ServerGroupManager.getInstance();
        manager.removeServers(sg, servers, user);
        getStrutsDelegate().saveMessage(
                    "systemgroup.systems.removed",
                        new String [] {String.valueOf(set.size()),
                            sg.getName()}, request);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(RequestContext.SERVER_GROUP_ID, sg.getId().toString());
        StrutsDelegate strutsDelegate = getStrutsDelegate();
        return strutsDelegate.forwardParams
                        (mapping.findForward("success"), params);
    }

    /** {@inheritDoc} */
    public List getResult(RequestContext context) {
        ManagedServerGroup sg = context.lookupAndBindServerGroup();
        return SystemManager.systemsInGroup(sg.getId(), null);
    }
}
