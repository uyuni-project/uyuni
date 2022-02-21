/*
 * Copyright (c) 2021 SUSE LLC
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

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModuleStreams;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModulemdApi;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModulemdApiException;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * AppStreamsAction
 */
public class AppStreamsAction extends RhnAction {

    /** {@inheritDoc} */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm formIn,
            HttpServletRequest request, HttpServletResponse response) {

        RequestContext requestContext = new RequestContext(request);
        User user = requestContext.getCurrentUser();

        long cid = requestContext.getRequiredParam("cid");
        Channel chan = ChannelFactory.lookupByIdAndUser(cid, user);

        if (chan.isModular()) {
            ModulemdApi api = new ModulemdApi();

            try {
                Map<String, ModuleStreams> result = api.getAllModulesInChannel(chan);
                List<Entry<String, ModuleStreams>> resultList = new ArrayList<>(result.entrySet());
                request.setAttribute(RequestContext.PAGE_LIST, resultList);
            }
            catch (ModulemdApiException e) {
                throw new RuntimeException("Cannot get channel modules.", e);
            }
        }

        request.setAttribute("cid", chan.getId());
        request.setAttribute("channel_name", chan.getName());
        request.setAttribute(ListTagHelper.PARENT_URL,
                request.getRequestURI() + "?" + request.getQueryString());
        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);

    }

}
