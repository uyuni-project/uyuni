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

import com.redhat.rhn.common.localization.LocalizationService;

import org.apache.commons.collections.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

class IbmInputDataValidator implements InputDataValidator {

    // These fields need to match those defined in web/html/src/components/coco-attestation/Utils.tsx and used in
    // microservices/coco-attestation/attestation-module-pvattest/src/main/resources/mapper/pvattest.xml
    public static final String HOST_KEY_DOCUMENT_FIELD = "host_key_document";
    public static final String SECURE_EXECUTION_HEADER_FIELD = "secure_execution_header";

    private static final Logger LOGGER = LogManager.getLogger(IbmInputDataValidator.class);

    private static final LocalizationService L10N = LocalizationService.getInstance();

    @Override
    public List<String> validate(Map<String, Object> inputData) {
        if (MapUtils.isEmpty(inputData)) {
            return List.of(L10N.getMessage("coco.attestation.ibm.missing_input_data"));
        }

        List<String> errors = new ArrayList<>();

        validateSecureExecutionHeader(inputData.get(SECURE_EXECUTION_HEADER_FIELD), errors);
        validateHostKeyDocument(inputData.get(HOST_KEY_DOCUMENT_FIELD), errors);

        return errors;
    }

    private void validateHostKeyDocument(Object value, List<String> errorList) {
        if (!(value instanceof String hostKeyDocument)) {
            errorList.add(L10N.getMessage("coco.attestation.ibm.hostKeyDocument_wrong_format"));
            return;
        }

        try {
            X509Certificate certificate = parse(hostKeyDocument);
            certificate.checkValidity();
        }
        catch (CertificateNotYetValidException ex) {
            LOGGER.warn("Host Key Document Certificate is not valid yet", ex);
            errorList.add(L10N.getMessage("coco.attestation.ibm.hostKeyDocument_not_valid_yet"));
        }
        catch (CertificateExpiredException ex) {
            LOGGER.warn("Host Key Document Certificate is expired", ex);
            errorList.add(L10N.getMessage("coco.attestation.ibm.hostKeyDocument_expired"));
        }
        catch (CertificateException ex) {
            LOGGER.warn("Host Key Document Certificate is not correct", ex);
            errorList.add(L10N.getMessage("coco.attestation.ibm.hostKeyDocument_failed"));
        }
    }

    public void validateSecureExecutionHeader(Object value, List<String> errorList) {
        if (value == null) {
            errorList.add(L10N.getMessage("coco.attestation.ibm.secureExecutionHeader_missing"));
            return;
        }

        if (!(value instanceof String encodedValue)) {
            errorList.add(L10N.getMessage("coco.attestation.ibm.secureExecutionHeader_wrong_format"));
            return;
        }

        byte[] secureExecutionHeader;

        try {
            secureExecutionHeader = Base64.getDecoder().decode(encodedValue.replace("\n", ""));
        }
        catch (Exception ex) {
            LOGGER.warn("Secure execution header is not in a valid base64 format", ex);
            errorList.add(L10N.getMessage("coco.attestation.ibm.secureExecutionHeader_wrong_format"));
            return;
        }

        if (0 == secureExecutionHeader.length) {
            errorList.add(L10N.getMessage("coco.attestation.ibm.secureExecutionHeader_empty"));
            return;
        }

        int minLength = 16;
        if (secureExecutionHeader.length <= minLength) {
            errorList.add(L10N.getMessage("coco.attestation.ibm.secureExecutionHeader_too_short"));
            return;
        }

        // Magic number 8 bytes: 49 42 4D 53 65 63 45 78 ("IBMSecEx")
        byte[] magicNumber = "IBMSecEx".getBytes(StandardCharsets.US_ASCII);
        if (secureExecutionHeader.length < magicNumber.length ||
                !Arrays.equals(secureExecutionHeader, 0, magicNumber.length, magicNumber, 0, magicNumber.length)) {
            errorList.add(L10N.getMessage("coco.attestation.ibm.secureExecutionHeader_invalid_header"));
            return;
        }

        // The size of the Secure Execution Header is a 4-byte big-endian unsigned integer located at bytes 12-15
        int signedHeaderSize = ByteBuffer.wrap(secureExecutionHeader).order(ByteOrder.BIG_ENDIAN).getInt(12);

        // A 4-byte int with the MSB set to 1 would overflow into a negative value. Promote the value to unsigned long
        long headerSize = Integer.toUnsignedLong(signedHeaderSize);

        // If declared size is larger than actual payload length, mark it as mismatch. Since Java array lengths are
        // signed integers capping at 2GB, any size above that threshold is impossible and treated as an invalid value
        if (headerSize > Integer.MAX_VALUE || secureExecutionHeader.length < headerSize) {
            errorList.add(L10N.getMessage("coco.attestation.ibm.secureExecutionHeader_size_mismatch"));
        }
    }

    private static X509Certificate parse(String pemCertificate) throws CertificateException {
        if (pemCertificate == null || pemCertificate.isEmpty()) {
            throw new CertificateException("empty or null PEM certificate");
        }

        try (InputStream inputStream = new ByteArrayInputStream(pemCertificate.getBytes(StandardCharsets.UTF_8))) {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certificateFactory.generateCertificate(inputStream);
        }
        catch (IOException ex) {
            throw new CertificateParsingException("Unable to load certificate from byte array", ex);
        }
    }

}
