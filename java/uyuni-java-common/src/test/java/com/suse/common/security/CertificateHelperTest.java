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

package com.suse.common.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.time.Instant;

public class CertificateHelperTest {

    private String getTestCertificate20240714() {
        try {
            Path path = Paths.get(getClass().getResource("testCertificate_2024_07_14.crt").toURI());
            return Files.readString(path);
        }
        catch (Exception e) {
            //ignore exception
        }
        return "";
    }

    private String getTestDigiCertCa20360429() {
        try {
            Path path = Paths.get(getClass().getResource("testDigiCertCa_2036_04_29.crt").toURI());
            return Files.readString(path);
        }
        catch (Exception e) {
            //ignore exception
        }
        return "";
    }

    @Test
    @DisplayName("check certificate parse and regenerate")
    public void testCertificateParseRegenerate() throws CertificateException {
        String testDigiCertCa20360429 = getTestDigiCertCa20360429();

        X509Certificate testDigiCertCa = CertificateHelper.parse(testDigiCertCa20360429);
        assertEquals(testDigiCertCa20360429.replace("\n", ""),
                CertificateHelper.getPemCertificate(testDigiCertCa).replace("\n", ""));

        String testCertificate20240714 = getTestCertificate20240714();
        X509Certificate testCertificate = CertificateHelper.parse(testCertificate20240714);
        assertEquals(testCertificate20240714.replace("\n", ""),
                CertificateHelper.getPemCertificate(testCertificate).replace("\n", ""));
    }

    @Test
    @DisplayName("check certificate validity, with valid certificate")
    public void testCertificateValidityValidCert() throws CertificateException {
        X509Certificate testCert = CertificateHelper.parse(getTestDigiCertCa20360429());

        //Not Before: Apr 29 00:00:00 2021 GMT
        assertEquals(Instant.parse("2021-04-29T00:00:00.000Z"), testCert.getNotBefore().toInstant());
        //Not After : Apr 28 23:59:59 2036 GMT
        assertEquals(Instant.parse("2036-04-28T23:59:59.000Z"), testCert.getNotAfter().toInstant());

        assertDoesNotThrow(() -> testCert.checkValidity());
    }

    @Test
    @DisplayName("check certificate validity, with invalid certificate")
    public void testCertificateValidityInvalidCert() throws CertificateException, IOException, URISyntaxException {
        X509Certificate testCert = CertificateHelper.parse(getTestCertificate20240714());

        //Not Before: "2022-06-15T19:20:19.000+0200"
        assertEquals(Instant.parse("2022-06-15T17:20:19.000Z"), testCert.getNotBefore().toInstant());
        //Not After : "2024-06-14T19:20:19.000+0200"
        assertEquals(Instant.parse("2024-06-14T17:20:19.000Z"), testCert.getNotAfter().toInstant());

        assertThrowsExactly(CertificateExpiredException.class, testCert::checkValidity);
    }

    @Test
    @Disabled("disabled: run only in local to test certificates download")
    public void testDownloadingFailure() throws IOException, CertificateEncodingException {
        IOException ex = assertThrows(IOException.class,
                () -> CertificateHelper.downloadCertificate("https://unreachable-url.com", 1));
        assertEquals("Unable to download [https://unreachable-url.com]: java.net.ConnectException", ex.getMessage());
    }

    @Test
    @Disabled("disabled: run only in local to test certificates download")
    public void testDownloadingCertificates() throws IOException, CertificateEncodingException {
        //The CA certificate, here from DigiCert, in DigiCertCA.crt
        final String digicertCaCertificate =
                "https://www.ibm.com/support/resourcelink/api/content/public/DigiCertCA.crt";
        //The IBM Z signing-key certificate in SigningKey.crt
        final String ibmZHostKeySigningCertificate =
                "https://www.ibm.com/support/resourcelink/api/content/public/ibm-z-host-key-signing-gen2.crt";
        //The IBM Z certificate-revocation lists
        final String ibmZHostKeyCertificateRevocationLists =
                "https://www.ibm.com/support/resourcelink/api/content/public/ibm-z-host-key-gen2.crl";

        X509Certificate digiCert = CertificateHelper.downloadCertificate(digicertCaCertificate);
        assertNotNull(digiCert);
        assertEquals(getTestDigiCertCa20360429().replace("\n", ""),
                CertificateHelper.getPemCertificate(digiCert).replace("\n", ""));

        X509Certificate ibmCert = CertificateHelper.downloadCertificate(ibmZHostKeySigningCertificate);
        assertNotNull(ibmCert);

        X509CRL ibmCrl = CertificateHelper.downloadCertificateRevocationList(ibmZHostKeyCertificateRevocationLists);
        assertNotNull(ibmCrl);
    }
}
