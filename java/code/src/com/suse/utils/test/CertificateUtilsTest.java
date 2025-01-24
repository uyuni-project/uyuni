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

package com.suse.utils.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import com.suse.utils.CertificateUtils;

import org.junit.jupiter.api.Test;

public class CertificateUtilsTest {

    @Test
    public void testGetGertificateSafePathNullEmpty() throws IllegalArgumentException {
        String errorMessage = "File name cannot be null or empty";
        assertThrowsExactly(IllegalArgumentException.class,
                () -> CertificateUtils.getCertificateSafePath(null), errorMessage);
        assertThrowsExactly(IllegalArgumentException.class,
                () -> CertificateUtils.getCertificateSafePath(""), errorMessage);
    }

    @Test
    public void testGetGertificateSafePathInvalidChars() throws IllegalArgumentException {

        String errorMessage = "File name contains invalid characters";
        assertThrowsExactly(IllegalArgumentException.class,
                () -> CertificateUtils.getCertificateSafePath("te$t1"), errorMessage);
        assertThrowsExactly(IllegalArgumentException.class,
                () -> CertificateUtils.getCertificateSafePath("te%t2"), errorMessage);
        assertThrowsExactly(IllegalArgumentException.class,
                () -> CertificateUtils.getCertificateSafePath(":test3"), errorMessage);
        assertThrowsExactly(IllegalArgumentException.class,
                () -> CertificateUtils.getCertificateSafePath("te#t4"), errorMessage);
        assertThrowsExactly(IllegalArgumentException.class,
                () -> CertificateUtils.getCertificateSafePath("te\\t5"), errorMessage);
        assertThrowsExactly(IllegalArgumentException.class,
                () -> CertificateUtils.getCertificateSafePath("te\\u0003t6"), errorMessage);
        assertThrowsExactly(IllegalArgumentException.class,
                () -> CertificateUtils.getCertificateSafePath("te\nt7"), errorMessage);
    }

    @Test
    public void testGetGertificateSafePathTraversalAttempt() throws IllegalArgumentException {
        String errorMessage = "Attempted path traversal attack detected";
        assertThrowsExactly(IllegalArgumentException.class,
                () -> CertificateUtils.getCertificateSafePath(".."), errorMessage);

        errorMessage = "File name contains invalid characters";
        assertThrowsExactly(IllegalArgumentException.class,
                () -> CertificateUtils.getCertificateSafePath("../test_1-8.txt"), errorMessage);
        assertThrowsExactly(IllegalArgumentException.class,
                () -> CertificateUtils.getCertificateSafePath(".../test_1-9.txt"), errorMessage);
        assertThrowsExactly(IllegalArgumentException.class,
                () -> CertificateUtils.getCertificateSafePath("..../test_1-10.txt"), errorMessage);
        assertThrowsExactly(IllegalArgumentException.class,
                () -> CertificateUtils.getCertificateSafePath("test/../test_1-11.txt"), errorMessage);
    }

    @Test
    public void testGetGertificateSafePathValidCases() throws IllegalArgumentException {
        assertEquals("/etc/pki/trust/anchors/test_1-12.txt",
                CertificateUtils.getCertificateSafePath("test_1-12.txt").toString());
        assertEquals("/etc/pki/trust/anchors/test___1---13..txt",
                CertificateUtils.getCertificateSafePath("test___1---13..txt").toString());
        assertEquals("/etc/pki/trust/anchors/registration_server_10.1.2.245.pem",
                CertificateUtils.getCertificateSafePath("registration_server_10.1.2.245.pem").toString());
    }
}
