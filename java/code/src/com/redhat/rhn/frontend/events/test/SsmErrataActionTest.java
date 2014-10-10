/**
 * Copyright (c) 2014 SUSE
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

package com.redhat.rhn.frontend.events.test;

import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper
    .createLaterTestPackage;
import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper
    .createTestInstalledPackage;
import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.createTestPackage;

import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.errata.ErrataAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.frontend.events.SsmErrataAction;
import com.redhat.rhn.frontend.events.SsmErrataEvent;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import org.hibernate.criterion.Restrictions;

import java.util.*;

/**
 * Tests SsmErrataAction.
 */
public class SsmErrataActionTest extends BaseTestCaseWithUser {

    /**
     * Test only relevant errata per system.
     *
     * @throws Exception the exception
     */
    @SuppressWarnings("unchecked")
    public void testOnlyRelevantErrataPerSystem() throws Exception {

        // Create System
        Server server1 = ServerFactoryTest.createTestServer(user, true);
        Server server2 = ServerFactoryTest.createTestServer(user, true);

        Errata errata1 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        Errata errata2 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        Errata errata3 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());

        Channel channel1 = ChannelFactoryTest.createTestChannel(user);
        Channel channel2 = ChannelFactoryTest.createTestChannel(user);
        Channel channel3 = ChannelFactoryTest.createTestChannel(user);

        // server 1 has an errata for package1 available
        com.redhat.rhn.domain.rhnpackage.Package package1 =
                createTestPackage(user, channel1, "noarch");
        createTestInstalledPackage(package1, server1);
        createLaterTestPackage(user, errata1, channel1, package1);

        // server 2 has an errata for package2 available
        Package package2 = createTestPackage(user, channel2, "noarch");
        createTestInstalledPackage(package2, server2);
        createLaterTestPackage(user, errata2, channel2, package2);

        // errata in common for both servers
        Package package3 = createTestPackage(user, channel3, "noarch");
        createTestInstalledPackage(package3, server1);
        createTestInstalledPackage(package3, server2);
        createLaterTestPackage(user, errata3, channel3, package3);

        //ErrataCacheManager.insertNeededErrataCache(
        //        server1.getId(), errata1.getId(), package1.getId());
        //ErrataCacheManager.insertNeededErrataCache(
        //        server2.getId(), errata2.getId(), package2.getId());
        //ErrataCacheManager.insertNeededErrataCache(
        //        server1.getId(), errata3.getId(), package3.getId());
        //ErrataCacheManager.insertNeededErrataCache(
        //        server2.getId(), errata3.getId(), package3.getId());

        List<Long> errataIds = new ArrayList<Long>();
        errataIds.add(errata1.getId());
        errataIds.add(errata2.getId());
        errataIds.add(errata3.getId());

        List<Long> serverIds = new ArrayList<Long>();
        serverIds.add(server1.getId());
        serverIds.add(server2.getId());

        SsmErrataAction action = new SsmErrataAction();
        SsmErrataEvent event = new SsmErrataEvent(user.getId(), new Date(),
                null, errataIds, serverIds);

        action.execute(event);

        // we want to check that no matter how many actions were scheduled for
        // server1, all the erratas included in those scheduled actions for
        // server1 do not contain the erratas for server2

        List<Action> actionsServer1 = ActionFactory.listActionsForServer(user, server1);
        Set<Long> server1ScheduledErrata = new HashSet<Long>();
        for (Action a : actionsServer1) {
            ErrataAction errataAction = errataActionFromAction(a);
            for (Errata e : errataAction.getErrata()) {
                server1ScheduledErrata.add(e.getId());
            }
        }

        List<Action> actionsServer2 = ActionFactory.listActionsForServer(user, server2);
        Set<Long> server2ScheduledErrata = new HashSet<Long>();
        for (Action a : actionsServer2) {
            ErrataAction errataAction = errataActionFromAction(a);
            for (Errata e : errataAction.getErrata()) {
                server2ScheduledErrata.add(e.getId());
            }
        }

        assertEquals("Server 1 Scheduled Erratas has 1 erratas",
                1, server1ScheduledErrata.size());
        assertFalse("Server 1 Scheduled Erratas do not include other server's errata",
                server1ScheduledErrata.contains(errata2.getId()));
        assertTrue("Server 1 Scheduled Erratas contain relevant erratas",
                server1ScheduledErrata.contains(errata1.getId()));
        assertTrue("Server 1 Scheduled Erratas contain relevant erratas",
                server1ScheduledErrata.contains(errata3.getId()));

        assertFalse("Server 2 Scheduled Erratas do not include other server's errata",
                server2ScheduledErrata.contains(errata1.getId()));
        assertTrue("Server 2 Scheduled Erratas contain relevant erratas",
                server2ScheduledErrata.contains(errata2.getId()));
        assertTrue("Server 2 Scheduled Erratas contain relevant erratas",
                server2ScheduledErrata.contains(errata3.getId()));

    }

    /**
     * Get an ErrataAction from an Action.
     * @param action the action
     * @return the errata action
     */
    private ErrataAction errataActionFromAction(Action action) {
        ErrataAction errataAction = (ErrataAction) HibernateFactory.getSession()
            .createCriteria(ErrataAction.class)
            .add(Restrictions.idEq(action.getId()))
            .uniqueResult();
        return errataAction;
    }
}
