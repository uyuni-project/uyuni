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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.action.user;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.access.AccessGroupFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.common.BadParameterException;
import com.redhat.rhn.frontend.dto.ChannelPerms;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.RhnListAction;
import com.redhat.rhn.manager.user.UserManager;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * AddressesAction Setup the Addresses on the Request so
 * the AddressTag will be able to render
 */
public class ChannelPermsSetupAction extends RhnListAction {

    /** {@inheritDoc} */
    @Override
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm formIn,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        RequestContext requestContext = new RequestContext(request);

        Long uid = requestContext.getParamAsLong("uid");
        DynaActionForm form = (DynaActionForm)formIn;
        User user = UserManager.lookupUser(requestContext.getCurrentUser(), uid);
        if (user == null) {
            throw new BadParameterException("Invalid uid");
        }

        request.setAttribute(RhnHelper.TARGET_USER, user);


        PageControl pc = new PageControl();
        pc.setIndexData(true);
        pc.setFilterColumn("name");
        pc.setFilter(true);

        clampListBounds(pc, request, requestContext.getCurrentUser());

        DataResult<ChannelPerms> dr = UserManager.channelSubscriptions(user, pc);

        request.setAttribute(RequestContext.PAGE_LIST, dr);
        request.setAttribute("user", user);
        request.setAttribute("role", "subscribe");
        request.setAttribute("userIsChannelAdmin",
                user.isMemberOf(AccessGroupFactory.CHANNEL_ADMIN));
        form.set("selectedChannels", dr.stream()
                .filter(ChannelPerms::isHasPerm)
                .map(p -> String.valueOf(p.getId())).toArray(String[]::new));

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }
}
