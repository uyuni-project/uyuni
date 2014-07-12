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
package com.redhat.rhn.frontend.action.rhnpackage.ssm;

import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.StrutsDelegate;
import com.redhat.rhn.frontend.taglibs.list.helper.ListSessionSetHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.Listable;
import com.redhat.rhn.manager.channel.ChannelManager;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * SSM action for selecting the packages to install. Package list is determined by the
 * first step in the flow where the channel is selected.
 *
 * @version $Revision$
 */
public class SelectInstallationPackagesAction extends RhnAction implements
        Listable<PackageListItem> {

    /** {@inheritDoc} */
    public ActionForward execute(ActionMapping actionMapping,
                                 ActionForm actionForm,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {

        RequestContext context = new RequestContext(request);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(RequestContext.CID, context.getRequiredParam(RequestContext.CID));
        params.put("mode", "install");
        ListSessionSetHelper helper = new ListSessionSetHelper(this, request, params);
        helper.setDataSetName(RequestContext.PAGE_LIST);
        helper.execute();

        StrutsDelegate strutsDelegate = getStrutsDelegate();

        if (helper.isDispatched()) {
            request.setAttribute("packagesDecl", helper.getDecl());
            return strutsDelegate.forwardParams(
                        actionMapping.findForward(RhnHelper.CONFIRM_FORWARD), params);
        }

        return strutsDelegate.forwardParams(
                actionMapping.findForward(RhnHelper.DEFAULT_FORWARD), params);
    }

    /** {@inheritDoc} */
    public List<PackageListItem> getResult(RequestContext context) {
        Long cid = context.getRequiredParam(RequestContext.CID);
        return ChannelManager.latestPackagesInChannel(cid);
    }

}
