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

package com.suse.manager.attestation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class IbmAttestationInputConfigCheckerTest {
    private static String getResource(String resourceName) {
        try {
            Path path = Paths.get(TestUtils.findTestData(resourceName).toURI());
            return Files.readString(path);
        }
        catch (Exception e) {
            //ignored
        }
        return "";
    }

    private String getTestDummyCertBodyFrom2000To2068() {
        return getResource("testDummyCertBodyFrom2000To2068.txt");
    }

    private String getTestDummyCertBodyFrom2067To2068() {
        return getResource("testDummyCertBodyFrom2067To2068.txt");
    }

    private String getTestDummyCertBodyFrom2000To2001() {
        return getResource("testDummyCertBodyFrom2000To2001.txt");
    }

    private String getTestHostKeyDocument() {
        return getResource("hostKeyDocument.crt");
    }

    private String getTestSecureExecutionHeader() {
        return getResource("secureExecutionHeaderBase64.txt");
    }

    public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----\n";
    public static final String END_CERT = "\n-----END CERTIFICATE-----";

    private final IbmAttestationInputConfigChecker testValidityChecker = new IbmAttestationInputConfigChecker();

    @Test
    @DisplayName("test validity of valid host key documents")
    public void testCheckValidHostKeyDocument() {
        assertTrue(testValidityChecker.isValidHostKeyDocument(getTestHostKeyDocument()));
        assertEquals("", testValidityChecker.getValidityError());
        assertEquals("", testValidityChecker.getLogValidityError());

        assertTrue(testValidityChecker.isValidHostKeyDocument(
                BEGIN_CERT + getTestDummyCertBodyFrom2000To2068() + END_CERT));
        assertEquals("", testValidityChecker.getValidityError());
        assertEquals("", testValidityChecker.getLogValidityError());
    }

    @Test
    @DisplayName("test validity of invalid host key documents caused by wrong certificate header and footer")
    public void testCheckInvalidHostKeyDocumentHeaderFooter() {

        //missing dashes before BEGIN CERTIFICATE
        assertFalse(testValidityChecker.isValidHostKeyDocument(
                "---BEGIN CERTIFICATE-----\n" +
                        getTestDummyCertBodyFrom2000To2068() + END_CERT));
        assertEquals("Incomplete data", testValidityChecker.getValidityError());
        assertTrue(testValidityChecker.getLogValidityError().contains(testValidityChecker.getValidityError()));

        //typo BEIN
        assertFalse(testValidityChecker.isValidHostKeyDocument(
                "-----BEIN CERTIFICATE-----\n" +
                        getTestDummyCertBodyFrom2000To2068() + END_CERT));
        assertEquals("Illegal header: -----BEIN CERTIFICATE-----", testValidityChecker.getValidityError());
        assertTrue(testValidityChecker.getLogValidityError().contains(testValidityChecker.getValidityError()));

        //missing dashes after BEGIN CERTIFICATE
        assertFalse(testValidityChecker.isValidHostKeyDocument(
                "-----BEGIN CERTIFICATE--\n" +
                        getTestDummyCertBodyFrom2000To2068() + END_CERT));
        assertEquals("Illegal header: -----BEGIN CERTIFICATE--", testValidityChecker.getValidityError());
        assertTrue(testValidityChecker.getLogValidityError().contains(testValidityChecker.getValidityError()));

        //missing \n after BEGIN CERTIFICATE
        assertFalse(testValidityChecker.isValidHostKeyDocument(
                "-----BEGIN CERTIFICATE-----" +
                        getTestDummyCertBodyFrom2000To2068() + END_CERT));
        assertTrue(testValidityChecker.getValidityError().startsWith("Illegal header: -----BEGIN CERTIFICATE-----"));
        assertTrue(testValidityChecker.getLogValidityError().contains(testValidityChecker.getValidityError()));

        //missing dashes before END CERTIFICATE
        assertFalse(testValidityChecker.isValidHostKeyDocument(
                BEGIN_CERT + getTestDummyCertBodyFrom2000To2068() +
                        "----END CERTIFICATE-----"));
        assertEquals("Illegal footer: ----END CERTIFICATE-----", testValidityChecker.getValidityError());
        assertTrue(testValidityChecker.getLogValidityError().contains(testValidityChecker.getValidityError()));

        //typo EMD
        assertFalse(testValidityChecker.isValidHostKeyDocument(
                BEGIN_CERT + getTestDummyCertBodyFrom2000To2068() +
                        "-----EMD CERTIFICATE-----"));
        assertEquals("Illegal footer: -----EMD CERTIFICATE-----", testValidityChecker.getValidityError());
        assertTrue(testValidityChecker.getLogValidityError().contains(testValidityChecker.getValidityError()));


        //missing dashes after END CERTIFICATE
        assertFalse(testValidityChecker.isValidHostKeyDocument(
                BEGIN_CERT + getTestDummyCertBodyFrom2000To2068() +
                        "-----END CERTIFICATE---"));
        assertEquals("Illegal footer: -----END CERTIFICATE---", testValidityChecker.getValidityError());
        assertTrue(testValidityChecker.getLogValidityError().contains(testValidityChecker.getValidityError()));

        //not matching
        assertFalse(testValidityChecker.isValidHostKeyDocument(
                "-----BEGIN THIS-----\n" +
                        getTestDummyCertBodyFrom2000To2068() +
                        "\n-----END THAT-----"));
        assertEquals("Header and footer do not match: -----BEGIN THIS----- -----END THAT-----",
                testValidityChecker.getValidityError());
        assertTrue(testValidityChecker.getLogValidityError().contains(testValidityChecker.getValidityError()));
    }

    private String modifyDummyCertBody(int pos, char content) {
        char[] certBody = getTestDummyCertBodyFrom2000To2068().toCharArray();
        certBody[pos] = content;
        return String.valueOf(certBody);
    }

    @Test
    @DisplayName("test validity of invalid host key documents caused by invalid content")
    public void testCheckInvalidHostKeyDocumentContent() {

        //gibberish content
        assertFalse(testValidityChecker.isValidHostKeyDocument(BEGIN_CERT +
                "gibberish7358358309487530475089374509374583745037503" + END_CERT));
        assertEquals("not enough content", testValidityChecker.getValidityError());
        assertTrue(testValidityChecker.getLogValidityError().contains(testValidityChecker.getValidityError()));

        //casual content injection
        assertFalse(testValidityChecker.isValidHostKeyDocument(BEGIN_CERT +
                modifyDummyCertBody(14, 'z') + END_CERT));
        assertEquals("Invalid lenByte", testValidityChecker.getValidityError());
        assertTrue(testValidityChecker.getLogValidityError().contains(testValidityChecker.getValidityError()));

        //casual content injection
        assertFalse(testValidityChecker.isValidHostKeyDocument(BEGIN_CERT +
                modifyDummyCertBody(47, 'z') + END_CERT));
        assertEquals("Signature algorithm mismatch", testValidityChecker.getValidityError());
        assertTrue(testValidityChecker.getLogValidityError().contains(testValidityChecker.getValidityError()));

        //invalid base64 char injection
        assertFalse(testValidityChecker.isValidHostKeyDocument(BEGIN_CERT +
                modifyDummyCertBody(145, '{') + END_CERT));
        assertEquals("java.lang.IllegalArgumentException: Illegal base64 character 7b",
                testValidityChecker.getValidityError());
        assertTrue(testValidityChecker.getLogValidityError().contains(testValidityChecker.getValidityError()));
    }

    @Test
    @DisplayName("test validity of invalid host key documents caused by time invalidity")
    public void testCheckInvalidHostKeyDocumentTimeValidity() {

        //not yet valid certificate
        assertFalse(testValidityChecker.isValidHostKeyDocument(BEGIN_CERT +
                getTestDummyCertBodyFrom2067To2068() +
                END_CERT));
        assertTrue(testValidityChecker.getValidityError()
                .startsWith("Certificate not yet valid: NotBefore: Sat Apr 23"));
        assertTrue(testValidityChecker.getValidityError().endsWith("2067"));
        assertTrue(testValidityChecker.getLogValidityError().contains(testValidityChecker.getValidityError()));

        //expired certificate
        assertFalse(testValidityChecker.isValidHostKeyDocument(BEGIN_CERT +
                getTestDummyCertBodyFrom2000To2001() +
                END_CERT));
        assertTrue(testValidityChecker.getValidityError()
                .startsWith("Certificate expired: NotAfter: Mon Apr 23"));
        assertTrue(testValidityChecker.getValidityError().endsWith("2001"));
        assertTrue(testValidityChecker.getLogValidityError().contains(testValidityChecker.getValidityError()));
    }


    @Test
    @DisplayName("test validity of valid secure execution header")
    public void testCheckValidSecureExecutionHeader() {
        assertTrue(testValidityChecker.isValidSecureExecutionHeader(getTestSecureExecutionHeader()));
        assertEquals("", testValidityChecker.getValidityError());
        assertEquals("", testValidityChecker.getLogValidityError());

        //content injection in header bytes to modify expected length to 784 bytes
        assertTrue(testValidityChecker.isValidSecureExecutionHeader(modifySeh(14, (byte) 0x03)));
        assertEquals("", testValidityChecker.getValidityError());
        assertEquals("", testValidityChecker.getLogValidityError());
    }

    private String modifySeh(int pos, byte content) {
        byte[] seh = Base64.getDecoder().decode(getTestSecureExecutionHeader().replace("\n", ""));
        seh[pos] = content;
        return Base64.getEncoder().encodeToString(seh);
    }

    @Test
    @DisplayName("test validity of invalid secure execution header - wrong header")
    public void testCheckInvalidSecureExecutionHeaderWrongHeader() {
        //empty
        assertFalse(testValidityChecker.isValidSecureExecutionHeader(""));
        assertEquals("empty", testValidityChecker.getValidityError());
        assertTrue(testValidityChecker.getLogValidityError().contains(testValidityChecker.getValidityError()));
        //invalid base64 char
        assertFalse(testValidityChecker.isValidSecureExecutionHeader("123{5"));
        assertEquals("Illegal base64 character 7b", testValidityChecker.getValidityError());
        assertTrue(testValidityChecker.getLogValidityError().contains(testValidityChecker.getValidityError()));

        // string "1234567890123456"
        assertFalse(testValidityChecker.isValidSecureExecutionHeader(
                "MTIzNDU2Nzg5MDEyMzQ1Ngo="));
        assertEquals("invalid header", testValidityChecker.getValidityError());
        assertTrue(testValidityChecker.getLogValidityError().contains(testValidityChecker.getValidityError()));
    }

    @Test
    @DisplayName("test validity of invalid secure execution header - length")
    public void testCheckInvalidSecureExecutionHeaderLength() {
        // string "1234567"
        assertFalse(testValidityChecker.isValidSecureExecutionHeader("MTIzNDU2Nwo="));
        assertEquals("too short", testValidityChecker.getValidityError());
        assertTrue(testValidityChecker.getLogValidityError().contains(testValidityChecker.getValidityError()));

        // string "IBMSecEx"
        assertFalse(testValidityChecker.isValidSecureExecutionHeader("SUJNIFNlYyBFeAo"));
        assertEquals("too short", testValidityChecker.getValidityError());
        assertTrue(testValidityChecker.getLogValidityError().contains(testValidityChecker.getValidityError()));

        // string "IBMSecEx12345678"
        assertFalse(testValidityChecker.isValidSecureExecutionHeader(
                "SUJNU2VjRXgxMjM0NTY3OAo="));
        assertEquals("too short", testValidityChecker.getValidityError());
        assertTrue(testValidityChecker.getLogValidityError().contains(testValidityChecker.getValidityError()));

        //content injection in header bytes to modify length
        assertFalse(testValidityChecker.isValidSecureExecutionHeader(modifySeh(15, (byte) 0xFF)));
        assertEquals("too short", testValidityChecker.getValidityError());
        assertTrue(testValidityChecker.getLogValidityError().contains(testValidityChecker.getValidityError()));

        assertFalse(testValidityChecker.isValidSecureExecutionHeader(modifySeh(12, (byte) 0xFF)));
        assertEquals("too short", testValidityChecker.getValidityError());
        assertTrue(testValidityChecker.getLogValidityError().contains(testValidityChecker.getValidityError()));
    }

    @Test
    @DisplayName("test validity of invalid secure execution header - wrong bytes")
    public void testCheckInvalidSecureExecutionHeaderWrongBytes() {
        //casual content injection in header bytes
        assertFalse(testValidityChecker.isValidSecureExecutionHeader(modifySeh(0, (byte) 0x0A)));
        assertEquals("invalid header", testValidityChecker.getValidityError());
        assertTrue(testValidityChecker.getLogValidityError().contains(testValidityChecker.getValidityError()));

        assertFalse(testValidityChecker.isValidSecureExecutionHeader(modifySeh(7, (byte) 0x0A)));
        assertEquals("invalid header", testValidityChecker.getValidityError());
        assertTrue(testValidityChecker.getLogValidityError().contains(testValidityChecker.getValidityError()));
    }
}
