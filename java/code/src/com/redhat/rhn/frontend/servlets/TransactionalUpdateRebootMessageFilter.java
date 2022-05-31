/*
 * Copyright (c) 2022 SUSE LLC
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
package com.redhat.rhn.frontend.servlets;

import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.StrutsDelegate;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

/**
 * This class implements a WebFilter that applies to all system details pages and adds the transactional update reboot
 * alert if necessary, considering whether the system supports Transactional Update.
 */
@WebFilter("/systems/details/*")
public class TransactionalUpdateRebootMessageFilter implements Filter {

    private static final String MESSAGE_KEY = "overview.jsp.transactionalupdate.reboot";

    @Override
    public void doFilter(
        ServletRequest request,
        ServletResponse response,
        FilterChain chain
    ) throws ServletException, IOException {
        chain.doFilter(request, response);
        HttpServletRequest req = (HttpServletRequest) request;
        if (!isMessageAlreadyPresent(req)) {
            RequestContext rctx = new RequestContext(req);
            Long sid = rctx.getRequiredParam("sid");
            User user = rctx.getCurrentUser();
            Server s  = SystemManager.lookupByIdAndUser(sid, user);
            s.asMinionServer().ifPresent(minion -> {
                if (minion.doesOsSupportsTransactionalUpdate()) {
                    ActionErrors errs = new ActionErrors();
                    errs.add(
                        ActionMessages.GLOBAL_MESSAGE,
                        new ActionMessage(MESSAGE_KEY)
                    );
                    StrutsDelegate.getInstance().saveMessages(req, errs);
                }
            });
        }
    }

    /**
     * Checks if the message is already present in the session, to avoid duplicate alerts
     */
    private boolean isMessageAlreadyPresent(HttpServletRequest request) {
        Object sessionErrs = request.getSession().getAttribute(Globals.ERROR_KEY);
        if (sessionErrs != null) {
            Iterator<ActionMessage> i = ((ActionMessages) sessionErrs).get(ActionMessages.GLOBAL_MESSAGE);
            while (i.hasNext()) {
                if (i.next().getKey().equals(MESSAGE_KEY)) {
                    return true;
                }
            }
        }
        return false;
    }
}
