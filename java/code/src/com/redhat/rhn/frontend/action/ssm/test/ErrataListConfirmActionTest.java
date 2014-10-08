package com.redhat.rhn.frontend.action.ssm.test;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.testing.RhnMockStrutsTestCase;

import static com.redhat.rhn.manager.audit.test.CVEAuditManagerTestHelper.*;

// TODO this methods need to be moved to somewhere more generic

public class ErrataListConfirmActionTest extends RhnMockStrutsTestCase {

    public void testExecute() throws Exception {
        setRequestPathInfo("/systems/ssm/ListPatchesConfirm");

        // Create System
        Server server1 = ServerFactoryTest.createTestServer(user, true);
        Server server2 = ServerFactoryTest.createTestServer(user, true);

        Errata errata1 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        Errata errata2 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());

        Channel channel1 = ChannelFactoryTest.createTestChannel(user);
        Channel channel2 = ChannelFactoryTest.createTestChannel(user);

        // server 1 has an errata for package1 available
        Package package1 = createTestPackage(user, channel1, "noarch");
        createTestInstalledPackage(package1, server1);
        Package package1_updated = createLaterTestPackage(user, errata1, channel1, package1);

        // server 2 has an errata for package2 available
        Package package2 = createTestPackage(user, channel2, "noarch");
        createTestInstalledPackage(package2, server2);
        Package package2_updated = createLaterTestPackage(user, errata2, channel2, package2);


        RhnSet errataSet = RhnSetDecl.ERRATA.get(user);
        errataSet.addElement(errata1.getId());
        errataSet.addElement(errata2.getId());
        RhnSetManager.store(errataSet);

        //Note: 2 invocations of getParameter("use_date") will be called by DatePicker
        //addRequestParameter(DatePicker.USE_DATE, "true");
        actionPerform();

    }


}