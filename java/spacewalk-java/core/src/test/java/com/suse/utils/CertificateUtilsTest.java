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

package com.suse.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class CertificateUtilsTest {

    @Test
    public void ensureSafePathRejectsNullEmptyFilenames() throws IllegalArgumentException {
        String errorMessage = "File name cannot be null or empty";
        assertThrowsExactly(IllegalArgumentException.class,
                () -> CertificateUtils.getCertificateSafePath(null), errorMessage);
        assertThrowsExactly(IllegalArgumentException.class,
                () -> CertificateUtils.getCertificateSafePath(""), errorMessage);
    }

    @ParameterizedTest
    @ValueSource(strings = {"te$t1", "te%t2", ";test3", "te#t4", "te\\t5", "te\\u0003t6", "te\nt7",
            "../test_1-8.txt", ".../test_1-9.txt", "..../test_1-10.txt", "test/../test_1-11.txt"})
    public void ensureSafePathRejectsInvalidChars(String badFilename) throws IllegalArgumentException {
        String errorMessage = "File name contains invalid characters";
        assertThrowsExactly(IllegalArgumentException.class,
                () -> CertificateUtils.getCertificateSafePath(badFilename), errorMessage);
    }

    @Test
    public void ensureSafePathRejectsTraversalAttempt() throws IllegalArgumentException {
        String errorMessage = "Attempted path traversal attack detected";
        assertThrowsExactly(IllegalArgumentException.class,
                () -> CertificateUtils.getCertificateSafePath(".."), errorMessage);
    }

    @Test
    public void ensureSafePathAcceptsValidCases() throws IllegalArgumentException {
        assertEquals("/etc/pki/trust/anchors/test_1-12.txt",
                CertificateUtils.getCertificateSafePath("test_1-12.txt").toString());
        assertEquals("/etc/pki/trust/anchors/test___1---13..txt",
                CertificateUtils.getCertificateSafePath("test___1---13..txt").toString());
        //ipv4
        assertEquals("/etc/pki/trust/anchors/registration_server_10.1.2.245.pem",
                CertificateUtils.getCertificateSafePath("registration_server_10.1.2.245.pem").toString());
        //ipv6
        assertEquals("/etc/pki/trust/anchors/registration_server_2001:db8:3333:4444:5555:6666:7777:8888.pem",
                CertificateUtils.getCertificateSafePath(
                        "registration_server_2001:db8:3333:4444:5555:6666:7777:8888.pem").toString());
        assertEquals("/etc/pki/trust/anchors/registration_server_::.pem",
                CertificateUtils.getCertificateSafePath("registration_server_::.pem").toString());
        assertEquals("/etc/pki/trust/anchors/registration_server_2001:db8::.pem",
                CertificateUtils.getCertificateSafePath("registration_server_2001:db8::.pem").toString());
        assertEquals("/etc/pki/trust/anchors/registration_server_::1234:5678.pem",
                CertificateUtils.getCertificateSafePath("registration_server_::1234:5678.pem").toString());
    }
}
