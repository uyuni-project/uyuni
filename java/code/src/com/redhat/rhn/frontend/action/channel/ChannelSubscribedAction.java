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
package com.redhat.rhn.frontend.action.channel;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.RhnListSetHelper;
import com.redhat.rhn.frontend.taglibs.list.AlphaBarHelper;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.frontend.taglibs.list.TagHelper;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ChannelPackagesAction
 * @version $Rev$
 */
public class ChannelSubscribedAction extends RhnAction {

    private final String LIST_NAME = "systemList";


    /** {@inheritDoc} */
    public ActionForward execute(ActionMapping mapping,
            ActionForm formIn,
            HttpServletRequest request,
            HttpServletResponse response) {

        RequestContext requestContext = new RequestContext(request);
        User user =  requestContext.getCurrentUser();

        long cid = requestContext.getRequiredParam("cid");
        Channel chan = ChannelFactory.lookupByIdAndUser(cid, user);



        DataResult<Map<String, Object>> result =
                SystemManager.systemsSubscribedToChannelDto(chan, user);
        result.setElaborationParams(new HashMap<String, Object>());
        RhnListSetHelper helper = new RhnListSetHelper(request);

        RhnSet set =  RhnSetDecl.SYSTEMS.get(user);
        request.getParameter(
                AlphaBarHelper.makeAlphaKey(TagHelper.generateUniqueName(LIST_NAME)));


        if (ListTagHelper.getListAction(LIST_NAME, request) != null) {
            helper.execute(set, LIST_NAME, result);
        }

        if (!set.isEmpty()) {
            helper.syncSelections(set, result);
            ListTagHelper.setSelectedAmount(LIST_NAME, set.size(), request);
        }

        ListTagHelper.bindSetDeclTo(LIST_NAME,  RhnSetDecl.SYSTEMS, request);
        TagHelper.bindElaboratorTo(LIST_NAME, result.getElaborator(), request);

        request.setAttribute("cid", chan.getId());
        request.setAttribute("channel_name", chan.getName());
        request.setAttribute(ListTagHelper.PARENT_URL, request.getRequestURI());
        request.setAttribute(RequestContext.PAGE_LIST, result);




        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);

    }





}
