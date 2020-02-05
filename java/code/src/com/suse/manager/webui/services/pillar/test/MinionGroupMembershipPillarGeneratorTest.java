/**
 * Copyright (c) 2020 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.webui.services.pillar.test;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerGroupTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.suse.manager.webui.services.SaltConstants.PILLAR_DATA_FILE_PREFIX;
import static com.suse.manager.webui.services.SaltConstants.PILLAR_DATA_FILE_EXT;

/**
 * Tests for {@link MinionGroupMembershipPillarGenerator}
 */
public class MinionGroupMembershipPillarGeneratorTest extends BaseTestCaseWithUser {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Config.get().setString("server.secret_key",
                DigestUtils.sha256Hex(TestUtils.randomString()));
    }

    public void testGenerateGroupMemebershipsPillarData() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);

        ServerGroup group = ServerGroupTest.createTestServerGroup(user.getOrg(), null);
        ServerFactory.addServerToGroup(minion, group);
        ServerFactory.save(minion);
        this.minionGroupMembershipPillarFileManager.generatePillarFile(minion);

        Path filePath = tmpPillarRoot.resolve(PILLAR_DATA_FILE_PREFIX + "_" +
        minion.getMinionId() + "_" + "group_memberships" + "." +
            PILLAR_DATA_FILE_EXT);

        assertTrue(Files.exists(filePath));

        Map<String, Object> map;
        try (FileInputStream fi = new FileInputStream(filePath.toFile())) {
            map = new Yaml().loadAs(fi, Map.class);
        }

        assertTrue(map.containsKey("group_ids"));
        List<Integer> groups = (List<Integer>) map.get("group_ids");
        assertContains(groups.stream().map(id -> (long) id).collect(Collectors.toList()), group.getId());

        assertTrue(map.containsKey("addon_group_types"));
        List<String> addonGroupTypes = (List<String>) map.get("addon_group_types");

        List<String> minionAddonEntitlements =
                minion.getAddOnEntitlements().stream().map(Entitlement::getLabel).collect(Collectors.toList());

        assertTrue(minionAddonEntitlements.stream().allMatch(g -> addonGroupTypes.contains(g)));
        assertContains(addonGroupTypes, minion.getBaseEntitlement().getLabel());
    }

}
