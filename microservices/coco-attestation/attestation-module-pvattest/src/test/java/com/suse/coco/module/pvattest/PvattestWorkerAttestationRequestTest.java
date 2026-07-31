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

package com.suse.coco.module.pvattest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.suse.coco.model.AttestationResult;
import com.suse.coco.model.AttestationStatus;
import com.suse.coco.module.pvattest.execution.PvattestWrapper;
import com.suse.coco.module.pvattest.model.AttestationReport;
import com.suse.coco.module.pvattest.model.AttestationRequest;
import com.suse.coco.module.pvattest.model.IbmZGeneration;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@ExtendWith(MockitoExtension.class)
public class PvattestWorkerAttestationRequestTest {

    @Mock
    private SqlSession session;

    @Mock
    private PvattestWrapper pvattestWrapper;

    private AttestationRequest attestationRequest;

    private AttestationResult result;

    private AttestationReport report;

    private PvattestWorker worker;

    @BeforeEach
    void setup() throws ExecutionException {

        result = new AttestationResult();
        result.setId(1L);
        result.setStatus(AttestationStatus.PENDING);
        result.setReportId(5L);
        result.setInData(null);

        report = new AttestationReport();
        report.setCpuGeneration(IbmZGeneration.IBM_Z16);
        report.setId(5L);
        report.setHostKeyDocument(PvattestTestHelper.hostKeyDocument());


        attestationRequest = new AttestationRequest(
                PvattestTestHelper.testAttestationRequestContentBase64(),
                PvattestTestHelper.testAttestationProtectionKeyContentBase64()
        );

        // Common mocking
        when(session.selectOne("PvattestModule.retrieveReport", 5L)).thenReturn(report);

        worker = new PvattestWorker(pvattestWrapper);
    }

    @Test
    @DisplayName("Rejects request if an exception is thrown")
    void rejectsWhenExceptionHappens() {

        when(session.selectOne("PvattestModule.retrieveReport", 5L))
                .thenThrow(PersistenceException.class);

        assertFalse(worker.processRequest(session, result));
        assertEquals("""
                        - Unable to create request: org.apache.ibatis.exceptions.PersistenceException
                        """,
                result.getProcessOutput()
        );

        verifyNoInteractions(pvattestWrapper);
    }

    @Test
    @DisplayName("Rejects request if report is not found")
    void rejectsWhenConfigurationIsNotFound() {

        when(session.selectOne("PvattestModule.retrieveReport", 5L))
                .thenReturn(null);

        assertFalse(worker.processRequest(session, result));
        assertEquals("""
                        - Unable to retrieve attestation report for request
                        """,
                result.getProcessOutput()
        );

        verifyNoInteractions(pvattestWrapper);
    }

    @Test
    @DisplayName("Rejects request if cpu generation is unknown")
    void rejectsWhenCpuIsNotFound() {
        report.setCpuGeneration(IbmZGeneration.UNKNOWN);

        assertFalse(worker.processRequest(session, result));
        assertEquals("""
                        - Unable to identify IBM processor generation for attestation request
                        """,
                result.getProcessOutput()
        );

        verifyNoInteractions(pvattestWrapper);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"  "})
    @DisplayName("Rejects request if host key document is empty")
    void rejectsWhenConfigurationIsNotFound(String inData) {
        report.setHostKeyDocument(inData);

        assertFalse(worker.processRequest(session, result));
        assertEquals("""
                        - Unable to create request: host key document not found
                        """,
                result.getProcessOutput()
        );

        verifyNoInteractions(pvattestWrapper);
    }


    @Test
    @DisplayName("Rejects request if configuration host key document is invalid")
    void rejectsWhenHkdIsNotValid() {
        report.setHostKeyDocument("invalid_hkd");

        assertFalse(worker.processRequest(session, result));
        assertEquals("""
                        - Unable to create request: could not parse host key certificate
                        """,
                result.getProcessOutput()
        );

        verifyNoInteractions(pvattestWrapper);
    }

    @Test
    @DisplayName("Rejects request if configuration host key document is not parseable")
    void rejectsWhenHkdIsNotParseable() throws CertificateException, IOException, ExecutionException {
        PvattestWrapper realPvattestWrapper = new PvattestWrapper();
        worker = new PvattestWorker(realPvattestWrapper);

        report.setHostKeyDocument(PvattestTestHelper.hostKeyDocument().replace("S", "X"));

        assertFalse(worker.processRequest(session, result));
        assertTrue(result.getProcessOutput().startsWith("- Unable to create request:"));
    }

    @Test
    @DisplayName("Accept request if configuration host key document and secure execution header are valid")
    void acceptedWhenHdkAndSehAreValid() throws CertificateException, IOException, ExecutionException {
        when(pvattestWrapper.createVerifyDownloadCertificates(any()))
                .thenReturn(attestationRequest);

        assertTrue(worker.processRequest(session, result));

        Map<String, String> dataMap = decodeSimpleJsonString(result.getInData());

        assertTrue(dataMap.containsKey(PvattestWorker.ATTESTATION_REQUEST_BIN_TAG));
        assertEquals(PvattestTestHelper.testAttestationRequestContentBase64(),
                dataMap.get(PvattestWorker.ATTESTATION_REQUEST_BIN_TAG));

        assertTrue(dataMap.containsKey(PvattestWorker.ATTESTATION_PROTECTION_KEY_TAG));
        assertEquals(PvattestTestHelper.testAttestationProtectionKeyContentBase64(),
                dataMap.get(PvattestWorker.ATTESTATION_PROTECTION_KEY_TAG));

        assertEquals("", result.getProcessOutput());
    }

    private static Map<String, String> decodeSimpleJsonString(String simpleJsonString) {
        Map<String, String> result = new HashMap<>();

        String body = trimChars(simpleJsonString, "\n {}\n ");

        String[] splitArray = body.split("[:,]");

        for (int i = 0; i < splitArray.length; i += 2) {
            if (i < splitArray.length - 1) {
                decodeResult(result, splitArray[i], splitArray[i + 1]);
            }
        }

        return result;
    }

    private static void decodeResult(Map<String, String> result, String keyString, String valueString) {
        String key = trimChars(keyString, "\n \"");
        String value = trimChars(valueString, "\n \"");

        result.put(key, value);
    }

    private static String trimChars(String str, String toTrimChars) {
        for (int i = 0; i < toTrimChars.length(); i++) {
            String toTrim = String.valueOf(toTrimChars.charAt(i));
            while (str.startsWith(toTrim)) {
                str = str.substring(1);
            }

            while (str.endsWith(toTrim)) {
                str = str.substring(0, str.length() - toTrim.length());
            }
        }
        return str;
    }

}

