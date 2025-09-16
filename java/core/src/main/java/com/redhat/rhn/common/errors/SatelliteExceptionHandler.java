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
package com.redhat.rhn.common.errors;


import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.events.TraceBackEvent;
import com.redhat.rhn.frontend.struts.RequestContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;


/**
 * SatelliteExceptionHandler
 */
public class SatelliteExceptionHandler extends ExceptionHandler {
    private Exception exception;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void logException(Exception ex) {
        Logger log = LogManager.getLogger(LookupExceptionHandler.class);
        log.error(ex);
        exception = ex;
    }

    @Override
    protected void storeException(HttpServletRequest request, String property,
                                  ActionMessage msg, ActionForward forward, String scope) {
        TraceBackEvent evt = new TraceBackEvent();
        RequestContext requestContext = new RequestContext(request);
        User usr = requestContext.getCurrentUser();
        evt.setUser(usr);
        evt.setRequest(request);
        evt.setException(exception);
        MessageQueue.publish(evt);
        request.setAttribute("error", exception);
    }
}
