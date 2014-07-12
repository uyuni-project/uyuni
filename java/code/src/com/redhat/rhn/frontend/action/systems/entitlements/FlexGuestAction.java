/**
 * Copyright (c) 2010--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.systems.entitlements;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ChannelFamilySystemGroup;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.ListHelper;
import com.redhat.rhn.manager.system.VirtualizationEntitlementsManager;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * FloatingVirtualizationAction
 * @version $Rev$
 */
public class FlexGuestAction  extends EligibleFlexGuestAction  {

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public ActionForward execute(ActionMapping mapping,
            ActionForm formIn,
            HttpServletRequest request,
            HttpServletResponse response) {

        RequestContext requestContext = new RequestContext(request);
        User user = requestContext.getCurrentUser();


        request.setAttribute("selected_family", getSelectedChannel(requestContext));
        ListHelper helper = new ListHelper(this, request);
        helper.execute();
        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }


    @Override
    protected List<ChannelFamilySystemGroup> query(RequestContext contextIn) {
        return VirtualizationEntitlementsManager.getInstance().
            listFlexGuests(contextIn.getCurrentUser());
    }

}
