/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.kickstart.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.frontend.action.kickstart.KickstartDetailsEditAction;
import com.redhat.rhn.frontend.struts.RequestContext;

import org.junit.jupiter.api.Test;

public class KickstartDeleteActionTest extends BaseKickstartEditTestCase {

    private static final String KICKSTART_ID = "ksid";

    @Test
    public void testExecute() {
        setRequestPathInfo("/kickstart/KickstartDelete");
        addRequestParameter(KickstartDetailsEditAction.COMMENTS, "test comment");
        addRequestParameter(KickstartDetailsEditAction.LABEL, "test label");
        addRequestParameter(KickstartDetailsEditAction.ACTIVE, Boolean.TRUE.toString());
        actionPerform();
        assertNotNull(request.getAttribute(RequestContext.KICKSTART));
    }

    @Test
    public void testSubmit() throws Exception {
        setRequestPathInfo("/kickstart/KickstartDelete");
        addRequestParameter(KickstartDetailsEditAction.SUBMITTED,
                                               Boolean.TRUE.toString());
        addRequestParameter(KickstartDetailsEditAction.COMMENTS, "test comment");
        addRequestParameter(KickstartDetailsEditAction.LABEL, "test label");
        addRequestParameter(KickstartDetailsEditAction.ACTIVE, Boolean.TRUE.toString());
        actionPerform();

        String[] keys = {"kickstart.delete.success"};
        verifyActionMessages(keys);

        assertNull(KickstartFactory.lookupKickstartDataByIdAndOrg(user.getOrg(),
                Long.valueOf(request.getParameter(KICKSTART_ID))));
    }
}
