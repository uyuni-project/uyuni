/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.systems.provisioning.kickstart;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartSession;
import com.redhat.rhn.domain.kickstart.KickstartSessionHistory;
import com.redhat.rhn.domain.kickstart.KickstartSessionState;
import com.redhat.rhn.domain.kickstart.KickstartVirtualizationType;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.systems.sdc.SdcHelper;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * SessionStatusAction
 */
public class SessionStatusAction extends RhnAction {

    private static final int GUEST_TIME_OUT_MINUTES = 15;

    /** {@inheritDoc} */
    @Override
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm formIn,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        RequestContext ctx = new RequestContext(request);
        User currentUser = ctx.getCurrentUser();
        Long serverId = ctx.getRequiredParam(RequestContext.SID);
        Server s = ServerFactory.lookupByIdAndOrg(serverId, currentUser.getOrg());
        if (s == null) {
            createSuccessMessage(request, "message.serverdeleted.param",
                    serverId.toString());
            return mapping.findForward("systems");
        }

        request.setAttribute(RequestContext.SYSTEM, s);
        KickstartSession kss = KickstartFactory.lookupKickstartSessionByServer(s.getId());

        // Add the history items as "TRUE" values
        // to the request so the display can render or not
        // if this status has occurred.
        for (Object oIn : kss.getHistory()) {
            KickstartSessionHistory hist = (KickstartSessionHistory) oIn;
            request.setAttribute(hist.getState().getLabel(), Boolean.TRUE);
        }

        request.setAttribute(RequestContext.KICKSTART_SESSION, kss);
        request.setAttribute(RequestContext.KICKSTART, kss.getKsdata());

        // When kickstarting guests, we will display some special information
        // for debugging if things are taking too long:
        String stateDesc = null;
        if (kss.getVirtualizationType().getLabel().equals(
                KickstartVirtualizationType.XEN_PARAVIRT)) {

            String ksStateLabel = kss.getState().getLabel();

            // Only display the time-out message if the kickstart is in the
            // STARTED or IN_PROGRESS phase.
            if (ksStateLabel.equals(KickstartSessionState.STARTED) ||
                ksStateLabel.equals(KickstartSessionState.IN_PROGRESS)) {

                // Get the last modified on the rhnKickstartSession
                long lastFileRequestMillis =
                    kss.getModified().getTime();
                long nowMillis = System.currentTimeMillis();
                long timeoutMillis = GUEST_TIME_OUT_MINUTES * // minutes
                                     60                     * // seconds
                                     1000;                    // millis

                // check timout and file requested time is current
                long sinceLastFileRequestMillis = nowMillis - lastFileRequestMillis;

                if (sinceLastFileRequestMillis > timeoutMillis) {
                    long sinceMinutes = sinceLastFileRequestMillis / // millis
                                        1000        / // seconds
                                        60;           // minutes
                    stateDesc =
                        LocalizationService.getInstance().getMessage(
                            "kickstart.state.guesttimedout",
                                                sinceMinutes);
                }
            }
        }
        if (stateDesc == null) {
            String key = "kickstart.state." + kss.getState().getLabel();
            stateDesc = LocalizationService.getInstance().getMessage(key);
        }
        request.setAttribute(RequestContext.KICKSTART_STATE_DESC, stateDesc);
        request.setAttribute(RequestContext.SID, request.getParameter(RequestContext.SID));
        DynaActionForm form = (DynaActionForm) formIn;
        if (form != null) {
            // Used to remember scroll positions
            Integer xPos = (Integer)form.get("xPosition");
            Integer yPos = (Integer)form.get("yPosition");
            request.setAttribute("scrollX", xPos);
            request.setAttribute("scrollY", yPos);
        }
        SdcHelper.ssmCheck(ctx.getRequest(), s.getId(), currentUser);
        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

}
