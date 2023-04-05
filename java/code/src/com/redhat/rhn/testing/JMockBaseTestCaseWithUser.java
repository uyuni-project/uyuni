/*
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

import static com.suse.manager.webui.services.SaltConstants.SALT_CONFIG_STATES_DIR;

import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.services.pillar.MinionPillarManager;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Basic test class class with a User
 */
public abstract class JMockBaseTestCaseWithUser extends RhnJmockBaseTestCase {

    protected User user;
    private boolean committed = false;
    protected Path tmpPillarRoot;
    protected Path tmpSaltRoot;

    /**
     * Called once per test method to setup the test environment.
     *
     * @throws Exception if an error occurs during setup
     */
    @BeforeEach
    public void setUp() throws Exception {
        user = UserTestUtils.findNewUser("testUser", "testOrg" +
                this.getClass().getSimpleName());
        KickstartDataTest.setupTestConfiguration(user);
        tmpPillarRoot = Files.createTempDirectory("pillar");
        tmpSaltRoot = Files.createTempDirectory("salt");
        MinionPillarManager.INSTANCE.setPillarDataPath(tmpPillarRoot.toAbsolutePath());
        SaltStateGeneratorService.INSTANCE.setSkipSetOwner(true);
        SaltStateGeneratorService.INSTANCE.setSuseManagerStatesFilesRoot(tmpSaltRoot
                .toAbsolutePath());
        Files.createDirectory(tmpSaltRoot.resolve(SALT_CONFIG_STATES_DIR));
    }

    @Override
    @AfterEach
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
