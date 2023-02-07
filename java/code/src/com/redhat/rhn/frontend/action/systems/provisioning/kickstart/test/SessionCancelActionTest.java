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
package com.redhat.rhn.frontend.action.systems.provisioning.kickstart.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;

import org.junit.jupiter.api.Test;

import servletunit.HttpServletRequestSimulator;

/**
 * SessionCancelActionTest
 */
public class SessionCancelActionTest extends BaseSessionTestCase {

    @Test
    public void testExecute() {
        setRequestPathInfo("/systems/details/kickstart/SessionCancel");
        actionPerform();
        assertNotNull(request.getAttribute(RequestContext.SYSTEM));
        assertNotNull(request.getAttribute(RequestContext.KICKSTART_SESSION));
    }

    @Test
    public void testExecuteSubmit() {
        addRequestParameter(RhnAction.SUBMITTED, Boolean.TRUE.toString());
        setRequestPathInfo("/systems/details/kickstart/SessionCancel");
        request.setMethod(HttpServletRequestSimulator.POST);
        actionPerform();
        verifyActionMessage("kickstart.session_cancel.success");
        assertEquals("/systems/details/kickstart/SessionStatus.do?sid=" + s.getId(),
                getActualForward());
    }
}

