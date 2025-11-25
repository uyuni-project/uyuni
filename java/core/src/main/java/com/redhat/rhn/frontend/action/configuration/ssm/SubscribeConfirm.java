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
package com.redhat.rhn.frontend.action.configuration.ssm;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.rhnset.RhnSetElement;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.common.BadParameterException;
import com.redhat.rhn.frontend.action.configuration.ConfigActionHelper;
import com.redhat.rhn.frontend.action.configuration.ConfigChannelSetComparator;
import com.redhat.rhn.frontend.dto.ConfigChannelDto;
import com.redhat.rhn.frontend.dto.ConfigSystemDto;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.manager.configuration.ConfigurationManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * SubscribeConfirmSetup, for ssm config subscribe.
 */
public class SubscribeConfirm extends RhnAction {

    private static final Logger LOG = LogManager.getLogger(SubscribeConfirm.class);

    public static final String REPLACE = "replace";
    public static final String LOWEST = "lowest";
    public static final String HIGHEST = "highest";
    public static final String POSITION = "position";

    /**
     * Set up the page.
     * @param mapping struts ActionMapping
     * @param formIn struts ActionForm
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return an ActionForward to the same page
     */
    @Override
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm formIn, HttpServletRequest request,
                                 HttpServletResponse response) {
        //check that we have a viable priority
        checkPosition(request);

        //typical stuff
        RequestContext context = new RequestContext(request);
        User user = context.getCurrentUser();

        //Decide whether we are visiting the same page or
        //performing the subscribe.
        String dispatch = request.getParameter("dispatch");
        if (dispatch != null && dispatch.equals(LocalizationService
                .getInstance().getMessage("ssm.config.subscribeconfirm.jsp.confirm"))) {
            return confirm(mapping, request, user);
        }
        return setup(mapping, request, user);

    }

    private ActionForward setup(ActionMapping mapping,
            HttpServletRequest request, User user) {
        //Get the data
        ConfigurationManager cm = ConfigurationManager.getInstance();
        List<ConfigChannelDto> channels = cm.ssmChannelsInSetForSubscribe(user);
        List<ConfigSystemDto> systems = cm.ssmSystemsForSubscribe(user);

        //Create the parent url. Copy the one important parameter.

        //store the data so the list tag can see it
        String parentUrl = request.getRequestURI() + "?" + POSITION + "=" + request.getParameter(POSITION);
        request.setAttribute(ListTagHelper.PARENT_URL, parentUrl);
        request.setAttribute("channelList", channels);
        request.setAttribute("systemList", systems);

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    /**
     * Subscribe to config channels
     * @param mapping struts ActionMapping
     * @param request HttpServletRequest
     * @param user The user confirming config subscriptions.
     * @return an ActionForward to a page with a success message.
     */
    private ActionForward confirm(ActionMapping mapping,
            HttpServletRequest request, User user) {
        //get where we are supposed to put new channels
        //validate the position parameter
        String position = request.getParameter(POSITION);
        checkPosition(request);

        List<Server> systems = ServerFactory.getSsmSystemsForSubscribe(user);

        //visit every server and change their subscriptions
        //keep track of how many servers we have changed
        LocalDateTime start = LocalDateTime.now();

        // Order the new channels in the order requested by user
        ConfigurationManager cm = ConfigurationManager.getInstance();
        List<RhnSetElement> rankElements = new ArrayList<>(RhnSetDecl.CONFIG_CHANNELS_RANKING.get(user).getElements());
        rankElements.sort(new ConfigChannelSetComparator());

        RhnSet selectedChannels = RhnSetDecl.CONFIG_CHANNELS.get(user);

        List<ConfigChannel> configChannels = rankElements.stream()
                .filter(elm -> selectedChannels.contains(elm.getElement()))
                .map(elm -> cm.lookupConfigChannel(user, elm.getElement()))
                .collect(Collectors.toList());

        LOG.debug("Start adding config state");
        int successes = 0;
        for (Server server : systems) {
            if (subscribeServer(user, server, configChannels, position)) {
                successes++;
            }
        }
        LOG.debug("End adding config state, duration: {}", Duration.between(start, LocalDateTime.now()));

        ConfigActionHelper.clearRhnSets(user);

        //Give the user a message about how many servers we have changed.
        if (successes == 1) {
            getStrutsDelegate().saveMessage("ssm.config.subscribeconfirm.jsp.onesuccess",
                    request);
        }
        else {
            String[] params = {Integer.toString(successes)};
            getStrutsDelegate().saveMessage("ssm.config.subscribeconfirm.jsp.success",
                    params, request);
        }

        return mapping.findForward("success");
    }

    private boolean subscribeServer(User user, Server server, List<ConfigChannel> channels, String position) {

        // Position:
        //   LOWEST: Subscribe to new channels at the lowest priority
        //   REPLACE: Replace subscriptions with the new list
        //   HIGHEST: Subscribe to new channels at the highest priority

        List<ConfigChannel> existingChannels = server.getConfigChannelList();
        ArrayList<ConfigChannel> newChannels = new ArrayList<>(channels);

        if (LOWEST.equals(position)) {
            // Subscribe the new channels in the end. No re-ordering needed.
            newChannels.removeAll(existingChannels);
            if (!newChannels.isEmpty()) {
                server.subscribeConfigChannels(newChannels, user);
                server.storeConfigChannels();
                return true;
            }
        }
        else {
            // Previously subscribed channels to append in the end,
            // or nothing in case of REPLACE
            if (HIGHEST.equals(position)) {
                newChannels.addAll(existingChannels);
            }

            if (!newChannels.equals(existingChannels)) {
                // Replace config channel subscriptions with the new list
                server.setConfigChannels(newChannels, user);
                server.storeConfigChannels();
                return true;
            }
        }

        // Subscriptions have not changed for this server
        return false;
    }

    private void checkPosition(HttpServletRequest request) {
        String position = request.getParameter(POSITION);
        String[] valids = {REPLACE, LOWEST, HIGHEST};
        if (!Arrays.asList(valids).contains(position)) {
            throw new BadParameterException("Invalid position value!");
        }
    }

}
