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
package com.suse.manager.webui.controllers.download;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Tests for PackagePathParser.
 */
public class PackagePathParserTest {

    static Stream<Arguments> packageParsingTestCases() {
        return Stream.of(
                Arguments.of(
                        "/rhn/manager/download/channel/getPackage/bash-4.4-1.x86_64.rpm",
                        "bash", null, "4.4", "1", "x86_64", null, null
                ),
                Arguments.of(
                        "/rhn/manager/download/channel/getPackage/" +
                                "java-1_8_0-openjdk-1.8.0.362-150000.3.76.1.x86_64.rpm",
                        "java-1_8_0-openjdk", null, "1.8.0.362", "150000.3.76.1", "x86_64", null, null
                ),
                Arguments.of(
                        "/rhn/manager/download/channel/getPackage/kernel-5.14.21-1.x86_64.drpm",
                        "kernel", null, "5.14.21", "1", "x86_64", null, null
                ),
                Arguments.of(
                        "/channel/getPackage/perl-Net.SSLeay-1.88-1.x86_64.rpm",
                        "perl-Net.SSLeay", null, "1.88", "1", "x86_64", null, null
                ),
                Arguments.of(
                        "/channel/getPackage/python3_base-3.9.7-1.x86_64.rpm",
                        "python3_base", null, "3.9.7", "1", "x86_64", null, null
                ),
                // Debian packages
                Arguments.of(
                        "/rhn/manager/download/debchannel/getPackage/gcc-8-base_8-20180414-1ubuntu2.amd64-deb.deb",
                        "gcc-8-base", null, "8-20180414", "1ubuntu2", "amd64-deb", null, null
                ),
                Arguments.of(
                        "/rhn/manager/download/debchannel/getPackage/python-tornado_4.2.1-1ubuntu3.amd64-deb.deb",
                        "python-tornado", null, "4.2.1", "1ubuntu3", "amd64-deb", null, null
                ),
                Arguments.of(
                        "/rhn/manager/download/ubuntu-18.04-amd64-main/getPackage/ruby_1:2.5.1-X.amd64-deb.deb",
                        "ruby", "1", "2.5.1", "X", "amd64-deb", null, null
                ),
                Arguments.of(
                        "/rhn/manager/download/channel/getPackage/busybox_1.30.1-1.amd64.udeb",
                        "busybox", null, "1.30.1", "1", "amd64", null, null
                ),
                Arguments.of(
                        "/channel/getPackage/libssl_1.1.1.1-1ubuntu2.amd64.deb",
                        "libssl", null, "1.1.1.1", "1ubuntu2", "amd64", null, null
                ),
                Arguments.of(
                        "/rhn/manager/download/channel/getPackage/123/abc123def456/vim-9.0-1.x86_64.rpm",
                        "vim", null, "9.0", "1", "x86_64", 123L, "abc123def456"
                ),
                Arguments.of(
                        "/rhn/manager/download/channel/getPackage/789/checksum123/package-1.0-1.x86_64.rpm",
                        "package", null, "1.0", "1", "x86_64", 789L, "checksum123"
                ),
                Arguments.of(
                        "/rhn/manager/download/channel/getPackage/456/xyz789abc/python-tornado_4.2-1ubuntu3.amd64.deb",
                        "python-tornado", null, "4.2", "1ubuntu3", "amd64", 456L, "xyz789abc"
                ),
                Arguments.of(
                        "/rhn/manager/download/hubsync/channel/getPackage/999/checksum456/package-1.0-1.x86_64.rpm",
                        "package", null, "1.0", "1", "x86_64", 999L, "checksum456"
                )
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("packageParsingTestCases")
    public void testParsePackage(
            String path, String expectedName, String expectedEpoch, String expectedVersion, String expectedRelease,
            String expectedArch, Long expectedOrgId, String expectedChecksum
    ) {
        PackageInfo pkg = PackagePathParser.parse(path);
        assertEquals(expectedName, pkg.getName());
        assertEquals(expectedEpoch, pkg.getEpoch());
        assertEquals(expectedVersion, pkg.getVersion());
        assertEquals(expectedRelease, pkg.getRelease());
        assertEquals(expectedArch, pkg.getArch());
        assertEquals(Optional.ofNullable(expectedOrgId), pkg.getOrgId());
        assertEquals(Optional.ofNullable(expectedChecksum), pkg.getChecksum());
    }

    static Stream<Arguments> invalidFormatTestCases() {
        return Stream.of(
                Arguments.of("/path/to/invalid.rpm", "Invalid RPM filename format"),
                Arguments.of("/path/to/invalid.deb", "Invalid Debian filename format"),
                Arguments.of("/path/to/incomplete-.rpm", "Invalid RPM filename format"),
                Arguments.of("/path/to/incomplete_.deb", "Invalid Debian filename format"),
                Arguments.of("/path/to/package.tar.gz", "Unsupported package type")
        );
    }

    // Invalid formats
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("invalidFormatTestCases")
    public void testParseInvalidFormat(String path, String expectedMessagePart) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            PackagePathParser.parse(path)
        );
        assertTrue(ex.getMessage().contains(expectedMessagePart));
    }
}
