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
package com.redhat.rhn.frontend.action.channel.manage.repo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.manager.channel.ChannelManager;

/**
 * RepoChannelsAction : class to list channels associated to repos
 * @version $Rev: 1 $
 */
public class RepoChannelsAction extends RhnAction {

    /**
     * ${@inheritDoc}
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws Exception {

        request.setAttribute(mapping.getParameter(), Boolean.TRUE);
        RequestContext context = new RequestContext(request);
        User user = context.getCurrentUser();
        Long csid = context.getRequiredParam("id");
        DataResult result;

        result = ChannelManager.channelsForContentSource(csid, null);
        request.setAttribute(RequestContext.PAGE_LIST, result);
        request.setAttribute(ListTagHelper.PARENT_URL, request.getRequestURI());
        request.setAttribute("repo_name", ChannelFactory.lookupContentSource(csid,
                user.getOrg()).getLabel());
        request.setAttribute("id", csid);
        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

}
