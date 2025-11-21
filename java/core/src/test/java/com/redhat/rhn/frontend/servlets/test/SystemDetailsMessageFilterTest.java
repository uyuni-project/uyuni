/*
 * Copyright (c) 2022--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.frontend.servlets.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.servlets.SystemDetailsMessageFilter;
import com.redhat.rhn.testing.MockObjectTestCase;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;
import com.redhat.rhn.testing.UserTestUtils;

import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

public class SystemDetailsMessageFilterTest extends MockObjectTestCase {
    private User user;

    @BeforeEach
    public void setUp() {
        user = UserTestUtils.createUser(this);
    }

    @Test
    public void shouldAddTraditionalStackDeprecationMessage() {
        // The server is created as a traditional system (enterprise entitled)
        Server server = ServerFactoryTest.createTestServer(user, false,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
        HttpServletRequest request = new RhnMockHttpServletRequest();
        SystemDetailsMessageFilter filter = new SystemDetailsMessageFilter();
        filter.processSystemMessages(request, server);

        ActionMessages messages = (ActionMessages) request.getSession().getAttribute("org.apache.struts.action.ERROR");
        @SuppressWarnings("unchecked")
        Iterator<ActionMessage> globalMessagesIterator = messages.get(ActionMessages.GLOBAL_MESSAGE);

        ActionMessage message = globalMessagesIterator.next();
        assertEquals(SystemDetailsMessageFilter.TRADITIONAL_STACK_MESSAGE_KEY, message.getKey());
        // Ensure only one message is present
        assertFalse(globalMessagesIterator.hasNext());
    }

    @Test
    public void shouldNotAddTraditionalStackDeprecationMessage() {
        // The server is created as a salt system (salt entitled)
        MinionServer minionServer = MinionServerFactoryTest.createTestMinionServer(user);
        HttpServletRequest request = new RhnMockHttpServletRequest();
        SystemDetailsMessageFilter filter = new SystemDetailsMessageFilter();
        filter.processSystemMessages(request, minionServer);
        ActionMessages messages =
                ((ActionMessages) request.getSession().getAttribute("org.apache.struts.action.ERROR"));
        assertNull(messages);
    }
}
