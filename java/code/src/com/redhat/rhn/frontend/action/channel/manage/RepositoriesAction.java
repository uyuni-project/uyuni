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
package com.redhat.rhn.frontend.action.channel.manage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.StrutsDelegate;
import com.redhat.rhn.frontend.taglibs.list.helper.ListSessionSetHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.Listable;

/**
 *
 * RepositoriesAction
 * @version $Rev$
 */
public class RepositoriesAction extends RhnAction implements Listable {

    /**
     *
     * {@inheritDoc}
     */
    public ActionForward execute(ActionMapping mapping,
            ActionForm formIn,
            HttpServletRequest request,
            HttpServletResponse response) {

        RequestContext context = new RequestContext(request);
        User user =  context.getCurrentUser();

        long cid = context.getRequiredParam("cid");
        Channel chan = ChannelFactory.lookupByIdAndUser(cid, user);
        request.setAttribute("channel_name", chan.getName());
        request.setAttribute("cid", chan.getId());

        Map<String, Object> params = new HashMap<String, Object>();
        params.put(RequestContext.CID, chan.getId().toString());

        ListSessionSetHelper helper =
            new ListSessionSetHelper(this, request, params);

        if (!context.isSubmitted()) {
            List<ContentSource> result = getResult(context);
            Set<String> preSelect = new HashSet<String>();
            for (int i = 0; i < result.size(); i++) {
                ContentSource src = result.get(i);
                if (src.getChannels().contains(chan)) {
                    preSelect.add(src.getId().toString());
                }
            }
            helper.preSelect(preSelect);
        }

        helper.ignoreEmptySelection();
        helper.execute();

        if (helper.isDispatched()) {
            Set<ContentSource> foo = chan.getSources();
            foo.clear();
            Set <String> set = helper.getSet();
            for (String id : set) {
                Long sgid = Long.valueOf(id);
                ContentSource tmp = ChannelFactory.lookupContentSource(sgid, user.getOrg());
                foo.add(tmp);
            }

            ChannelFactory.save(chan);

            StrutsDelegate strutsDelegate = getStrutsDelegate();
            strutsDelegate.saveMessage("channel.edit.repo.updated",
                    new String[] {chan.getName()}, request);

            return strutsDelegate.forwardParams
            (mapping.findForward("success"), params);
        }

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

        /**
         *
         * {@inheritDoc}
         */
        public List<ContentSource> getResult(RequestContext context) {
            User user =  context.getCurrentUser();
            return ChannelFactory.lookupContentSources(user.getOrg());
        }
}
