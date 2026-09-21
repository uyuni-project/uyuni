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
package com.redhat.rhn.manager.content.ubuntu;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

class UbuntuErrataManagerTest {

    @Test
    void canParseUbuntuErrataWithoutBinaries() throws Exception {
        URL testFile = TestUtils.findTestData("missing-binaries.json");
        String json = Files.readString(Path.of(testFile.toURI()), StandardCharsets.UTF_8);
        UbuntuErrataInfo info = UbuntuErrataManager.GSON.fromJson(json, UbuntuErrataInfo.class);

        Map<String, UbuntuErrataInfo> errataInfos = Map.of("8678-2", info);
        Set<String> packageNames = Set.of("libssl1.0.0", "libssl1.1", "openssl", "openssl1.0");

        Entry entry = assertDoesNotThrow(() -> UbuntuErrataManager.parseUbuntuErrata(errataInfos, packageNames))
            .reduce((a, b) -> fail("More than one element in the parsed entries stream"))
            .orElseGet(() -> fail("No entries in the parsed stream"));

        assertAll(
            () -> assertEquals("8678-2", entry.getId()),
            () -> assertEquals(
                List.of("CVE-2024-2511", "CVE-2026-63072", "CVE-2026-54874", "CVE-2024-5535"),
                entry.getCves()
            ),
            () -> assertEquals("openssl, openssl1.0 vulnerabilities", entry.getSummary()),
            () -> assertEquals("""
                    USN-8678-1 fixed vulnerabilities in OpenSSL. This update provides the
                    corresponding fix for OpenSSL and OpenSSL 1.0 on Ubuntu 14.04 LTS,
                    Ubuntu 16.04 LTS, and Ubuntu 18.04 LTS.

                    In addition, this update also fixes the following issues that were
                    not previously addressed in those releases:

                    It was discovered that OpenSSL incorrectly handled TLS handshake
                    message buffering. A remote attacker could possibly use this issue to
                    cause OpenSSL to consume excessive memory, leading to a denial of
                    service. (LP: #2161371)

                    It was discovered that OpenSSL incorrectly handled session cache
                    management when processing TLSv1.3 sessions. A remote attacker could
                    possibly use this issue to cause OpenSSL to consume excessive memory,
                    leading to a denial of service. This issue only affected OpenSSL 1.1.1
                    on Ubuntu 18.04 LTS. (CVE-2024-2511)

                    It was discovered that OpenSSL incorrectly handled the SSL_select_next_proto
                    function when called with an empty client protocol list. A remote attacker
                    could possibly use this issue to cause OpenSSL to disclose private memory
                    contents to the peer, leading to a loss of confidentiality. This issue
                    only affected Ubuntu 16.04 LTS and Ubuntu 18.04 LTS. (CVE-2024-5535)

                    Original advisory details:

                     It was discovered that OpenSSL incorrectly handled buffering of DTLS
                     records for a future epoch. A remote attacker could possibly use this issue
                     to cause OpenSSL to use excessive resources, leading to a denial of
                     service. (CVE-2026-54874)

                     It was discovered that OpenSSL incorrectly handled CMS key unwrapping. A
                     remote attacker could possibly use this issue to cause a heap buffer
                     overflow, leading to a denial of service or arbitrary code execution.
                     (CVE-2026-63072)
                    """, entry.getDescription()
                )
        );
    }

    @Test
    void canConvertFromJsonUbuntuErrataWithSingleCVEString() throws Exception {
        URL testFile = TestUtils.findTestData("single-cve-as-string.json");
        String json = Files.readString(Path.of(testFile.toURI()), StandardCharsets.UTF_8);
        UbuntuErrataInfo info = UbuntuErrataManager.GSON.fromJson(json, UbuntuErrataInfo.class);

        assertAll(
            () -> assertEquals("8706-1", info.getId()),
            // Ensure the single string is converted into a list
            () -> assertEquals(List.of("CVE-2026-27171"), info.getCves()),
            () -> assertEquals("zlib vulnerability", info.getTitle()),
            () -> assertEquals(
                "In general, a standard system update will make all the necessary changes.",
                info.getAction().orElseGet(() -> fail("Action should not be empty"))
            )
        );
    }
}
