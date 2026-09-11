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
package com.suse.manager.reactor.hardware.sysinfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Tests for {@link SysinfoParser}
 */
public class SysinfoParserTest {

    @Test
    public void testParseSysinfo() {
        Map<String, String> sysvalues = SysinfoParser.parseSysinfo(
                """
                Type:                 this line gets overridden\r
                Type:                 8561\r
                Sequence Code:        00000000000612A3

                this line has no value
                VM00 Control Program: z/VM    7.4.0  \s
                LPAR Extended Name:  \s
                any:commas:keep:it:together \s
                """);

        assertEquals(Map.of(
                "Type", "8561",
                "Sequence Code", "00000000000612A3",
                "VM00 Control Program", "z/VM    7.4.0",
                "LPAR Extended Name", "",
                "any", "commas:keep:it:together"

        ), sysvalues);
    }

    @ParameterizedTest
    @MethodSource("osNameAndVersionForS390ArchData")
    public void testOsNameAndVersionForS390Arch(
            String readValuesOutput, String expectedOsName, String expectedOsVersion
    ) {
        Map<String, String> sysvalues = SysinfoParser.parseSysinfo(readValuesOutput);

        assertEquals(expectedOsName, SysinfoParser.osNameForS390Arch(sysvalues));
        assertEquals(expectedOsVersion, SysinfoParser.osVersionForS390Arch(sysvalues));
    }

    private static Stream<Arguments> osNameAndVersionForS390ArchData() {
        return Stream.of(
                Arguments.of(StringUtils.EMPTY, "z/VM", "N/A"),
                Arguments.of("Type: 2827", "z/VM", "N/A"),
                Arguments.of("VM00 Control Program: z/VM    6.3.0   ", "z/VM", "6.3.0"),
                Arguments.of("VM00 Control Program: z/VM    7.4.0", "z/VM", "7.4.0"),
                Arguments.of("VM00 Control Program: KVM/Linux", "KVM/Linux", "N/A")
        );
    }

    @ParameterizedTest
    @MethodSource("totalIflsData")
    public void testTotalIfls(String line, long expectedIfls) {
        assertEquals(expectedIfls, SysinfoParser.totalIfls(SysinfoParser.parseSysinfo(line)));
    }

    private static Stream<Arguments> totalIflsData() {
        return Stream.of(
                Arguments.of("", 0),
                Arguments.of("CPUs Total: 45", 45),
                Arguments.of("CPUs Total: 45", 45),
                Arguments.of("CPUs Total: 0", 0),
                Arguments.of("Type: 2827", 0),
                Arguments.of("CPUs Total: many", 0),
                Arguments.of("LPAR CPUs Total: 16", 0)
        );
    }


    @ParameterizedTest
    @MethodSource("serverFamilyForS390ArchData")
    public void testServerFamilyForS390Arch(String type, String expectedFamily) {
        assertEquals(expectedFamily, SysinfoParser.serverFamilyForS390Arch(type));
    }

    private static Stream<Arguments> serverFamilyForS390ArchData() {
        return Stream.of(
                Arguments.of("", ""),
                Arguments.of("9175", "z17"),
                Arguments.of("3931", "z16"),
                Arguments.of("3932", "z16"),
                Arguments.of("8561", "z15"),
                Arguments.of("8562", "z15"),
                Arguments.of("3906", "z14"),
                Arguments.of("3907", "z14"),
                Arguments.of("2964", "z13"),
                Arguments.of("2965", "z13"),
                Arguments.of("2827", "z12"),
                Arguments.of("2828", "z12"),
                Arguments.of("2817", "zEnterprise 114"),
                Arguments.of("2818", "zEnterprise 114"),
                Arguments.of("2097", "z10"),
                Arguments.of("2098", "z10"),
                Arguments.of("2094", "z9"),
                Arguments.of("2096", "z9")
        );
    }

    @Test
    public void testServerFamilyForS390ArchOfUnknownType() {
        assertTrue(SysinfoParser.serverFamilyForS390Arch("1234").isEmpty());
    }

}
