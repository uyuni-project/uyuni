/*
 * Copyright (c) 2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.coco.attestation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.suse.coco.model.AttestationResult;
import com.suse.coco.model.AttestationStatus;
import com.suse.coco.module.AttestationWorker;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class AttestationResultServiceTest {

    private static final Logger LOGGER = LogManager.getLogger(AttestationResultServiceTest.class);

    @Mock
    private SqlSession session;

    @Mock
    private AttestationWorker worker;

    private AttestationResultService service;

    @BeforeAll
    public static void beforeAll() {
        LOGGER.debug("Running test from AttestationResultManagerTest");
    }

    @BeforeEach
    public void setup(@Mock SqlSessionFactory sessionFactory) {
        when(sessionFactory.openSession()).thenReturn(session);

        service = new AttestationResultService(sessionFactory);
    }

    @Test
    @DisplayName("The attestation results are listed and filtered by the result types")
    void canListPendingResultsByType() {
        when(session.selectList(
            "AttestationResult.listPendingForResultType",
            Map.of("supportedTypes", List.of(1, 2, 3), "batchSize", 10)
        )).thenReturn(List.of(5L, 7L, 13L));

        List<Long> resultIds = service.getPendingResultByType(List.of(1, 2, 3), 10);
        assertEquals(List.of(5L, 7L, 13L), resultIds);

        verify(session).selectList(
            "AttestationResult.listPendingForResultType",
            Map.of("supportedTypes", List.of(1, 2, 3), "batchSize", 10)
        );
        verify(session).close();

        verifyNoMoreInteractions(session);
    }

    @Test
    @DisplayName("An attestation result is locked, successfully processed by the worker and the result is updated")
    void canProcessAttestationResultAndMarkAsSucceeded() {
        AttestationResult attestationResult = new AttestationResult();
        attestationResult.setId(5L);
        attestationResult.setStatus(AttestationStatus.PENDING);
        attestationResult.setAttested(null);

        when(session.selectOne("AttestationResult.selectForUpdate", 5L)).thenReturn(attestationResult);
        when(worker.process(session, attestationResult)).thenReturn(true);

        OffsetDateTime callStart = OffsetDateTime.now();
        service.processAttestationResult(5L, worker);
        OffsetDateTime callEnd = OffsetDateTime.now();

        verify(session).selectOne("AttestationResult.selectForUpdate", 5L);
        verify(worker).process(session, attestationResult);
        verify(session).update("AttestationResult.update", attestationResult);
        verify(session).commit();
        verify(session).close();

        verifyNoMoreInteractions(session);
        verifyNoMoreInteractions(worker);

        // Confirm the status has been updated
        assertEquals(AttestationStatus.SUCCEEDED, attestationResult.getStatus());
        // Verify the attestation date is correctly marked
        assertNotNull(attestationResult.getAttested());
        assertTrue(attestationResult.getAttested().isAfter(callStart));
        assertTrue(attestationResult.getAttested().isBefore(callEnd));
    }

    @Test
    @DisplayName("An attestation result is locked, worker fails to process and the result is updated")
    void canProcessAttestationResultAndMarkAsFailed() {
        AttestationResult attestationResult = new AttestationResult();
        attestationResult.setId(5L);
        attestationResult.setStatus(AttestationStatus.PENDING);
        attestationResult.setAttested(null);

        when(session.selectOne("AttestationResult.selectForUpdate", 5L)).thenReturn(attestationResult);
        when(worker.process(session, attestationResult)).thenReturn(false);

        service.processAttestationResult(5L, worker);

        verify(session).selectOne("AttestationResult.selectForUpdate", 5L);
        verify(worker).process(session, attestationResult);
        verify(session).update("AttestationResult.update", attestationResult);
        verify(session).commit();
        verify(session).close();

        verifyNoMoreInteractions(session);
        verifyNoMoreInteractions(worker);

        // Confirm the status has been updated
        assertEquals(AttestationStatus.FAILED, attestationResult.getStatus());
        // Verify the attestation date is correctly marked
        assertNull(attestationResult.getAttested());
    }

    @Test
    @DisplayName("Nothing is processed if the attestation result has already been updated")
    void doesNotProcessIfAttestationResultIsAlreadyProcessed() {
        // Select for update returns null when another process already updated the same row
        when(session.selectOne("AttestationResult.selectForUpdate", 5L)).thenReturn(null);

        service.processAttestationResult(5L, worker);

        verify(session).selectOne("AttestationResult.selectForUpdate", 5L);
        verify(session).rollback();
        verify(session).close();

        verifyNoMoreInteractions(session);
        verifyNoInteractions(worker);
    }

    @Test
    @DisplayName("Nothing is processed if the attestation result cannot be locked")
    void doesNotProcessIfAttestationResultCannotBeLocked() {
        // Select for update returns null when another process already updated the same row
        when(session.selectOne("AttestationResult.selectForUpdate", 5L))
            .thenThrow(new PersistenceException("PG/SQL Error: could not obtain lock on row"));

        service.processAttestationResult(5L, worker);

        verify(session).selectOne("AttestationResult.selectForUpdate", 5L);
        verify(session).rollback();
        verify(session).close();

        verifyNoMoreInteractions(session);
        verifyNoInteractions(worker);
    }
}
