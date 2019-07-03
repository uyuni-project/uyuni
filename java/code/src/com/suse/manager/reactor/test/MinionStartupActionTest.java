/**
 * Copyright (c) 2019 SUSE LLC
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
package com.suse.manager.reactor.test;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.suse.manager.reactor.messaging.MinionStartEventMessage;
import com.suse.manager.reactor.messaging.MinionStartEventMessageAction;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.salt.netapi.datatypes.target.MinionList;
import org.cobbler.test.MockConnection;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;

/**
 * Tests for {@link MinionStartEventMessageAction}.
 */
public class MinionStartupActionTest extends JMockBaseTestCaseWithUser {

    private static final String MINION_ID = "suma3pg.vagrant.local";
    private SaltService saltServiceMock;
   
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);
        MockConnection.clear();
        saltServiceMock = mock(SaltService.class);
    }

   
    public void testStarupEventFired() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setMinionId(MINION_ID);
        // Verify the resulting system entry
        context().checking(new Expectations(){{
            allowing(saltServiceMock).updateSystemInfo(with(any(MinionList.class)));
        }});
        // On minion start up apply state via mocked SaltService
        MinionStartEventMessageAction action = new MinionStartEventMessageAction(saltServiceMock);
        action.execute(new MinionStartEventMessage(MINION_ID));
    }   
}
