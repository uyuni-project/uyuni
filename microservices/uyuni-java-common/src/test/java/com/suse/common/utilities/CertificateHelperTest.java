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

package com.suse.common.utilities;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.time.Instant;

public class CertificateHelperTest {

    @Test
    @DisplayName("check certificate parse and regenerate")
    public void testCertificateParseRegenerate() throws CertificateException {
        X509Certificate testDigiCertCa = CertificateHelper.parse(testDigiCertCa_2036_04_29);
        assertEquals(testDigiCertCa_2036_04_29.replace("\n", ""),
                CertificateHelper.getPemCertificate(testDigiCertCa).replace("\n", ""));

        X509Certificate testCertificate = CertificateHelper.parse(testCertificate_2024_07_14);
        assertEquals(testCertificate_2024_07_14.replace("\n", ""),
                CertificateHelper.getPemCertificate(testCertificate).replace("\n", ""));
    }

    @Test
    @DisplayName("check certificate validity, with valid certificate")
    public void testCertificateValidityValidCert() throws CertificateException {

        X509Certificate testCert = CertificateHelper.parse(testDigiCertCa_2036_04_29);

        //Not Before: Apr 29 00:00:00 2021 GMT
        assertEquals(Instant.parse("2021-04-29T00:00:00.000Z"), testCert.getNotBefore().toInstant());
        //Not After : Apr 28 23:59:59 2036 GMT
        assertEquals(Instant.parse("2036-04-28T23:59:59.000Z"), testCert.getNotAfter().toInstant());

        assertDoesNotThrow(() -> testCert.checkValidity());
    }

    @Test
    @DisplayName("check certificate validity, with invalid certificate")
    public void testCertificateValidityInvalidCert() throws CertificateException {

        X509Certificate testCert = CertificateHelper.parse(testCertificate_2024_07_14);

        //Not Before: "2022-06-15T19:20:19.000+0200"
        assertEquals(Instant.parse("2022-06-15T17:20:19.000Z"), testCert.getNotBefore().toInstant());
        //Not After : "2024-06-14T19:20:19.000+0200"
        assertEquals(Instant.parse("2024-06-14T17:20:19.000Z"), testCert.getNotAfter().toInstant());

        assertThrowsExactly(CertificateExpiredException.class, testCert::checkValidity);
    }


