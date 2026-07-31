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

import static com.suse.manager.attestation.IbmInputDataValidator.HOST_KEY_DOCUMENT_FIELD;
import static com.suse.manager.attestation.IbmInputDataValidator.SECURE_EXECUTION_HEADER_FIELD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class IbmInputDataValidatorTest {

    private static final LocalizationService L10N = LocalizationService.getInstance();
    private final IbmInputDataValidator validator = new IbmInputDataValidator();

    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----\n";
    private static final String END_CERT = "\n-----END CERTIFICATE-----";

    @Test
    @DisplayName("test validation when input data map is null")
    void testNullInputData() {
        List<String> errorsNull = validator.validate(null);
        assertEquals(1, errorsNull.size());
        assertEquals(L10N.getMessage("coco.attestation.ibm.missing_input_data"), errorsNull.get(0));
    }

    @Test
    @DisplayName("test validation when input data map is empty")
    void testEmptyInputData() {
        List<String> errorsEmpty = validator.validate(Map.of());
        assertEquals(1, errorsEmpty.size());
        assertEquals(L10N.getMessage("coco.attestation.ibm.missing_input_data"), errorsEmpty.get(0));
    }

    @Test
    @DisplayName("test validation of completely valid inputs")
    void testCheckValidInputs() {
        Map<String, Object> inputData = createValidInputMap();
        List<String> errors = validator.validate(inputData);
        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("test validation of completely valid inputs")
    void testCheckGeneratedCertificate() {
        // Test with the generated dummy certificate too
        Map<String, Object> inputData = createValidInputMap();
        inputData.put(HOST_KEY_DOCUMENT_FIELD, BEGIN_CERT + getTestDummyCertBodyFrom2000To2068() + END_CERT);
        List<String> dummyErrors = validator.validate(inputData);
        assertTrue(dummyErrors.isEmpty());
    }

    @Nested
    class HostKeyDocumentTest {

        @Test
        @DisplayName("test validity of invalid host key documents caused by wrong certificate header and footer")
        void testCheckInvalidHostKeyDocumentHeaderFooter() {
            Map<String, Object> inputData = createValidInputMap();

            // missing dashes before BEGIN CERTIFICATE
            inputData.put(HOST_KEY_DOCUMENT_FIELD,
                "---BEGIN CERTIFICATE-----\n" + getTestDummyCertBodyFrom2000To2068() + END_CERT);
            List<String> errors = validator.validate(inputData);
            assertEquals(1, errors.size());
            assertTrue(errors.get(0).contains(L10N.getMessage("coco.attestation.ibm.hostKeyDocument_failed")));

            // typo BEIN
            inputData.put(HOST_KEY_DOCUMENT_FIELD,
                "-----BEIN CERTIFICATE-----\n" + getTestDummyCertBodyFrom2000To2068() + END_CERT);
            errors = validator.validate(inputData);
            assertTrue(errors.get(0).contains(L10N.getMessage("coco.attestation.ibm.hostKeyDocument_failed")));

            // missing dashes after BEGIN CERTIFICATE
            inputData.put(HOST_KEY_DOCUMENT_FIELD,
                "-----BEGIN CERTIFICATE--\n" + getTestDummyCertBodyFrom2000To2068() + END_CERT);
            errors = validator.validate(inputData);
            assertEquals(1, errors.size());
            assertTrue(errors.get(0).contains(L10N.getMessage("coco.attestation.ibm.hostKeyDocument_failed")));

            // missing \n after BEGIN CERTIFICATE
            inputData.put(HOST_KEY_DOCUMENT_FIELD,
                "-----BEGIN CERTIFICATE-----" + getTestDummyCertBodyFrom2000To2068() + END_CERT);
            errors = validator.validate(inputData);
            assertEquals(1, errors.size());
            assertTrue(errors.get(0).contains(L10N.getMessage("coco.attestation.ibm.hostKeyDocument_failed")));

            // missing dashes before END CERTIFICATE
            inputData.put(HOST_KEY_DOCUMENT_FIELD,
                BEGIN_CERT + getTestDummyCertBodyFrom2000To2068() + "----END CERTIFICATE-----");
            errors = validator.validate(inputData);
            assertEquals(1, errors.size());
            assertTrue(errors.get(0).contains(L10N.getMessage("coco.attestation.ibm.hostKeyDocument_failed")));

            // typo EMD
            inputData.put(HOST_KEY_DOCUMENT_FIELD,
                BEGIN_CERT + getTestDummyCertBodyFrom2000To2068() + "-----EMD CERTIFICATE-----");
            errors = validator.validate(inputData);
            assertEquals(1, errors.size());
            assertTrue(errors.get(0).contains(L10N.getMessage("coco.attestation.ibm.hostKeyDocument_failed")));

            // missing dashes after END CERTIFICATE
            inputData.put(HOST_KEY_DOCUMENT_FIELD,
                BEGIN_CERT + getTestDummyCertBodyFrom2000To2068() + "-----END CERTIFICATE---");
            errors = validator.validate(inputData);
            assertEquals(1, errors.size());
            assertTrue(errors.get(0).contains(L10N.getMessage("coco.attestation.ibm.hostKeyDocument_failed")));

            // not matching
            inputData.put(HOST_KEY_DOCUMENT_FIELD,
                "-----BEGIN THIS-----\n" + getTestDummyCertBodyFrom2000To2068() + "\n-----END THAT-----");
            errors = validator.validate(inputData);
            assertEquals(1, errors.size());
            assertTrue(errors.get(0).contains(L10N.getMessage("coco.attestation.ibm.hostKeyDocument_failed")));
        }

        @Test
        @DisplayName("test validity of invalid host key documents caused by invalid content")
        void testCheckInvalidHostKeyDocumentContent() {
            Map<String, Object> inputData = createValidInputMap();

            // gibberish content
            inputData.put(HOST_KEY_DOCUMENT_FIELD,
                BEGIN_CERT + "gibberish7358358309487530475089374509374583745037503" + END_CERT);
            List<String> errors = validator.validate(inputData);
            assertEquals(1, errors.size());
            assertTrue(errors.get(0).startsWith(L10N.getMessage("coco.attestation.ibm.hostKeyDocument_failed")));

            // invalid base64 char injection
            inputData.put(HOST_KEY_DOCUMENT_FIELD, BEGIN_CERT + modifyDummyCertBody(145, '{') + END_CERT);
            errors = validator.validate(inputData);
            assertTrue(errors.get(0).startsWith(L10N.getMessage("coco.attestation.ibm.hostKeyDocument_failed")));

            // casual content injection
            inputData.put(HOST_KEY_DOCUMENT_FIELD, BEGIN_CERT + modifyDummyCertBody(47, 'z') + END_CERT);
            errors = validator.validate(inputData);
            assertTrue(errors.get(0).startsWith(L10N.getMessage("coco.attestation.ibm.hostKeyDocument_failed")));

            // invalid base64 char injection
            inputData.put(HOST_KEY_DOCUMENT_FIELD, BEGIN_CERT + modifyDummyCertBody(145, '{') + END_CERT);
            errors = validator.validate(inputData);
            assertTrue(errors.get(0).startsWith(L10N.getMessage("coco.attestation.ibm.hostKeyDocument_failed")));
        }

        @Test
        @DisplayName("test validity of invalid host key documents caused by time invalidity")
        void testCheckInvalidHostKeyDocumentTimeValidity() {
            Map<String, Object> inputData = createValidInputMap();

            // not yet valid certificate
            inputData.put(HOST_KEY_DOCUMENT_FIELD, BEGIN_CERT + getTestDummyCertBodyFrom2067To2068() + END_CERT);
            List<String> errors = validator.validate(inputData);
            assertEquals(1, errors.size());
            assertEquals(L10N.getMessage("coco.attestation.ibm.hostKeyDocument_not_valid_yet"), errors.get(0));

            // expired certificate
            inputData.put(HOST_KEY_DOCUMENT_FIELD, BEGIN_CERT + getTestDummyCertBodyFrom2000To2001() + END_CERT);
            errors = validator.validate(inputData);
            assertEquals(1, errors.size());
            assertEquals(L10N.getMessage("coco.attestation.ibm.hostKeyDocument_expired"), errors.get(0));
        }
    }

    @Nested
    class SecureExecutionHeaderTest {

        @Test
        @DisplayName("test validity of invalid secure execution header - wrong header")
        void testCheckInvalidSecureExecutionHeaderWrongHeader() {
            Map<String, Object> inputData = createValidInputMap();

            // empty
            inputData.put(SECURE_EXECUTION_HEADER_FIELD, null);
            List<String> errors = validator.validate(inputData);
            assertEquals(1, errors.size());
            assertEquals(L10N.getMessage("coco.attestation.ibm.secureExecutionHeader_missing"), errors.get(0));

            // empty
            inputData.put(SECURE_EXECUTION_HEADER_FIELD, "");
            errors = validator.validate(inputData);
            assertEquals(1, errors.size());
            assertEquals(L10N.getMessage("coco.attestation.ibm.secureExecutionHeader_empty"), errors.get(0));

            // invalid base64 format char
            inputData.put(SECURE_EXECUTION_HEADER_FIELD, "123{5");
            errors = validator.validate(inputData);
            assertEquals(1, errors.size());
            assertEquals(L10N.getMessage("coco.attestation.ibm.secureExecutionHeader_wrong_format"), errors.get(0));

            // string "1234567890123456"
            inputData.put(SECURE_EXECUTION_HEADER_FIELD, "MTIzNDU2Nzg5MDEyMzQ1Ngo=");
            errors = validator.validate(inputData);
            assertEquals(1, errors.size());
            assertEquals(L10N.getMessage("coco.attestation.ibm.secureExecutionHeader_invalid_header"), errors.get(0));
        }

        @Test
        @DisplayName("test validity of invalid secure execution header - length")
        void testCheckInvalidSecureExecutionHeaderLength() {
            Map<String, Object> inputData = createValidInputMap();

            // string "1234567"
            inputData.put(SECURE_EXECUTION_HEADER_FIELD, "MTIzNDU2Nwo=");
            List<String> errors = validator.validate(inputData);
            assertEquals(1, errors.size());
            assertEquals(L10N.getMessage("coco.attestation.ibm.secureExecutionHeader_too_short"), errors.get(0));

            // string "IBMSecEx"
            inputData.put(SECURE_EXECUTION_HEADER_FIELD, "SUJNIFNlYyBFeAo");
            errors = validator.validate(inputData);
            assertEquals(1, errors.size());
            assertEquals(L10N.getMessage("coco.attestation.ibm.secureExecutionHeader_too_short"), errors.get(0));

            // string "IBMSecEx12345678"
            inputData.put(SECURE_EXECUTION_HEADER_FIELD, "SUJNU2VjRXgxMjM0NTY3OAo=");
            errors = validator.validate(inputData);
            assertEquals(1, errors.size());
            assertEquals(L10N.getMessage("coco.attestation.ibm.secureExecutionHeader_size_mismatch"), errors.get(0));

            // content injection forcing expected header size length calculation mismatch
            inputData.put(SECURE_EXECUTION_HEADER_FIELD, modifySeh(15, (byte) 0xFF));
            errors = validator.validate(inputData);
            assertEquals(1, errors.size());
            assertEquals(L10N.getMessage("coco.attestation.ibm.secureExecutionHeader_size_mismatch"), errors.get(0));

            inputData.put(SECURE_EXECUTION_HEADER_FIELD, modifySeh(12, (byte) 0xFF));
            errors = validator.validate(inputData);
            assertEquals(1, errors.size());
            assertEquals(L10N.getMessage("coco.attestation.ibm.secureExecutionHeader_size_mismatch"), errors.get(0));
        }

        @Test
        @DisplayName("test validity of invalid secure execution header - wrong bytes")
        void testCheckInvalidSecureExecutionHeaderWrongBytes() {
            Map<String, Object> inputData = createValidInputMap();

            // modify first byte of magic numbers
            inputData.put(SECURE_EXECUTION_HEADER_FIELD, modifySeh(0, (byte) 0x0A));
            List<String> errors = validator.validate(inputData);
            assertEquals(1, errors.size());
            assertEquals(L10N.getMessage("coco.attestation.ibm.secureExecutionHeader_invalid_header"), errors.get(0));

            inputData.put(SECURE_EXECUTION_HEADER_FIELD, modifySeh(7, (byte) 0x0A));
            errors = validator.validate(inputData);
            assertEquals(1, errors.size());
            assertEquals(L10N.getMessage("coco.attestation.ibm.secureExecutionHeader_invalid_header"), errors.get(0));
        }
    }

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

    private String modifyDummyCertBody(int pos, char content) {
        char[] certBody = getTestDummyCertBodyFrom2000To2068().toCharArray();
        certBody[pos] = content;
        return String.valueOf(certBody);
    }

    private String modifySeh(int pos, byte content) {
        byte[] seh = Base64.getDecoder().decode(getTestSecureExecutionHeader().replace("\n", ""));
        seh[pos] = content;
        return Base64.getEncoder().encodeToString(seh);
    }

    private Map<String, Object> createValidInputMap() {
        Map<String, Object> inputData = new HashMap<>();
        inputData.put(HOST_KEY_DOCUMENT_FIELD, getTestHostKeyDocument());
        inputData.put(SECURE_EXECUTION_HEADER_FIELD, getTestSecureExecutionHeader());
        return inputData;
    }
}
