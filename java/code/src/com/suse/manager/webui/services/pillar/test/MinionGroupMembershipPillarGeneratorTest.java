/*
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.webui.services.pillar.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerGroupTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.webui.services.pillar.MinionGroupMembershipPillarGenerator;
import com.suse.manager.webui.services.pillar.MinionPillarGenerator;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tests for {@link MinionGroupMembershipPillarGenerator}
 */
public class MinionGroupMembershipPillarGeneratorTest extends BaseTestCaseWithUser {

    protected MinionPillarGenerator minionGroupMembershipPillarGenerator =
            new MinionGroupMembershipPillarGenerator();

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        Config.get().setString("server.secret_key",
                DigestUtils.sha256Hex(TestUtils.randomString()));
    }

    @Test
    public void testGenerateGroupMembershipsPillarData() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);

        ServerGroup group = ServerGroupTest.createTestServerGroup(user.getOrg(), null);
        ServerFactory.addServerToGroup(minion, group);
        ServerFactory.save(minion);
        this.minionGroupMembershipPillarGenerator.generatePillarData(minion);

        Pillar pillar = minion.getPillarByCategory(MinionGroupMembershipPillarGenerator.CATEGORY).orElseThrow();
        Map<String, Object> map = pillar.getPillar();

        assertTrue(map.containsKey("group_ids"));
        List<Long> groups = Arrays.asList((Long[]) map.get("group_ids"));
        assertContains(groups, group.getId());

        assertTrue(map.containsKey("addon_group_types"));
        List<String> addonGroupTypes = Arrays.asList((String[]) map.get("addon_group_types"));

        List<String> minionAddonEntitlements =
                minion.getAddOnEntitlements().stream().map(Entitlement::getLabel).collect(Collectors.toList());

        assertTrue(addonGroupTypes.containsAll(minionAddonEntitlements));
        assertContains(addonGroupTypes, minion.getBaseEntitlement().getLabel());
    }

}
