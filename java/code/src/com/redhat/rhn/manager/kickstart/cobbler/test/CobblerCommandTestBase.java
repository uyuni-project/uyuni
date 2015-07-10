package com.redhat.rhn.manager.kickstart.cobbler.test;

import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.kickstart.test.KickstartableTreeTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerDistroCreateCommand;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.UserTestUtils;

/**
 * Base for Cobbler command tests.
 * Contains pre-filled KickstartData instance to be used by tests.
 */
public class CobblerCommandTestBase extends BaseTestCaseWithUser {

    protected KickstartData ksdata;

    /**
     * {@inheritDoc}
     *
     * @throws Exception if anything goes wrong
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        user = UserTestUtils.createUserInOrgOne();
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        this.ksdata = KickstartDataTest.createKickstartWithDefaultKey(this.user.getOrg());
        this.ksdata.getTree().setBasePath("/tmp/opt/repo/f9-x86_64/");

        // Uncomment this if you want the tests to actually talk to cobbler
        //Config.get().setString(CobblerXMLRPCHelper.class.getName(),
        //        CobblerXMLRPCHelper.class.getName());
        //Config.get().setString(CobblerConnection.class.getName(),
        //        CobblerConnection.class.getName());
        //commitAndCloseSession();

        KickstartableTreeTest.createKickstartTreeItems(this.ksdata.getTree());
        CobblerDistroCreateCommand dcreate = new
            CobblerDistroCreateCommand(ksdata.getTree(), user);
        dcreate.store();
    }

}