    private final String testCertificate_2024_07_14 = """
            -----BEGIN CERTIFICATE-----
            MIIFRjCCAy6gAwIBAgIJZHtstznUcMSKMA0GCSqGSIb3DQEBDQUAMIHMMQswCQYD
            VQQGEwJVUzE0MDIGA1UECgwrSW50ZXJuYXRpb25hbCBCdXNpbmVzcyBNYWNoaW5l
            cyBDb3Jwb3JhdGlvbjEnMCUGA1UECwweSUJNIFogSG9zdCBLZXkgU2lnbmluZyBT
            ZXJ2aWNlMRUwEwYDVQQHDAxQb3VnaGtlZXBzaWUxETAPBgNVBAgMCE5ldyBZb3Jr
            MTQwMgYDVQQDDCtJbnRlcm5hdGlvbmFsIEJ1c2luZXNzIE1hY2hpbmVzIENvcnBv
            cmF0aW9uMB4XDTIyMDYxNTE3MjAxOVoXDTI0MDYxNDE3MjAxOVowgaoxCzAJBgNV
            BAYTAlVTMSgwJgYDVQQKDB9JbnRlcm5hdGlvbmFsIEJ1c2luZXNzIE1hY2hpbmVz
            MScwJQYDVQQLDB5JQk0gWiBIb3N0IEtleSBTaWduaW5nIFNlcnZpY2UxDzANBgNV
            BAcMBkFybW9uazERMA8GA1UECAwITmV3IFlvcmsxJDAiBgNVBAMMG2libS16LWhv
            c3Qta2V5LTAwMDAyMDA2ODhFODCBmzAQBgcqhkjOPQIBBgUrgQQAIwOBhgAEAOTg
            Qro26O3m61M9GftqU4ih1oJtt/WwnwIUhhJ0QIncJ47mxQc/Lcpc6SEC+Z9NRcak
            9DSjK5N8bL3SqGmK8qHkAYg7TaE345xsZlE0/HHKEaR5kt/n76j8Rh9OdF0Nuwfr
            tvhCnIbC6wWmv0fjHco1JOK3Hka1uBQKfLqSEtdUlLzpo4HSMIHPMA4GA1UdDwEB
            /wQEAwIDCDCBvAYDVR0fBIG0MIGxMHmgd6B1hnNodHRwczovL3d3dy5pYm0uY29t
            L3NlcnZlcnMvcmVzb3VyY2VsaW5rL2xpYjAzMDYwLm5zZi9wYWdlcy9JQk0tU2Vj
            dXJlLUV4ZWN1dGlvbi1mb3ItTGludXgvJGZpbGUvaWJtLXotaG9zdC1rZXkuY3Js
            MDSgMqAwhi5odHRwczovL2libS5iaXovaWJtLXotaG9zdC1rZXktcmV2b2NhdGlv
            bi1saXN0MA0GCSqGSIb3DQEBDQUAA4ICAQBMPAUPmopgtQpDaJjTtEoAJesvpNMk
            mzUfr7Rjk5PDk/X+oyUtkq5zDEGywMVyWjztRAoX+a1BvCEuoTbSF6f2H6veYwmd
            KauvxBZ/u50Ql+/YEDco56ZeFjeNzKpG1U3wPLSFjetExVrP2PfyqLqTT+nh3SNU
            qOMpWF1rXCJ/1Nvo4h7a7fFcbgpleNhPjXWO+oK5kex2RDbQs/Pq6hYBdRUwi99f
            ZlDUvs7NiYpgGaHM69slSxykgrJVEnQGz7FGuLAIgJ53r3E2Dxb2+JgzGqPTj+/z
            57HmuoiYr2XRJNGO0XQl/dJhDW4bqtH2enl2FYGiL7utFC2azWcNny74Xo0rd/4O
            m2SnZ78LOpjv1mysNogbimGZkBhAYldG1Wq0YGarFQkHPdSpcFTk7suWHeGiiQXa
            KSrHNCEhTmpK0wenXle15DuRo1HVXd1XUlXG8rvvvGjslMUv/fnBb3XAH47Q1JLB
            e7GoUdoqqtWYXZaQ1+AUlyvCq7+y6CVXOYi5AjQLO+QplyEaf8EXnQ08ilYPgL70
            YEBJuISLfYwfLxnuid7RLDYqON4J2lER2SDLRrcMAU5cCact6001wVBmxmC6bMjU
            AiiibBNzMaNHKQAdwwwahrgWXhcvpGhr3xpu0jwYlwpBkqfSEYn1hWTgvQIBllxZ
            SxGOjdKJdChuZw==
            -----END CERTIFICATE-----
            """;

