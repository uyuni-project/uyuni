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
package com.redhat.rhn.taskomatic.task.test;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.ServerInfo;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.taskomatic.task.MinionCheckin;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.webui.services.impl.SaltService;
import com.suse.salt.netapi.datatypes.target.MinionList;

import org.apache.commons.lang3.time.DateUtils;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.Date;
import java.util.Optional;

/**
 * Tests for {@link MinionCheckin}.
 */
public class MinionCheckinTest extends JMockBaseTestCaseWithUser {

    private int thresholdMax;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.thresholdMax =  Config.get().getInt(ConfigDefaults.SYSTEM_CHECKIN_THRESHOLD) * 86400;
        setImposteriser(ClassImposteriser.INSTANCE);
    }

    /**
     * Test execution MinionCheckin job.
     * Check that we don't perform a check-in on ACTIVE minions.
     *
     * @throws Exception in case of an error
     */
    public void testExecuteOnActiveMinions() throws Exception {
        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        minion1.setMinionId("minion1");

        Optional<MinionServer> minion = MinionServerFactory
                .findByMinionId(minion1.getMinionId());
        assertTrue(minion.isPresent());
        assertEquals(minion.get().getMinionId(), minion1.getMinionId());

        ServerInfo serverInfo = minion1.getServerInfo();
        serverInfo.setCheckin(new Date());
        minion1.setServerInfo(serverInfo);
        TestUtils.saveAndFlush(minion1);

        SaltService saltServiceMock = mock(SaltService.class);

        context().checking(new Expectations() { {
            never(saltServiceMock).checkIn(with(any(MinionList.class)));
        } });

        MinionCheckin minionCheckinJob = new MinionCheckin();
        minionCheckinJob.setSaltService(saltServiceMock);

        minionCheckinJob.execute(null);
    }

    /**
     * Test execution MinionCheckin job.
     * Check that we do perform a test.ping on INACTIVE minions.
     *
     * @throws Exception in case of an error
     */
    public void testExecuteOnInactiveMinions() throws Exception {
        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        minion1.setMinionId("minion1");

        Optional<MinionServer> minion = MinionServerFactory
                .findByMinionId(minion1.getMinionId());
        assertTrue(minion.isPresent());
        assertEquals(minion.get().getMinionId(), minion1.getMinionId());

        ServerInfo serverInfo = minion1.getServerInfo();
        serverInfo.setCheckin(DateUtils.addHours(new Date(), -this.thresholdMax));
        minion1.setServerInfo(serverInfo);
        TestUtils.saveAndFlush(minion1);

        SaltService saltServiceMock = mock(SaltService.class);

        context().checking(new Expectations() { {
            oneOf(saltServiceMock).checkIn(with(any(MinionList.class)));
        } });

        MinionCheckin minionCheckinJob = new MinionCheckin();
        minionCheckinJob.setSaltService(saltServiceMock);

        minionCheckinJob.execute(null);
    }

}
