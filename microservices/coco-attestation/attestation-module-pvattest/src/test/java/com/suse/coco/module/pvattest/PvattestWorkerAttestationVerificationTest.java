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
import com.suse.coco.module.pvattest.model.AttestationResponse;
import com.suse.coco.module.pvattest.model.IbmZGeneration;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.ExecutionException;

@ExtendWith(MockitoExtension.class)
class PvattestWorkerAttestationVerificationTest {

    @Mock
    private SqlSession session;

    @Mock
    private PvattestWrapper pvattestWrapper;

    @Mock
    private AttestationResponse verificationResult;

    private AttestationResult result;

    private AttestationReport report;

    private PvattestWorker worker;


    @BeforeEach
    void setup() throws ExecutionException {

        result = new AttestationResult();
        result.setId(1L);
        result.setStatus(AttestationStatus.PENDING);
        result.setReportId(5L);
        result.setInData(PvattestTestHelper.getInputData());

        report = new AttestationReport();
        report.setCpuGeneration(IbmZGeneration.IBM_Z16);
        report.setId(5L);
        report.setSecureExecutionHeader(PvattestTestHelper.getSecureExecutionHeader());
        report.setAttestationResponse(PvattestTestHelper.getAttestationResponse());
        report.setAttestationProtectionKey(PvattestTestHelper.getAttestationProtectionKey());

        verificationResult = new AttestationResponse(PvattestTestHelper.testAttestationResultYaml());

        // Common mocking
        when(session.selectOne("PvattestModule.retrieveReport", 5L)).thenReturn(report);

        worker = new PvattestWorker(pvattestWrapper);
    }

    @Test
    @DisplayName("Rejects verification if an exception is thrown")
    void rejectsWhenExceptionHappens() {

        when(session.selectOne("PvattestModule.retrieveReport", 5L))
                .thenThrow(PersistenceException.class);

        assertFalse(worker.processVerification(session, result));
        assertEquals("""
                        - Unable to process attestation result: org.apache.ibatis.exceptions.PersistenceException
                        """,
                result.getProcessOutput()
        );

        verifyNoInteractions(pvattestWrapper);
    }


    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Rejects verification if secure extension header is empty or not present")
    void rejectsWhenSehIsEmptyOrNotPresent(byte[] seh) {

        report.setSecureExecutionHeader(seh);

        assertFalse(worker.processVerification(session, result));
        assertEquals("""
                        - Unable to verify: secure extension header not found in report config data
                        """,
                result.getProcessOutput()
        );

        verifyNoInteractions(pvattestWrapper);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Rejects verification if attestation response is empty or not present")
    void rejectsWhenAttestationResponseIsEmptyOrNotPresent(byte[] attResult) {

        report.setAttestationResponse(attResult);

        assertFalse(worker.processVerification(session, result));
        assertEquals("""
                        - Unable to verify: attestation response not found in report output data
                        """,
                result.getProcessOutput()
        );

        verifyNoInteractions(pvattestWrapper);
    }


    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Rejects verification if attestation protection key is empty or not present")
    void rejectsWhenAttestationProtectionKeyIsEmptyOrNotPresent(byte[] attProtectionKey) {

        report.setAttestationProtectionKey(attProtectionKey);
        assertFalse(worker.processVerification(session, result));
        assertEquals("""
                        - Unable to verify: attestation protection key not found in report input data
                        """,
                result.getProcessOutput()
        );

        verifyNoInteractions(pvattestWrapper);
    }

    @Test
    @DisplayName("Rejects verification if the verification process throws an exception")
    void rejectsWhenVerificationThrows() throws ExecutionException {

        when(pvattestWrapper.verifyAttestationResponse(any(byte[].class), any(byte[].class), any(byte[].class)))
                .thenThrow(ExecutionException.class);

        assertFalse(worker.processVerification(session, result));
        assertEquals("""
                        - Unable to process attestation result: java.util.concurrent.ExecutionException
                        """,
                result.getProcessOutput()
        );

    }

    @Test
    @DisplayName("Rejects verification if the verification process fails")
    void rejectsWhenVerificationProcessFails() throws ExecutionException {

        AttestationResponse res = new AttestationResponse(null);
        when(pvattestWrapper.verifyAttestationResponse(any(byte[].class), any(byte[].class), any(byte[].class)))
                .thenReturn(res);

        assertFalse(worker.processVerification(session, result));
        assertEquals("""
                        - Unable to verify: verification failed
                        """,
                result.getProcessOutput()
        );
    }

    //suppress checkstyle warnings in order to keep it as it appears in the file
    @SuppressWarnings("checkstyle:lineLength")
    @Test
    @DisplayName("Approves verification if all checks pass")
    void approvesVerificationIfAllChecksPass() throws ExecutionException {

        when(pvattestWrapper.verifyAttestationResponse(any(byte[].class), any(byte[].class), any(byte[].class)))
                .thenReturn(verificationResult);

        assertTrue(worker.processVerification(session, result));
        assertEquals("""
                        - Attestation report correctly verified
                        """,
                result.getProcessOutput()
        );

        assertEquals("""
                        cuid: '0x3eb912539634cdce660ab840209190e2'
                        add: 0xd1f728efd99e1da650e758512621a65f9dd8d77d8d34b1dbbb56904eb373b83cd1f728efd99e1da650e758512621a65f9dd8d77d8d34b1dbbb56904eb373b83c
                        add_fields:
                          image_phkh: 0xd1f728efd99e1da650e758512621a65f9dd8d77d8d34b1dbbb56904eb373b83c
                          attestation_phkh: 0xd1f728efd99e1da650e758512621a65f9dd8d77d8d34b1dbbb56904eb373b83c
                        user_data: 0x72616e646f6d2075736572206461746120666f72207374616e646172645f617474656d70745f355f30335f31360a
                        """,
                result.getDetails()
        );
    }

}
