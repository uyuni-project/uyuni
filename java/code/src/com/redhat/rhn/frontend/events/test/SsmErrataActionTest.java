package com.redhat.rhn.frontend.events.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.errata.ErrataAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.rhnpackage.*;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.frontend.events.SsmErrataAction;
import com.redhat.rhn.frontend.events.SsmErrataEvent;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import org.hibernate.criterion.Restrictions;

import java.util.*;

import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.createLaterTestPackage;
import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.createTestInstalledPackage;
import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.createTestPackage;

public class SsmErrataActionTest extends BaseTestCaseWithUser {

    public void testOnlyRelevantErrataPerSystem() throws Exception {

        // Create System
        Server server1 = ServerFactoryTest.createTestServer(user, true);
        Server server2 = ServerFactoryTest.createTestServer(user, true);

        Errata errata1 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        Errata errata2 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());

        Channel channel1 = ChannelFactoryTest.createTestChannel(user);
        Channel channel2 = ChannelFactoryTest.createTestChannel(user);

        // server 1 has an errata for package1 available
        com.redhat.rhn.domain.rhnpackage.Package package1 = createTestPackage(user, channel1, "noarch");
        createTestInstalledPackage(package1, server1);
        Package package1_updated = createLaterTestPackage(user, errata1, channel1, package1);

        // server 2 has an errata for package2 available
        Package package2 = createTestPackage(user, channel2, "noarch");
        createTestInstalledPackage(package2, server2);
        Package package2_updated = createLaterTestPackage(user, errata2, channel2, package2);

        List<Long> errataIds = new ArrayList<Long>();
        errataIds.add(errata1.getId());
        errataIds.add(errata2.getId());

        List<Long> serverIds = new ArrayList<Long>();
        serverIds.add(server1.getId());
        serverIds.add(server1.getId());

        SsmErrataAction action = new SsmErrataAction();
        SsmErrataEvent event = new SsmErrataEvent(
                user.getId(),
                new Date(),
                null, //actionchain
                errataIds,
                serverIds);

        action.execute(event);

        List<Action> actionsServer1 = ActionFactory.listActionsForServer(user, server1);

        // we want to check that no matter how many actions were scheduled for
        // server1, all the erratas included in those scheduled actions for
        // server1 do not contain the erratas for server2
        Set<Long> server1ScheduledErrata = new HashSet<Long>();
        for (Action a : actionsServer1) {
            ErrataAction errataAction = errataActionFromAction(a);
            for (Errata e : errataAction.getErrata()) {
                server1ScheduledErrata.add(e.getId());
            }
        }
        //assert(!server1ScheduledErrata.contains(errata1.getId()));
        //assertEquals(1, server1ScheduledErrata.size());
        assertFalse("Scheduled Erratas do not include other server's errata",
                server1ScheduledErrata.contains(errata2.getId()));
    }

   private ErrataAction errataActionFromAction(Action action) {
       ErrataAction errataAction = (ErrataAction) HibernateFactory.getSession()
               .createCriteria(ErrataAction.class)
               .add(Restrictions.idEq(action.getId())).uniqueResult();
       return errataAction;
   }
}