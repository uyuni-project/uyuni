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

import com.redhat.rhn.common.util.DatePicker;
import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.ActionChainHelper;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.ListRhnSetHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.Listable;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.system.SystemManager;

import com.redhat.rhn.taskomatic.TaskomaticApiException;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

/**
 *
 * TargetSystemsConfirmAction
 * @version $Rev$
 */
public class TargetSystemsConfirmAction extends RhnAction implements Listable {

    private static final Logger LOG = Logger.getLogger(TargetSystemsConfirmAction.class);

    /**
     *
     * {@inheritDoc}
     */
    public ActionForward execute(ActionMapping mapping,
            ActionForm formIn,
            HttpServletRequest request,
            HttpServletResponse response) {

        RequestContext requestContext = new RequestContext(request);
        User user =  requestContext.getCurrentUser();
        DynaActionForm form = (DynaActionForm) formIn;

        Long cid = requestContext.getRequiredParam(RequestContext.CID);
        Channel chan = ChannelManager.lookupByIdAndUser(cid, user);
        request.setAttribute("channel_name", chan.getName());
        ListRhnSetHelper helper = new ListRhnSetHelper(this, request,
                TargetSystemsAction.getSetDecl(chan));
        helper.setWillClearSet(false);
        helper.execute();

        request.setAttribute("channel_name", chan.getName());
        request.setAttribute("cid", chan.getId());
        request.setAttribute("date", this.getStrutsDelegate().prepopulateDatePicker(
                request, form, "date", DatePicker.YEAR_RANGE_POSITIVE));
        ActionChainHelper.prepopulateActionChains(request);

        if (helper.isDispatched()) {
            RhnSet set = TargetSystemsAction.getSetDecl(chan).get(user);
            List<Server> servers = new ArrayList<Server>();
            for (Long id : set.getElementValues()) {
                Server s  = SystemManager.lookupByIdAndUser(id, user);
                servers.add(s);
            }
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(RequestContext.CID, cid);
            try {
                scheduleSubscribeChannels(form, user, servers, chan, request);
            }
            catch (TaskomaticApiException e) {
                LOG.error("Could not schedule subscribe to channel " + chan.getName(), e);
                ActionErrors errors = new ActionErrors();
                getStrutsDelegate().addError("taskscheduler.down", errors);
                getStrutsDelegate().saveMessages(request, errors);
                request.setAttribute(ListTagHelper.PARENT_URL, request.getRequestURI() +
                         "?" + request.getQueryString());
            }

            return getStrutsDelegate().forwardParams(mapping.findForward("success"),
                    params);
        }
        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    private void scheduleSubscribeChannels(DynaActionForm form, User user, List<Server> servers, Channel channel,
                                           HttpServletRequest request)
            throws TaskomaticApiException {
        if (channel.isBaseChannel()) {
            LOG.error("Subscribing to base channel not allowed");
            getStrutsDelegate().saveMessage("base.channel.subscribe.not.allowed", request);
            return;
        }

        Date scheduleDate = getStrutsDelegate().readDatePicker(
                form, "date", DatePicker.YEAR_RANGE_POSITIVE);
        ActionChain actionChain = ActionChainHelper.readActionChain(form, user);

        for (Server server : servers) {
            Set<Channel> childChannels = new HashSet<>();
            childChannels.addAll(server.getChildChannels());
            childChannels.add(channel);
            ActionChainManager.scheduleSubscribeChannelsAction(user,
                    singleton(server.getId()),
                    Optional.ofNullable(server.getBaseChannel()),
                    !channel.isBaseChannel() ? childChannels : emptySet(),
                    scheduleDate, actionChain);
        }

        ActionMessages msgs = new ActionMessages();
        if (actionChain == null && servers.size() > 0) {
            msgs.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("channels.subscribe.target.systems.channel.scheduled",
                            servers.size(),
                            channel.getName()));
            getStrutsDelegate().saveMessages(request, msgs);
        }
        else {
            msgs.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("message.addedtoactionchain", actionChain.getId(),
                            StringUtil.htmlifyText(actionChain.getLabel())));
            getStrutsDelegate().saveMessages(request, msgs);
        }
    }

    /**
     *
     * {@inheritDoc}
     */
    public List getResult(RequestContext context) {
        User user =  context.getCurrentUser();
        Long cid = context.getRequiredParam(RequestContext.CID);
        Channel chan = ChannelManager.lookupByIdAndUser(cid, user);
        return SystemManager.inSet(user, TargetSystemsAction.getSetDecl(chan).getLabel());
    }

}
