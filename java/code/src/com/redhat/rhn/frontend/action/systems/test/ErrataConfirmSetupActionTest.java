/**
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.systems.test;

import com.redhat.rhn.common.util.DatePicker;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.testing.RhnMockStrutsTestCase;

/**
 * ErrataConfirmSetupActionTest
 */
public class ErrataConfirmSetupActionTest extends RhnMockStrutsTestCase {

    public void testExecute() throws Exception {
        setRequestPathInfo("/systems/details/ErrataConfirm");

        // Create Server
        Server server = ServerFactoryTest.createTestServer(user, true);
        addRequestParameter("sid", server.getId().toString());
        addRequestParameter("allowVendorChange", new String( "false" ));

        //Note: 2 invocations of getParameter("schedule_type") will be called by DatePicker
        addRequestParameter(DatePicker.SCHEDULE_TYPE, DatePicker.ScheduleType.DATE.asString());
        actionPerform();
        assertNotNull(request.getAttribute("system"));
        Server server2 = (Server) request.getAttribute("system");
        assertEquals(server, server2);

    }

}
