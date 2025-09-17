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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for the Pillar class
 */
public class PillarTest extends RhnBaseTestCase {

    @Test
    public void testGlobalPillarPersistence() {
        for (int i = 0; i < 2; i++) {
            Map<String, Object> data = Collections.singletonMap("key", String.format("value%d", i));
            Pillar.createGlobalPillar(String.format("pillar%s", i), data);
        }
        List<Pillar> pillars = Pillar.getGlobalPillars();
        assertEquals(2, pillars.size());
        Pillar pillar0 = pillars.stream()
                .filter(pillar -> pillar.getCategory().equals("pillar0"))
                .findFirst()
                .orElseThrow();
        assertEquals("value0", pillar0.getPillar().get("key"));
        assertTrue(pillar0.isGlobalPillar());
        assertFalse(pillar0.isOrgPillar());
        assertFalse(pillar0.isGroupPillar());
        assertFalse(pillar0.isMinionPillar());
    }
}
