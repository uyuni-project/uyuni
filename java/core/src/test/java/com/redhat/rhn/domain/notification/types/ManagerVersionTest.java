/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.domain.notification.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class ManagerVersionTest {

    public static final String DATA_DRIVEN_VERSION_2023_12 = "2023.12";
    public static final String DATA_DRIVEN_VERSION_2024_01 = "2024.01";
    public static final String DATA_DRIVEN_VERSION_2024_07 = "2024.07";
    public static final String DATA_DRIVEN_VERSION_2024_08 = "2024.08";
    public static final String SEMANTIC_VERSION_3_0_0 = "3.0.0";
    public static final String SEMANTIC_VERSION_4_0_0 = "4.0.0";
    public static final String SEMANTIC_VERSION_4_0_1 = "4.0.1";
    public static final String SEMANTIC_VERSION_4_1_0 = "4.1.0";
    public static final String SEMANTIC_VERSION_4_3_12 = "4.3.12";
    public static final String SEMANTIC_VERSION_4_3_13 = "4.3.13";
    public static final String SEMANTIC_VERSION_5_1_0_RC = "5.1.0 RC";
    public static final String SEMANTIC_VERSION_5_1_0_ALPHA = "5.1.0-alpha+001";
    public static final String SEMANTIC_VERSION_5_1_0_BUILD = "5.1.0+20130313144700";

    @Test
    public void testVersionValidating() {
        assertThrows(IllegalArgumentException.class, () -> new ManagerVersion("", true));
        assertThrows(IllegalArgumentException.class, () -> new ManagerVersion("  ", true));
        assertThrows(IllegalArgumentException.class, () -> new ManagerVersion(null, true));
    }

    @Test
    public void testVersionParsing() {
        final String semanticVersion = SEMANTIC_VERSION_4_3_13;
        ManagerVersion v1 = new ManagerVersion(semanticVersion, false);
        assertEquals(4, v1.getMajor());
        assertEquals(3, v1.getMinor());
        assertEquals(13, v1.getPatch());
        assertEquals(semanticVersion, v1.toString());
        assertFalse(v1.isUyuni());

        final String dataDrivenVersion = DATA_DRIVEN_VERSION_2024_07;
        ManagerVersion v2 = new ManagerVersion(dataDrivenVersion, true);
        assertEquals(2024, v2.getMajor());
        assertEquals(7, v2.getMinor());
        assertEquals(-1, v2.getPatch());
        assertEquals(dataDrivenVersion, v2.toString());
        assertTrue(v2.isUyuni());


        ManagerVersion rc = new ManagerVersion(SEMANTIC_VERSION_5_1_0_RC, false);
        assertEquals(5, rc.getMajor());
        assertEquals(1, rc.getMinor());
        assertEquals(0, rc.getPatch());
        assertEquals(SEMANTIC_VERSION_5_1_0_RC, rc.toString() + " RC");
        assertFalse(rc.isUyuni());

        ManagerVersion rc1 = new ManagerVersion(SEMANTIC_VERSION_5_1_0_ALPHA, false);
        assertEquals(5, rc1.getMajor());
        assertEquals(1, rc1.getMinor());
        assertEquals(0, rc1.getPatch());
        assertEquals(SEMANTIC_VERSION_5_1_0_ALPHA, rc1.toString() + "-alpha+001");
        assertFalse(rc1.isUyuni());

        ManagerVersion rc2 = new ManagerVersion(SEMANTIC_VERSION_5_1_0_BUILD, false);
        assertEquals(5, rc2.getMajor());
        assertEquals(1, rc2.getMinor());
        assertEquals(0, rc2.getPatch());
        assertEquals(SEMANTIC_VERSION_5_1_0_BUILD, rc2.toString() + "+20130313144700");
        assertFalse(rc2.isUyuni());
    }

    @ParameterizedTest
    @MethodSource("dataDrivenVersionComparisonData")
    public void testDataDrivenVersionComparison(
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
    @MethodSource("semanticVersionComparisonData")
    public void testSemanticVersionComparison(
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


    static Stream<Arguments> dataDrivenVersionComparisonData() {
        return Stream.of(
                // same year
                Arguments.of(DATA_DRIVEN_VERSION_2024_08, DATA_DRIVEN_VERSION_2024_08, 0, false, true),
                Arguments.of(DATA_DRIVEN_VERSION_2024_08, DATA_DRIVEN_VERSION_2024_07, 1, true, false),
                Arguments.of(DATA_DRIVEN_VERSION_2024_07, DATA_DRIVEN_VERSION_2024_08, -1, false, false),

                // testing different years
                Arguments.of(DATA_DRIVEN_VERSION_2023_12, DATA_DRIVEN_VERSION_2024_01, -1, false, false),
                Arguments.of(DATA_DRIVEN_VERSION_2024_01, DATA_DRIVEN_VERSION_2023_12, 1, true, false)
        );
    }

    static Stream<Arguments> semanticVersionComparisonData() {
        return Stream.of(
                // same major and minor version
                Arguments.of(SEMANTIC_VERSION_4_3_12, SEMANTIC_VERSION_4_3_13, -1, false, false),
                Arguments.of(SEMANTIC_VERSION_4_3_13, SEMANTIC_VERSION_4_3_12, 1, true, false),
                Arguments.of(SEMANTIC_VERSION_4_3_12, SEMANTIC_VERSION_4_3_12, 0, false, true),

                // testing minor and patch version changes
                Arguments.of(SEMANTIC_VERSION_4_0_0, SEMANTIC_VERSION_4_1_0, -1, false, false),
                Arguments.of(SEMANTIC_VERSION_4_0_0, SEMANTIC_VERSION_4_0_1, -1, false, false),
                Arguments.of(SEMANTIC_VERSION_4_1_0, SEMANTIC_VERSION_4_0_0, 1, true, false),
                Arguments.of(SEMANTIC_VERSION_4_0_1, SEMANTIC_VERSION_4_0_0, 1, true, false),

                // testing major version changes
                Arguments.of(SEMANTIC_VERSION_3_0_0, SEMANTIC_VERSION_4_0_0, -1, false, false),
                Arguments.of(SEMANTIC_VERSION_3_0_0, "2.0.0", 1, true, false)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidVersionData")
    public void testInvalidVersionFormat(String versionString, boolean isUyuni) {
        assertThrows(IllegalArgumentException.class, () -> new ManagerVersion(versionString, isUyuni));
    }

    static Stream<Arguments> invalidVersionData() {
        return Stream.of(
                Arguments.of("2024.08.01", true),   // Invalid data driven format
                Arguments.of("2024-08", true),      // Invalid separator

                Arguments.of("1.2", false),         // Incomplete semantic version
                Arguments.of("abc.def.ghi", false), // Non-numeric semantic version
                Arguments.of("1.2.X", false)        // Non-numeric semantic version
        );
    }

    @Test
    public void testEqualityAndEquivalence() {
        ManagerVersion semanticVersion = new ManagerVersion(SEMANTIC_VERSION_4_3_13, false);
        ManagerVersion dataDrivenVersion = new ManagerVersion(DATA_DRIVEN_VERSION_2024_08, true);

        assertNotEquals(semanticVersion, dataDrivenVersion,
                "Versions should not be equals as they belong to different products");
        assertEquals(semanticVersion, new ManagerVersion(SEMANTIC_VERSION_4_3_13, false));
        assertEquals(dataDrivenVersion, new ManagerVersion(DATA_DRIVEN_VERSION_2024_08, true));
    }
}
