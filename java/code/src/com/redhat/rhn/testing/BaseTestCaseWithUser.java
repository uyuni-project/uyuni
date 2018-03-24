/**
 * Copyright (c) 2009--2013 Red Hat, Inc.
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
package com.redhat.rhn.testing;

import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.user.User;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.suse.manager.webui.services.SaltConstants.SALT_CONFIG_STATES_DIR;

/**
 * Basic test class class with a User
 */
public abstract class BaseTestCaseWithUser extends RhnBaseTestCase {

    protected User user;
    private boolean committed = false;
    protected Path tmpPillarRoot;
    protected Path tmpSaltRoot;
    /**
     * {@inheritDoc}
     */
    public void setUp() throws Exception {
        super.setUp();
        user = UserTestUtils.findNewUser("testUser", "testOrg" +
                this.getClass().getSimpleName());
        KickstartDataTest.setupTestConfiguration(user);
        tmpPillarRoot = Files.createTempDirectory("pillar");
        tmpSaltRoot = Files.createTempDirectory("salt");
        SaltStateGeneratorService.INSTANCE.setPillarDataPath(tmpPillarRoot
                .toAbsolutePath());
        SaltStateGeneratorService.INSTANCE.setSuseManagerStatesFilesRoot(tmpSaltRoot
                .toAbsolutePath());
        Files.createDirectory(tmpSaltRoot.resolve(SALT_CONFIG_STATES_DIR));
    }

    /**
     * {@inheritDoc}
     */
    public void tearDown() throws Exception {
        super.tearDown();

        // If at some point we created a user and committed the transaction, we need
        // clean up our mess
        if (committed) {
           OrgFactory.deleteOrg(user.getOrg().getId(), user);
           commitAndCloseSession();
        }
        committed = false;
        user = null;
        FileUtils.deleteDirectory(tmpPillarRoot.toFile());
        FileUtils.deleteDirectory(tmpSaltRoot.toFile());
    }

    // If we have to commit in mid-test, set up the next transaction correctly
    protected void commitHappened() {
        committed = true;
    }
}
