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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.server.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
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

    @Test
    public void testgetPillarValue() {
        Map<String, Object> data = Map.of(
                "testPillar", Map.ofEntries(
                        Map.entry("bool", true),
                        Map.entry("string", "test"),
                        Map.entry("numeric", 86400),
                        Map.entry("struct", Map.ofEntries(
                                Map.entry("nestedString", "test2"),
                                Map.entry("nestedStruct", Map.ofEntries(
                                        Map.entry("doubleNested", 20015)
                                ))
                        ))

                ));
        Pillar pillar = Pillar.createGlobalPillar("test", data);

        assertEquals(true, pillar.getPillarValue("testPillar:bool"));
        assertEquals("test", pillar.getPillarValue("testPillar:string"));
        assertEquals(86400, pillar.getPillarValue("testPillar:numeric"));
        assertEquals("test2", pillar.getPillarValue("testPillar:struct:nestedString"));
        assertEquals(20015, pillar.getPillarValue("testPillar:struct:nestedStruct:doubleNested"));
    }

    @Test
    public void testgetPillarValueEmptyComponent() {
        Map<String, Object> data = Map.of(
                "testPillar", Map.ofEntries(
                        Map.entry("struct", Map.ofEntries(
                                Map.entry("nestedStruct", Map.ofEntries(
                                        Map.entry("doubleNested", 20015)
                                ))
                        ))

                ));
        Pillar pillar = Pillar.createGlobalPillar("test", data);

        assertEquals(20015, pillar.getPillarValue("testPillar::nestedStruct:doubleNested"));
        assertEquals(20015, pillar.getPillarValue("testPillar:::doubleNested"));
    }

    @Test
    public void testgetPillarValuePathDoesNotExists() {
        Pillar pillar = Pillar.createGlobalPillar("test", Map.of("basically", "empty"));

        assertEquals("empty", pillar.getPillarValue("basically"));
        assertThrowsExactly(LookupException.class, () -> pillar.getPillarValue("nonexisting"));
        assertThrowsExactly(LookupException.class, () -> pillar.getPillarValue("nonexisting:path"));
    }

    @Test
    public void testsetPillarValue() {
        Map<String, Object> data = new HashMap<>();
        data.put("testPillar", "value");
        Pillar pillar = Pillar.createGlobalPillar("test", data);

        // Value update to same type
        pillar.setPillarValue("testPillar", "newValue");
        assertEquals("newValue", pillar.getPillar().get("testPillar"));
        // Value update to struct
        pillar.setPillarValue("testPillar", new HashMap<String, Object>(Map.of("testValue", "nested")));
        assertEquals("nested", ((Map<String, Object>)pillar.getPillar().get("testPillar")).get("testValue"));

        // Value insert with struct creation
        pillar.setPillarValue("testPillar:newNest:secondNest:thirdNest:Value", "deepNested");
        assertEquals("deepNested", pillar.getPillarValue("testPillar:newNest:secondNest:thirdNest:Value"));
    }
}
