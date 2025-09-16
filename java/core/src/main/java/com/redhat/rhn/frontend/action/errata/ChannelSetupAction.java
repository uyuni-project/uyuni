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
package com.redhat.rhn.frontend.action.errata;

import static com.redhat.rhn.frontend.struts.RequestContext.ERRATA_ID;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.errata.ClonedErrata;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.rhnset.RhnSetElement;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ChannelOverview;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.RhnListAction;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ChannelSetupAction
 */
public class ChannelSetupAction extends RhnListAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm formIn,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        RequestContext requestContext = new RequestContext(request);

        //get the user and page control
        User user = requestContext.getCurrentUser();
        PageControl pc = new PageControl();

        clampListBounds(pc, request, user);

        //Get the errata object
        Long errataId = requestContext.getParamAsLong(ERRATA_ID);
        Errata e = ErrataManager.lookupErrata(errataId, requestContext.getCurrentUser());

        //get the data result containing the channels in this org
        DataResult<ChannelOverview> channels =
                ChannelManager.channelsOwnedByOrg(user.getOrg().getId(), pc);

        //loop through the ones that will be displayed on the page, and get
        //the number of relevant packages.
        for (ChannelOverview channel : channels) {
            List<Long> pkgs;
            //so if the channel is not a clone or the errata is not cloned, we simply allow
            //the package name match to be used
            if (e != null && (channel.getOriginalId() == null || !e.isCloned())) {
                pkgs = ChannelManager.relevantPackages(channel.getId(), e);
            }
            //Else we check and see if the original channel was listed in
            //      the original errata
            else if (e != null && (e.isCloned() && errataInChannel(((ClonedErrata) e).getOriginal(),
                    channel.getOriginalId()))) {
                pkgs = ChannelManager.relevantPackages(channel.getId(), e);
            } //if it wasn't then no packages are listed
            else {
                pkgs = new ArrayList<>();
            }

            if (pkgs.isEmpty()) { //There must be 0 relevant packages
                channel.setRelevantPackages(0L);
            }
            else { //set relevantPackages to the number of items in the data result
                channel.setRelevantPackages((long) pkgs.size());
            }
        }

        //get the set to start with
        RhnSet set = RhnSetDecl.CHANNELS_FOR_ERRATA.get(user);
        /*
         * If returnvisit is null, this is our first visit to a channels page. We
         * need to intialize the set in the db to contain the ids of the channels
         * that are in the channels set for this errata.
         */
        if (request.getParameter("returnvisit") == null) {
            //this must be our first visit
            request.setAttribute("returnvisit", "true");
            //init the set
            set = RhnSetDecl.CHANNELS_FOR_ERRATA.create(user);
            // If e has already been saved, it must already have channels and needs it's set init
            if (e != null) {
                //get the channels for this errata
                Set<Channel> channelsInErrata = e.getChannels();
                //loop through and add them to the set
                for (Channel channelsInErratumIn : channelsInErrata) {
                    Long cid = channelsInErratumIn.getId(); //get channel id
                    set.addElement(cid); //add to set
                }
            }
            //Store the set here. It should either be empty or contain the channels
            //for this errata.
            RhnSetManager.store(set);
        }
        /*
         * If setupdated exists, then we have just called either update list, select all,
         * or unselect all. Either way, we need to init the set to what is in the db.
         */
        else if (request.getParameter("setupdated") != null) {
            List<String> ids = new ArrayList<>();
            Set<RhnSetElement> elems =
                    RhnSetDecl.CHANNELS_FOR_ERRATA.get(user).getElements();
            for (RhnSetElement elemIn : elems) {
                //get a string version of the id from the RhnSetElment object
                String id = elemIn.getElement().toString();
                ids.add(id); //add to the list
            }
        }

        request.setAttribute(RequestContext.PAGE_LIST, channels);
        request.setAttribute("set", set);
        //set advisory for toolbar
        request.setAttribute("advisory", e != null ? e.getAdvisory() : "");

        // forward to page
        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    private boolean errataInChannel(Errata e, Long id) {
        for (Channel chan : e.getChannels()) {
            if (chan.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }


}
