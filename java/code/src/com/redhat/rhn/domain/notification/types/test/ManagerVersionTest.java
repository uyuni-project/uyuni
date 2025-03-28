/*
 * Copyright (c) 2024 SUSE LLC
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

package com.redhat.rhn.domain.notification.types.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.notification.types.ManagerVersion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class ManagerVersionTest {

    @Test
    public void testVersionValidating() {
        assertThrows(IllegalArgumentException.class, () -> new ManagerVersion("", true));
        assertThrows(IllegalArgumentException.class, () -> new ManagerVersion("  ", true));
        assertThrows(IllegalArgumentException.class, () -> new ManagerVersion(null, true));
    }

    @Test
    public void testVersionParsing() {
        final String versionUyuni = "4.3.13";
        ManagerVersion v1 = new ManagerVersion(versionUyuni, false);
        assertEquals(4, v1.getMajor());
        assertEquals(3, v1.getMinor());
        assertEquals(13, v1.getPatch());
        assertEquals(versionUyuni, v1.toString());
        assertFalse(v1.isUyuni());

        final String versionSuma = "2024.07";
        ManagerVersion v2 = new ManagerVersion(versionSuma, true);
        assertEquals(2024, v2.getMajor());
        assertEquals(7, v2.getMinor());
        assertEquals(-1, v2.getPatch());
        assertEquals(versionSuma, v2.toString());
        assertTrue(v2.isUyuni());
    }

    @ParameterizedTest
    @MethodSource("uyuniVersionComparisonData")
    public void testUyuniVersionComparison(
            String version1,
            String version2,
            int compareResult,
            boolean isNewer,
            boolean isEquals
    ) {
        ManagerVersion v1 = new ManagerVersion(version1, true);
        ManagerVersion v2 = new ManagerVersion(version2, true);
        assertEquals(compareResult, v1.compareTo(v2));
        assertEquals(isNewer, v1.isNewerThan(v2));
        assertEquals(isEquals, v1.equals(v2));
    }

    @ParameterizedTest
    @MethodSource("sumaVersionComparisonData")
    public void testSumaVersionComparison(
            String version1,
            String version2,
            int compareResult,
            boolean isNewer,
            boolean isEquals
    ) {
        ManagerVersion v1 = new ManagerVersion(version1, false);
        ManagerVersion v2 = new ManagerVersion(version2, false);
        assertEquals(compareResult, v1.compareTo(v2));
        assertEquals(isNewer, v1.isNewerThan(v2));
        assertEquals(isEquals, v1.equals(v2));
    }


    static Stream<Arguments> uyuniVersionComparisonData() {
        return Stream.of(
                // same year
                Arguments.of("2024.08", "2024.08", 0, false, true),
                Arguments.of("2024.08", "2024.07", 1, true, false),
                Arguments.of("2024.07", "2024.08", -1, false, false),

                // testing different years
                Arguments.of("2023.12", "2024.01", -1, false, false),
                Arguments.of("2024.01", "2023.12", 1, true, false)
        );
    }

    static Stream<Arguments> sumaVersionComparisonData() {
        return Stream.of(
                // same major and minor version
                Arguments.of("4.3.12", "4.3.13", -1, false, false),
                Arguments.of("4.3.13", "4.3.12", 1, true, false),
                Arguments.of("4.3.12", "4.3.12", 0, false, true),

                // testing minor and patch version changes
                Arguments.of("4.0.0", "4.1.0", -1, false, false),
                Arguments.of("4.0.0", "4.0.1", -1, false, false),
                Arguments.of("4.1.0", "4.0.0", 1, true, false),
                Arguments.of("4.0.1", "4.0.0", 1, true, false),

                // testing major version changes
                Arguments.of("3.0.0", "4.0.0", -1, false, false),
                Arguments.of("3.0.0", "2.0.0", 1, true, false)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidVersionData")
    public void testInvalidVersionFormat(String versionString, boolean isUyuni) {
        assertThrows(IllegalArgumentException.class, () -> new ManagerVersion(versionString, isUyuni));
    }

    static Stream<Arguments> invalidVersionData() {
        return Stream.of(
                Arguments.of("2024.08.01", true),  // Invalid Uyuni format
                Arguments.of("2024-08", true),     // Invalid separator

                Arguments.of("1.2", false),        // Incomplete suma version
                Arguments.of("4.3.12.4", false),    // Extra version part
                Arguments.of("abc.def.ghi", false) // Non-numeric suma version
        );
    }

    @Test
    public void testEqualityAndEquivalence() {
        ManagerVersion vSuma = new ManagerVersion("4.3.13", false);
        ManagerVersion vUyuni = new ManagerVersion("2024.08", true);

        assertNotEquals(vSuma, vUyuni, "Versions should not be equals as they belong to different products");
        assertEquals(vSuma, new ManagerVersion("4.3.13", false));
        assertEquals(vUyuni, new ManagerVersion("2024.08", true));
    }
}