    private final String testDigiCertCa_2036_04_29 = """
            -----BEGIN CERTIFICATE-----
            MIIGsDCCBJigAwIBAgIQCK1AsmDSnEyfXs2pvZOu2TANBgkqhkiG9w0BAQwFADBi
            MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMRkwFwYDVQQLExB3
            d3cuZGlnaWNlcnQuY29tMSEwHwYDVQQDExhEaWdpQ2VydCBUcnVzdGVkIFJvb3Qg
            RzQwHhcNMjEwNDI5MDAwMDAwWhcNMzYwNDI4MjM1OTU5WjBpMQswCQYDVQQGEwJV
            UzEXMBUGA1UEChMORGlnaUNlcnQsIEluYy4xQTA/BgNVBAMTOERpZ2lDZXJ0IFRy
            dXN0ZWQgRzQgQ29kZSBTaWduaW5nIFJTQTQwOTYgU0hBMzg0IDIwMjEgQ0ExMIIC
            IjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA1bQvQtAorXi3XdU5WRuxiEL1
            M4zrPYGXcMW7xIUmMJ+kjmjYXPXrNCQH4UtP03hD9BfXHtr50tVnGlJPDqFX/IiZ
            wZHMgQM+TXAkZLON4gh9NH1MgFcSa0OamfLFOx/y78tHWhOmTLMBICXzENOLsvsI
            8IrgnQnAZaf6mIBJNYc9URnokCF4RS6hnyzhGMIazMXuk0lwQjKP+8bqHPNlaJGi
            TUyCEUhSaN4QvRRXXegYE2XFf7JPhSxIpFaENdb5LpyqABXRN/4aBpTCfMjqGzLm
            ysL0p6MDDnSlrzm2q2AS4+jWufcx4dyt5Big2MEjR0ezoQ9uo6ttmAaDG7dqZy3S
            vUQakhCBj7A7CdfHmzJawv9qYFSLScGT7eG0XOBv6yb5jNWy+TgQ5urOkfW+0/tv
            k2E0XLyTRSiDNipmKF+wc86LJiUGsoPUXPYVGUztYuBeM/Lo6OwKp7ADK5GyNnm+
            960IHnWmZcy740hQ83eRGv7bUKJGyGFYmPV8AhY8gyitOYbs1LcNU9D4R+Z1MI3s
            MJN2FKZbS110YU0/EpF23r9Yy3IQKUHw1cVtJnZoEUETWJrcJisB9IlNWdt4z4FK
            PkBHX8mBUHOFECMhWWCKZFTBzCEa6DgZfGYczXg4RTCZT/9jT0y7qg0IU0F8WD1H
            s/q27IwyCQLMbDwMVhECAwEAAaOCAVkwggFVMBIGA1UdEwEB/wQIMAYBAf8CAQAw
            HQYDVR0OBBYEFGg34Ou2O/hfEYb7/mF7CIhl9E5CMB8GA1UdIwQYMBaAFOzX44LS
            cV1kTN8uZz/nupiuHA9PMA4GA1UdDwEB/wQEAwIBhjATBgNVHSUEDDAKBggrBgEF
            BQcDAzB3BggrBgEFBQcBAQRrMGkwJAYIKwYBBQUHMAGGGGh0dHA6Ly9vY3NwLmRp
            Z2ljZXJ0LmNvbTBBBggrBgEFBQcwAoY1aHR0cDovL2NhY2VydHMuZGlnaWNlcnQu
            Y29tL0RpZ2lDZXJ0VHJ1c3RlZFJvb3RHNC5jcnQwQwYDVR0fBDwwOjA4oDagNIYy
            aHR0cDovL2NybDMuZGlnaWNlcnQuY29tL0RpZ2lDZXJ0VHJ1c3RlZFJvb3RHNC5j
            cmwwHAYDVR0gBBUwEzAHBgVngQwBAzAIBgZngQwBBAEwDQYJKoZIhvcNAQEMBQAD
            ggIBADojRD2NCHbuj7w6mdNW4AIapfhINPMstuZ0ZveUcrEAyq9sMCcTEp6QRJ9L
            /Z6jfCbVN7w6XUhtldU/SfQnuxaBRVD9nL22heB2fjdxyyL3WqqQz/WTauPrINHV
            UHmImoqKwba9oUgYftzYgBoRGRjNYZmBVvbJ43bnxOQbX0P4PpT/djk9ntSZz0rd
            KOtfJqGVWEjVGv7XJz/9kNF2ht0csGBc8w2o7uCJob054ThO2m67Np375SFTWsPK
            6Wrxoj7bQ7gzyE84FJKZ9d3OVG3ZXQIUH0AzfAPilbLCIXVzUstG2MQ0HKKlS43N
            b3Y3LIU/Gs4m6Ri+kAewQ3+ViCCCcPDMyu/9KTVcH4k4Vfc3iosJocsL6TEa/y4Z
            XDlx4b6cpwoG1iZnt5LmTl/eeqxJzy6kdJKt2zyknIYf48FWGysj/4+16oh7cGvm
            oLr9Oj9FpsToFpFSi0HASIRLlk2rREDjjfAVKM7t8RhWByovEMQMCGQ8M4+uKIw8
            y4+ICw2/O/TOHnuO77Xry7fwdxPm5yg/rBKupS8ibEH5glwVZsxsDsrFhsP2JjMM
            B0ug0wcCampAMEhLNKhRILutG4UI4lkNbcoFUCvqShyepf2gpx8GdOfy1lKQ/a+F
            SCH5Vzu0nAPthkX0tGFuv2jiJmCG6sivqf6UHedjGzqGVnhO
            -----END CERTIFICATE-----
            """;

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
        assertEquals(testDigiCertCa_2036_04_29.replace("\n", ""),
                CertificateHelper.getPemCertificate(digiCert).replace("\n", ""));

        X509Certificate ibmCert = CertificateHelper.downloadCertificate(ibmZHostKeySigningCertificate);
        assertNotNull(ibmCert);

        X509CRL ibmCrl = CertificateHelper.downloadCertificateRevocationList(ibmZHostKeyCertificateRevocationLists);
        assertNotNull(ibmCrl);
    }
}
