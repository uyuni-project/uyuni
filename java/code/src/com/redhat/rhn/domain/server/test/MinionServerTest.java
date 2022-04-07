/*
 * Copyright (c) 2021 SUSE LLC
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
package com.redhat.rhn.domain.server.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tests for MinionServer
 */
public class MinionServerTest extends BaseTestCaseWithUser {

    /**
     * Test the pillars persistence
     *
     * @throws Exception if anything bad happens
     */
    @Test
    public void testPillars() throws Exception {
        MinionServer minionServer = MinionServerFactoryTest.createTestMinionServer(user);
        Set<Pillar> pillars = new HashSet<>();
        Map<String, Object> pillar1 = new HashMap<>();
        pillar1.put("data1", "foo");
        pillar1.put("data2", 123);
        pillars.add(new Pillar("category1", pillar1, minionServer));
        Map<String, Object> pillar2 = new HashMap<>();
        pillar1.put("bar1", "baz");
        pillar1.put("bar2", 456);
        pillars.add(new Pillar("category2", pillar2, minionServer));

        minionServer.setPillars(pillars);
        TestUtils.saveAndFlush(minionServer);
        minionServer = reload(minionServer);

        Pillar actual = minionServer.getPillarByCategory("category1").get();
        assertNotNull(actual);
        assertEquals(123, actual.getPillar().get("data2"));
        assertTrue(actual.isMinionPillar());
        assertFalse(actual.isGlobalPillar());
        assertFalse(actual.isGroupPillar());
        assertFalse(actual.isOrgPillar());
    }
}
