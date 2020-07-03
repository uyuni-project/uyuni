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
import com.redhat.rhn.domain.user.User;

import com.redhat.rhn.manager.system.ServerGroupManager;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.services.pillar.MinionGeneralPillarGenerator;
import com.suse.manager.webui.services.pillar.MinionGroupMembershipPillarGenerator;
import com.suse.manager.webui.services.pillar.MinionPillarFileManager;

import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.suse.manager.webui.services.SaltConstants.SALT_CONFIG_STATES_DIR;

/**
 * Basic test class class with a User
 */
public abstract class JMockBaseTestCaseWithUser extends RhnJmockBaseTestCase {

    protected User user;
    protected Path tmpPillarRoot;
    protected Path tmpSaltRoot;
    protected MinionPillarFileManager minionGroupMembershipPillarFileManager =
            new MinionPillarFileManager(new MinionGroupMembershipPillarGenerator());
    protected MinionPillarFileManager minionGeneralPillarFileManager =
            new MinionPillarFileManager(new MinionGeneralPillarGenerator());

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        user = UserTestUtils.findNewUser("testUser", "testOrg" +
                this.getClass().getSimpleName());
        KickstartDataTest.setupTestConfiguration(user);
        tmpPillarRoot = Files.createTempDirectory("pillar");
        tmpSaltRoot = Files.createTempDirectory("salt");
        minionGroupMembershipPillarFileManager.setPillarDataPath(tmpPillarRoot.toAbsolutePath());
        minionGeneralPillarFileManager.setPillarDataPath(tmpPillarRoot.toAbsolutePath());
        SaltStateGeneratorService.INSTANCE.setSuseManagerStatesFilesRoot(tmpSaltRoot
                .toAbsolutePath());
        Files.createDirectory(tmpSaltRoot.resolve(SALT_CONFIG_STATES_DIR));
        ServerGroupManager.getInstance()
                .setMinionGroupMembershipPillarFileManager(minionGroupMembershipPillarFileManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        user = null;
        FileUtils.deleteDirectory(tmpPillarRoot.toFile());
        FileUtils.deleteDirectory(tmpSaltRoot.toFile());
    }
}
