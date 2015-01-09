/**
 * Copyright (c) 2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.errata;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.common.BadParameterException;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.manager.channel.ChannelManager;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ErrataChannelIntersectionAction
 * @version $Rev$
 */
public class ErrataChannelIntersectionAction extends RhnAction {

    /**
     * {@inheritDoc}
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm formIn,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        RequestContext requestContext = new RequestContext(request);

        //get the user and page control
        User user = requestContext.getCurrentUser();
        Long cid = requestContext.getRequiredParam("cid");
        Channel channel = ChannelManager.lookupByIdAndUser(cid, user);

        if (channel == null) {
            throw new BadParameterException("Invalid cid parameter:" + cid);
        }

        //Get the errata object
        Errata e = requestContext.lookupErratum();

        request.setAttribute("channel", channel.getLabel());
        request.setAttribute("advisory", e.getAdvisory());
        request.setAttribute(RequestContext.PAGE_LIST, PackageFactory
                .getErrataChannelIntersection(cid, e.getId(), e.isPublished()));

        // forward to page
        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }
}
