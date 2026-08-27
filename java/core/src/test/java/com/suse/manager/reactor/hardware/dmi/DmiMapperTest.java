/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.reactor.hardware.dmi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.server.Dmi;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactoryTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Tests for {@link DmiMapper}
 */
class DmiMapperTest extends BaseTestCaseWithUser {

    private static final Map<String, Object> NONE = Collections.emptyMap();

    private MinionServer testServer;

    @BeforeEach
    public void setUp() throws Exception {
        this.testServer = MinionServerFactoryTest.createTestMinionServer(user);
    }

    // enum to simplify defining an initial dmi state
    private enum StoredDmi {
        MISSING,    // no dmi
        EMPTY,      // dmi w/o data
        POPULATED   // filled dmi
    }

    private void setupStoredDmi(StoredDmi stored) {
        if (stored == StoredDmi.MISSING) {
            return;
        }

        Dmi dmi = new Dmi();
        if (stored == StoredDmi.POPULATED) {
            dmi.setSystem("stale system");
            dmi.setProduct("stale product");
            dmi.setVendor("stale vendor");
            dmi.setBios("stale bios vendor", "stale bios version", "stale bios release");
            dmi.setBoard("stale board");
            dmi.setAsset("stale asset");
        }
        dmi.setServer(testServer);
        testServer.setDmi(dmi);
    }


    @ParameterizedTest
    @EnumSource(StoredDmi.class)
    void testMapDmiInfoWhenNoSmbiosAreProvided(StoredDmi stored) {
        setupStoredDmi(stored);

        Map<String, Object> none = Collections.emptyMap();
        Optional<String> error = new DmiMapper(testServer).mapDmiInfo(none, none, none, none);

        assertTrue(error.isEmpty());

        Dmi dmi = testServer.getDmi();
        assertNotNull(dmi);
        assertEquals(testServer, dmi.getServer());
        assertNull(dmi.getSystem());
        assertNull(dmi.getProduct());
        assertNull(dmi.getVendor());
        // won't override existing bios data with non-existing
        if (stored == StoredDmi.POPULATED) {
            assertEquals("stale bios vendor", dmi.getBios().getVendor());
            assertEquals("stale bios version", dmi.getBios().getVersion());
            assertEquals("stale bios release", dmi.getBios().getRelease());
        }
        else {
            assertNull(dmi.getBios());
        }
        assertNull(dmi.getBoard());
        assertEquals("(chassis: ) (chassis: ) (board: ) (system: )", dmi.getAsset());
    }

    @ParameterizedTest
    @EnumSource(StoredDmi.class)
    void testMapDmiInfoWhenAllSmbiosDataIsProvided(StoredDmi stored) {
        setupStoredDmi(stored);

        Map<String, Object> bios = Map.of(
                "vendor", "SeaBIOS",
                "version", "1.16.0",
                "release_date", "04/01/2014"
        );
        Map<String, Object> system = Map.of(
                "product_name", "Standard PC",
                "version", "pc-q35-7.1",
                "serial_number", "sys-1"
        );
        Map<String, Object> baseboard = Map.of(
                "manufacturer", "Intel",
                "product_name", "440BX",
                "serial_number", "board-1"
        );
        Map<String, Object> chassis = Map.of(
                "serial_number", "chassis-1",
                "asset_tag", "tag-1"
        );

        Optional<String> error = new DmiMapper(testServer).mapDmiInfo(bios, system, baseboard, chassis);

        assertTrue(error.isEmpty());

        Dmi dmi = testServer.getDmi();
        assertNotNull(dmi);
        assertEquals(testServer, dmi.getServer());
        assertEquals("Standard PC pc-q35-7.1", dmi.getSystem());
        assertEquals("Standard PC", dmi.getProduct());
        assertEquals("SeaBIOS", dmi.getVendor());
        assertEquals("SeaBIOS", dmi.getBios().getVendor());
        assertEquals("1.16.0", dmi.getBios().getVersion());
        assertEquals("04/01/2014", dmi.getBios().getRelease());
        assertEquals("Intel 440BX", dmi.getBoard());
        assertEquals("(chassis: chassis-1) (chassis: tag-1) (board: board-1) (system: sys-1)", dmi.getAsset());
    }

    @Test
    void testMapDmiInfoReportsFailureInsteadOfThrowing() {
        Map<String, Object> exploding = new HashMap<>() {
            @Override
            public Object get(Object key) {
                throw new IllegalStateException("boom");
            }
        };

        Optional<String> error = new DmiMapper(testServer).mapDmiInfo(exploding, NONE, NONE, NONE);

        assertEquals("DMI mapping failed: boom", error.orElseThrow());
        assertNull(testServer.getDmi());
    }

}
