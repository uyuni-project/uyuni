/*
 * Copyright (c) 2015--2021 SUSE LLC
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
package com.redhat.rhn.manager.kickstart.cobbler;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartDataTest;
import com.redhat.rhn.domain.kickstart.KickstartableTreeTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import org.cobbler.CobblerConnection;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base for Cobbler command tests.
 * Contains pre-filled KickstartData instance to be used by tests.
 */
public abstract class CobblerCommandTestBase extends BaseTestCaseWithUser {

    protected KickstartData ksdata;

    private final boolean connectToCobbler;

    /**
     * Default constructor
     */
    protected CobblerCommandTestBase() {
        this(false);
    }

    /**
     * Alternative constructor
     * @param connectToCobblerIn true to actually connect to cobbler
     */
    protected CobblerCommandTestBase(boolean connectToCobblerIn) {
        connectToCobbler = connectToCobblerIn;
    }


    /**
     * {@inheritDoc}
     *
     * @throws Exception if anything goes wrong
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        // Make the default user admin
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        UserFactory.save(user);

        this.ksdata = KickstartDataTest.createKickstartWithDefaultKey(this.user.getOrg());
        this.ksdata.getTree().setBasePath("/tmp/opt/repo/f9-x86_64/");

        if (connectToCobbler) {
            Config.get().setString(CobblerXMLRPCHelper.class.getName(), CobblerXMLRPCHelper.class.getName());
            Config.get().setString(CobblerConnection.class.getName(), CobblerConnection.class.getName());
            commitAndCloseSession();
            commitHappened();
        }

        KickstartableTreeTest.createKickstartTreeItems(this.ksdata.getTree());
        CobblerDistroCreateCommand dcreate = new CobblerDistroCreateCommand(ksdata.getTree(), user);
        dcreate.store();
    }

}
