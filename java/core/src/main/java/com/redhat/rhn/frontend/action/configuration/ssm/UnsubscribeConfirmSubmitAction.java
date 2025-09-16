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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.action.configuration.ssm;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.rhnset.RhnSetElement;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.configuration.ConfigActionHelper;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnListDispatchAction;
import com.redhat.rhn.manager.configuration.ConfigurationManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * UnsubscribeConfirmSubmitAction
 */
public class UnsubscribeConfirmSubmitAction extends RhnListDispatchAction {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void processMethodKeys(Map<String, String> mapIn) {
        mapIn.put("unsubscribeconfirm.jsp.confirm", "confirm");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void processParamMap(ActionForm formIn,
                                   HttpServletRequest requestIn, Map<String, Object> paramsIn) {
        //no-op
    }

    /**
     * Unsubscribes selected systems from selected config channels.
     * @param mapping struts ActionMapping
     * @param form struts ActionForm
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return forward to the starting Diff page.
     */
    public ActionForward confirm(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) {
        User user = new RequestContext(request).getCurrentUser();
        RhnSet channelSet = RhnSetDecl.CONFIG_CHANNELS.get(user);
        ConfigurationManager cm = ConfigurationManager.getInstance();

        List<ConfigChannel> configChannelList = new ArrayList<>();
        for (RhnSetElement ch : channelSet.getElements()) {
            try {
                configChannelList.add(cm.lookupConfigChannel(user, ch.getElement()));
            }
            catch (LookupException ignored) {
                // Ignore non-existing channels to remove: shouldn't happen
            }
        }

        List<Server> systems = ServerFactory.getSsmSystemsForSubscribe(user);

        systems.forEach(server -> {
            server.unsubscribeConfigChannels(configChannelList, user);
            server.storeConfigChannels();
        });

        RhnSetManager.remove(channelSet); //clear the set
        //now that we have unsubscribed from channels, these other sets may
        //no longer be valid, so delete them too.
        ConfigActionHelper.clearRhnSets(user);

        createMessage(request, systems.size() == 1);
        return mapping.findForward("success");
    }

    private void createMessage(HttpServletRequest request, boolean single) {
        ActionMessages msg = new ActionMessages();
        if (single) {
            msg.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("unsubscribe.ssm.success"));
        }
        else {
            msg.add(ActionMessages.GLOBAL_MESSAGE,
                new ActionMessage("unsubscribe.ssm.successes"));
        }
        getStrutsDelegate().saveMessages(request, msg);
    }

}
