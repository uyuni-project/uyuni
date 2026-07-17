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
package com.suse.coco.module.pvattest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class PvattestTestHelper {

    private PvattestTestHelper() {
        // utility classes should not have a public or default constructor
    }

    private static String getResource(String resourceName) {
        try {
            Path path = Paths.get(PvattestTestHelper.class.getResource(resourceName).toURI());
            return Files.readString(path);
        }
        catch (Exception e) {
            //ignore exception
        }
        return "";
    }

    public static String hostKeyDocument() {
        return getResource("host_key_document.crt");
    }

    public static String testAttestationRequestContentBase64() {
        return getResource("test_attestation_request_content_base_64.txt");
    }

    public static String testAttestationProtectionKeyContentBase64() {
        return getResource("test_attestation_protection_key_content_base_64.txt");
    }

    public static String secureExecutionHeaderContentBase64() {
        return getResource("secure_execution_header_base_64.txt");
    }

    public static String testAttestationResponseContentBase64() {
        return getResource("test_attestation_response_content_base_64.txt");
    }


    public static String testAttestationResultYaml() {
        return getResource("test_attestation_result.yaml");
    }


    public static String testAttestationResultOutput() {
        return getResource("test_attestation_result_output.txt");
    }


    public static String testCertificate20240714() {
        return getResource("test_certificate_2024_07_14.crt");
    }

    public static String testDigiCertCa20360429() {
        return getResource("test_digi_cert_ca_2036_04_29.crt");
    }

    public static String getOutData() {
        return getOutData(getResource("test_attestation_response_content_base_64.txt"));
    }

    public static String getOutData(String attResponse) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");

        if (null != attResponse) {
            builder.append("\"attestation_response\": ");
            builder.append("\"");
            builder.append(attResponse);
            builder.append("\"");
        }

        builder.append("}");
        return builder.toString();
    }

    public static byte[] getSecureExecutionHeader() {
        return Base64.getDecoder()
                .decode(secureExecutionHeaderContentBase64().replace("\n", ""));
    }
    public static byte[] getAttestationProtectionKey() {
        return Base64.getDecoder()
                .decode(testAttestationProtectionKeyContentBase64().replace("\n", ""));
    }
    public static byte[] getAttestationResponse() {
        return Base64.getDecoder()
                .decode(testAttestationResponseContentBase64().replace("\n", ""));
    }


    public static String getInputData() {
        return getInputData(testAttestationResponseContentBase64(),
                testAttestationProtectionKeyContentBase64());
    }

    public static String getInputData(String attRequest, String attKey) {
        String separator = "";
        StringBuilder builder = new StringBuilder();
        builder.append("{");

        if (null != attRequest) {
            builder.append(separator);
            builder.append("\"attestation_request\": ");
            builder.append("\"");
            builder.append(attRequest);
            builder.append("\"");
            separator = ", ";
        }

        if (null != attKey) {
            builder.append(separator);
            builder.append("\"attestation_protection_key\": ");
            builder.append("\"");
            builder.append(attKey);
            builder.append("\"");
        }

        builder.append("}");
        return builder.toString();
    }

    public static String getConfigData() {
        return getConfigData(hostKeyDocument(),
                secureExecutionHeaderContentBase64());
    }

    public static String getConfigData(String hostKeyDocument, String secureExecutionHeader) {
        String separator = "";
        StringBuilder builder = new StringBuilder();
        builder.append("{");

        if (null != hostKeyDocument) {
            builder.append(separator);
            builder.append("\"host_key_document\": ");
            builder.append("\"");
            builder.append(hostKeyDocument);
            builder.append("\"");
            separator = ", ";
        }

        if (null != secureExecutionHeader) {
            builder.append(separator);
            builder.append("\"secure_execution_header\": ");
            builder.append("\"");
            builder.append(secureExecutionHeader);
            builder.append("\"");
        }

        builder.append("}");
        return builder.toString();
    }

}
