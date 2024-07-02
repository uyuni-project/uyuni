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

package com.suse.coco.module.snpguest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.suse.coco.model.AttestationResult;
import com.suse.coco.model.AttestationStatus;
import com.suse.coco.module.snpguest.execution.ProcessOutput;
import com.suse.coco.module.snpguest.execution.SNPGuestWrapper;
import com.suse.coco.module.snpguest.io.VerificationDirectory;
import com.suse.coco.module.snpguest.io.VerificationDirectoryProvider;
import com.suse.coco.module.snpguest.model.AttestationReport;
import com.suse.coco.module.snpguest.model.EpycGeneration;
import com.suse.common.io.ByteSequenceFinder;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

@ExtendWith(MockitoExtension.class)
class SNPGuestWorkerTest {

    private static final Path MOCK_CERTS_DIR = Path.of("fakedir/certs");
    private static final Path MOCK_REPORT_FILE = Path.of("fakedir/report.bin");

    private AttestationResult result;

    private AttestationReport report;

    @Mock
    private SqlSession session;

    @Mock
    private VerificationDirectoryProvider directoryProvider;

    @Mock
    private VerificationDirectory directory;

    @Mock
    private SNPGuestWrapper snpWrapper;

    @Mock
    private ByteSequenceFinder sequenceFinder;

    private SNPGuestWorker worker;

    @BeforeEach
    public void setup() {
        result = new AttestationResult();
        result.setId(1L);
        result.setStatus(AttestationStatus.PENDING);
        result.setReportId(5L);

        report = new AttestationReport();
        report.setId(5L);

        // Common mocking
        when(session.selectOne("SNPGuestModule.retrieveReport", 5L)).thenReturn(report);

        worker = new SNPGuestWorker(directoryProvider, snpWrapper, sequenceFinder);
    }

    @Test
    @DisplayName("Rejects report if an exception is thrown")
    void rejectsWhenExceptionHappens() {

        when(session.selectOne("SNPGuestModule.retrieveReport", 5L))
            .thenThrow(PersistenceException.class);

        assertFalse(worker.process(session, result));
        assertEquals(
            String.join(System.lineSeparator(),
                "- Unable to process attestation result: org.apache.ibatis.exceptions.PersistenceException",
                ""
            ),
            result.getProcessOutput()
        );

        // Verify no files have been created
        verifyNoInteractions(directoryProvider);

        // Verify no checks have been performed
        verifyNoInteractions(sequenceFinder);
        verifyNoInteractions(snpWrapper);
    }

    @Test
    @DisplayName("Rejects report if the data is not found")
    void rejectsWhenReportIsNotFound() {

        when(session.selectOne("SNPGuestModule.retrieveReport", 5L))
            .thenReturn(null);

        assertFalse(worker.process(session, result));
        assertEquals(
            String.join(System.lineSeparator(),
                "- Unable to retrieve attestation report for result",
                ""
            ),
            result.getProcessOutput()
        );

        // Verify no files have been created
        verifyNoInteractions(directoryProvider);

        // Verify no checks have been performed
        verifyNoInteractions(sequenceFinder);
        verifyNoInteractions(snpWrapper);
    }

    @Test
    @DisplayName("Rejects report if processor model is unknown")
    void rejectsWithUnknownProcessorModel() {
        // Set the model as UNKNOWN
        report.setCpuGeneration(EpycGeneration.UNKNOWN);

        assertFalse(worker.process(session, result));
        assertEquals(
            String.join(System.lineSeparator(),
                "- Unable to identify Epyc processor generation for attestation report",
                ""
            ),
            result.getProcessOutput()
        );

        // Verify no files have been created
        verifyNoInteractions(directoryProvider);

        // Verify no checks have been performed
        verifyNoInteractions(sequenceFinder);
        verifyNoInteractions(snpWrapper);
    }

    @Test
    @DisplayName("Rejects report if nonce is null")
    void rejectsWithNullNonce() {
        // Set the model as UNKNOWN
        report.setRandomNonce(null);
        report.setReport("REPORT".getBytes(StandardCharsets.UTF_8));

        assertFalse(worker.process(session, result));
        assertEquals(
            String.join(System.lineSeparator(),
                "- Unable to verify: randomized nonce not found",
                ""
            ),
            result.getProcessOutput()
        );

        // Verify no files have been created
        verifyNoInteractions(directoryProvider);

        // Verify no checks have been performed
        verifyNoInteractions(sequenceFinder);
        verifyNoInteractions(snpWrapper);
    }

    @Test
    @DisplayName("Rejects report if nonce is empty")
    void rejectsWithEmptyNonce() {
        // Set the model as UNKNOWN
        report.setRandomNonce(new byte[0]);
        report.setReport("REPORT".getBytes(StandardCharsets.UTF_8));

        assertFalse(worker.process(session, result));
        assertEquals(
            String.join(System.lineSeparator(),
                "- Unable to verify: randomized nonce not found",
                ""
            ),
            result.getProcessOutput()
        );

        // Verify no files have been created
        verifyNoInteractions(directoryProvider);

        // Verify no checks have been performed
        verifyNoInteractions(sequenceFinder);
        verifyNoInteractions(snpWrapper);
    }

    @Test
    @DisplayName("Rejects report if data is null")
    void rejectsWithNullReport() {
        // Set the model as UNKNOWN
        report.setReport(null);
        report.setRandomNonce("NONCE".getBytes(StandardCharsets.UTF_8));

        assertFalse(worker.process(session, result));
        assertEquals(
            String.join(System.lineSeparator(),
                "- Unable to verify: attestation report not found",
                ""
            ),
            result.getProcessOutput()
        );

        // Verify no files have been created
        verifyNoInteractions(directoryProvider);

        // Verify no checks have been performed
        verifyNoInteractions(sequenceFinder);
        verifyNoInteractions(snpWrapper);
    }

    @Test
    @DisplayName("Rejects report if data is empty")
    void rejectsWithEmptyReport() {
        // Set the model as UNKNOWN
        report.setReport(new byte[0]);
        report.setRandomNonce("NONCE".getBytes(StandardCharsets.UTF_8));

        assertFalse(worker.process(session, result));
        assertEquals(
            String.join(System.lineSeparator(),
                "- Unable to verify: attestation report not found",
                ""
            ),
            result.getProcessOutput()
        );

        // Verify no files have been created
        verifyNoInteractions(directoryProvider);

        // Verify no checks have been performed
        verifyNoInteractions(sequenceFinder);
        verifyNoInteractions(snpWrapper);
    }

    @Test
    @DisplayName("Rejects report if nonce is not found in sequence")
    void rejectsWithoutTheNonceInTheReport() throws IOException {
        // Set the model as UNKNOWN
        report.setCpuGeneration(EpycGeneration.MILAN);
        report.setRandomNonce("NONCE".getBytes(StandardCharsets.UTF_8));
        report.setReport("REPORT THAT DOES NOT CONTAIN THE RANDOM SEQUENCE".getBytes(StandardCharsets.UTF_8));

        when(directoryProvider.createDirectoryFor(1L, report)).thenReturn(directory);

        // Nonce verification fails
        when(sequenceFinder.search(report.getReport())).thenReturn(-1);

        assertFalse(worker.process(session, result));
        assertEquals(
            String.join(System.lineSeparator(),
                "- The report does not contain the expected random nonce",
                ""
            ),
            result.getProcessOutput()
        );

        // Verify the required files have been created and deleted
        verify(directoryProvider).createDirectoryFor(1L, report);
        verify(directory).close();

        // Verify the sequence has been searched
        verify(sequenceFinder).setSequence(report.getRandomNonce());
        verify(sequenceFinder).search(report.getReport());

        // Verify no checks have been performed
        verifyNoInteractions(snpWrapper);
    }

    @Test
    @DisplayName("Rejects report if the VCEK could not be retrieved")
    void rejectsWhenVECKIsNotRetrieved() throws IOException, ExecutionException {
        // Set the model as UNKNOWN
        report.setCpuGeneration(EpycGeneration.MILAN);
        report.setRandomNonce("NONCE".getBytes(StandardCharsets.UTF_8));
        report.setReport("REPORT WITH NONCE".getBytes(StandardCharsets.UTF_8));

        when(directoryProvider.createDirectoryFor(1L, report)).thenReturn(directory);

        when(directory.getCertsPath()).thenReturn(MOCK_CERTS_DIR);
        when(directory.getReportPath()).thenReturn(MOCK_REPORT_FILE);

        // Pass nonce verification
        when(sequenceFinder.search(report.getReport())).thenReturn(12);

        // Fetching fails
        when(snpWrapper.fetchVCEK(EpycGeneration.MILAN, MOCK_CERTS_DIR, MOCK_REPORT_FILE))
            .thenReturn(new ProcessOutput(-1, "", "Fetch FAILED"));

        assertFalse(worker.process(session, result));
        assertEquals(
            String.join(System.lineSeparator(),
                "- The report contains the expected random nonce",
                "- Unable to retrieve VCEK file:",
                "    - Exit code: -1",
                "    - Standard error: >",
                "        Fetch FAILED",
                ""
            ),
            result.getProcessOutput()
        );

        // Verify the required files have been created and deleted
        verify(directoryProvider).createDirectoryFor(1L, report);
        verify(directory).close();

        // Verify the sequence has been searched
        verify(sequenceFinder).setSequence(report.getRandomNonce());
        verify(sequenceFinder).search(report.getReport());

        // Verify the VCEK was fetched but since the result was not ok the VCEK path was not evaluated
        verify(snpWrapper).fetchVCEK(EpycGeneration.MILAN, MOCK_CERTS_DIR, MOCK_REPORT_FILE);
        verify(directory, never()).isVCEKAvailable();

        verifyNoMoreInteractions(snpWrapper);
    }

    @Test
    @DisplayName("Rejects report if the VCEK is not readable")
    void rejectsWhenVECKIsNotReadable() throws IOException, ExecutionException {
        // Set the model as UNKNOWN
        report.setCpuGeneration(EpycGeneration.MILAN);
        report.setRandomNonce("NONCE".getBytes(StandardCharsets.UTF_8));
        report.setReport("REPORT WITH NONCE".getBytes(StandardCharsets.UTF_8));

        when(directoryProvider.createDirectoryFor(1L, report)).thenReturn(directory);

        when(directory.getCertsPath()).thenReturn(MOCK_CERTS_DIR);
        when(directory.getReportPath()).thenReturn(MOCK_REPORT_FILE);

        // Pass nonce verification
        when(sequenceFinder.search(report.getReport())).thenReturn(12);

        // Fetch works but the file does not exist
        when(snpWrapper.fetchVCEK(EpycGeneration.MILAN, MOCK_CERTS_DIR, MOCK_REPORT_FILE))
            .thenReturn(new ProcessOutput(0, "Fetch ok", ""));
        when(directory.isVCEKAvailable()).thenReturn(false);

        assertFalse(worker.process(session, result));
        assertEquals(
            String.join(System.lineSeparator(),
                "- The report contains the expected random nonce",
                "- Unable to retrieve VCEK file:",
                "    - Standard output: >",
                "        Fetch ok",
                ""
            ),
            result.getProcessOutput()
        );

        // Verify the required files have been created and deleted
        verify(directoryProvider).createDirectoryFor(1L, report);
        verify(directory).close();

        // Verify the sequence has been searched
        verify(sequenceFinder).setSequence(report.getRandomNonce());
        verify(sequenceFinder).search(report.getReport());

        // Verify the VCEK was fetched
        verify(snpWrapper).fetchVCEK(EpycGeneration.MILAN, MOCK_CERTS_DIR, MOCK_REPORT_FILE);
        verify(directory).isVCEKAvailable();

        verifyNoMoreInteractions(snpWrapper);
    }

    @Test
    @DisplayName("Rejects report if the certificate verification fails")
    void rejectsWhenCertificatesCannotBeVerified() throws IOException, ExecutionException {
        // Set the model as UNKNOWN
        report.setCpuGeneration(EpycGeneration.MILAN);
        report.setRandomNonce("NONCE".getBytes(StandardCharsets.UTF_8));
        report.setReport("REPORT WITH NONCE".getBytes(StandardCharsets.UTF_8));

        when(directoryProvider.createDirectoryFor(1L, report)).thenReturn(directory);

        when(directory.getCertsPath()).thenReturn(MOCK_CERTS_DIR);
        when(directory.getReportPath()).thenReturn(MOCK_REPORT_FILE);

        // Pass nonce verification
        when(sequenceFinder.search(report.getReport())).thenReturn(12);

        // Pass fetching of the VECK
        when(snpWrapper.fetchVCEK(EpycGeneration.MILAN, MOCK_CERTS_DIR, MOCK_REPORT_FILE))
            .thenReturn(new ProcessOutput(0, "Fetch ok", ""));
        when(directory.isVCEKAvailable()).thenReturn(true);

        // Fail the certificate verification
        when(snpWrapper.verifyCertificates(MOCK_CERTS_DIR))
            .thenReturn(new ProcessOutput(-1, "", "Certificate verify FAILED"));

        assertFalse(worker.process(session, result));
        assertEquals(
            String.join(System.lineSeparator(),
                "- The report contains the expected random nonce",
                "- VCEK fetched successfully:",
                "    - Standard output: >",
                "        Fetch ok",
                "- Unable to verify the validity of the certificates:",
                "    - Exit code: -1",
                "    - Standard error: >",
                "        Certificate verify FAILED",
                ""
            ),
            result.getProcessOutput()
        );

        // Verify the required files have been created and deleted
        verify(directoryProvider).createDirectoryFor(1L, report);
        verify(directory).close();

        // Verify the sequence has been searched
        verify(sequenceFinder).setSequence(report.getRandomNonce());
        verify(sequenceFinder).search(report.getReport());

        // Verify the VCEK was fetched
        verify(snpWrapper).fetchVCEK(EpycGeneration.MILAN, MOCK_CERTS_DIR, MOCK_REPORT_FILE);
        verify(directory).isVCEKAvailable();

        // Verify the certificate verification have happened
        verify(snpWrapper).verifyCertificates(MOCK_CERTS_DIR);

        verifyNoMoreInteractions(snpWrapper);
    }

    @Test
    @DisplayName("Rejects report if the attestation verification fails")
    void rejectsWhenAttestationCannotBeVerified() throws IOException, ExecutionException {
        // Set the model as UNKNOWN
        report.setCpuGeneration(EpycGeneration.MILAN);
        report.setRandomNonce("NONCE".getBytes(StandardCharsets.UTF_8));
        report.setReport("REPORT WITH NONCE".getBytes(StandardCharsets.UTF_8));

        when(directoryProvider.createDirectoryFor(1L, report)).thenReturn(directory);

        when(directory.getCertsPath()).thenReturn(MOCK_CERTS_DIR);
        when(directory.getReportPath()).thenReturn(MOCK_REPORT_FILE);

        // Pass nonce verification
        when(sequenceFinder.search(report.getReport())).thenReturn(12);

        // Pass fetching of the VECK
        when(snpWrapper.fetchVCEK(EpycGeneration.MILAN, MOCK_CERTS_DIR, MOCK_REPORT_FILE))
            .thenReturn(new ProcessOutput(0, "Fetch ok", ""));
        when(directory.isVCEKAvailable()).thenReturn(true);

        // Pass the certificate verification
        when(snpWrapper.verifyCertificates(MOCK_CERTS_DIR))
            .thenReturn(new ProcessOutput(0, "Certificate verify ok", ""));

        // Fail the attestation verification
        when(snpWrapper.verifyAttestation(MOCK_CERTS_DIR, MOCK_REPORT_FILE))
            .thenReturn(new ProcessOutput(-1, "", "Attestation verify FAILED"));

        assertFalse(worker.process(session, result));
        assertEquals(
            String.join(System.lineSeparator(),
                "- The report contains the expected random nonce",
                "- VCEK fetched successfully:",
                "    - Standard output: >",
                "        Fetch ok",
                "- Certification chain validated successfully:",
                "    - Standard output: >",
                "        Certificate verify ok",
                "- Unable to verify the attestation report:",
                "    - Exit code: -1",
                "    - Standard error: >",
                "        Attestation verify FAILED",
                ""
            ),
            result.getProcessOutput()
        );

        // Verify the required files have been created and deleted
        verify(directoryProvider).createDirectoryFor(1L, report);
        verify(directory).close();

        // Verify the sequence has been searched
        verify(sequenceFinder).setSequence(report.getRandomNonce());
        verify(sequenceFinder).search(report.getReport());

        // Verify the VCEK was fetched
        verify(snpWrapper).fetchVCEK(EpycGeneration.MILAN, MOCK_CERTS_DIR, MOCK_REPORT_FILE);
        verify(directory).isVCEKAvailable();

        // Verify the certificate verification have happened
        verify(snpWrapper).verifyCertificates(MOCK_CERTS_DIR);

        // Verify the attestation verification have happened
        verify(snpWrapper).verifyAttestation(MOCK_CERTS_DIR, MOCK_REPORT_FILE);
    }

    @Test
    @DisplayName("Approves report if all checks pass")
    void approvesReportIfAllChecksPass() throws IOException, ExecutionException {
        // Set the model as UNKNOWN
        report.setCpuGeneration(EpycGeneration.MILAN);
        report.setRandomNonce("NONCE".getBytes(StandardCharsets.UTF_8));
        report.setReport("REPORT WITH NONCE".getBytes(StandardCharsets.UTF_8));

        when(directoryProvider.createDirectoryFor(1L, report)).thenReturn(directory);

        when(directory.getCertsPath()).thenReturn(MOCK_CERTS_DIR);
        when(directory.getReportPath()).thenReturn(MOCK_REPORT_FILE);

        // Pass nonce verification
        when(sequenceFinder.search(report.getReport())).thenReturn(12);

        // Pass fetching of the VECK
        when(snpWrapper.fetchVCEK(EpycGeneration.MILAN, MOCK_CERTS_DIR, MOCK_REPORT_FILE))
            .thenReturn(new ProcessOutput(0, "Fetch ok", ""));
        when(directory.isVCEKAvailable()).thenReturn(true);

        // Pass the certificate verification
        when(snpWrapper.verifyCertificates(MOCK_CERTS_DIR))
            .thenReturn(new ProcessOutput(0, "Certificate verify ok", ""));

        // Pass the attestation verification
        when(snpWrapper.verifyAttestation(MOCK_CERTS_DIR, MOCK_REPORT_FILE))
            .thenReturn(new ProcessOutput(0, "Attestation verify ok", ""));

        // Retrieve the report
        when(snpWrapper.displayReport(MOCK_REPORT_FILE)).thenReturn(new ProcessOutput(0, "dummy-report", ""));

        assertTrue(worker.process(session, result));
        assertEquals("dummy-report", result.getDetails());
        assertEquals(
            String.join(System.lineSeparator(),
                "- The report contains the expected random nonce",
                "- VCEK fetched successfully:",
                "    - Standard output: >",
                "        Fetch ok",
                "- Certification chain validated successfully:",
                "    - Standard output: >",
                "        Certificate verify ok",
                "- Attestation report correctly verified:",
                "    - Standard output: >",
                "        Attestation verify ok",
                ""
            ),
            result.getProcessOutput()
        );

        // Verify the required files have been created and deleted
        verify(directoryProvider).createDirectoryFor(1L, report);
        verify(directory).close();

        // Verify the sequence has been searched
        verify(sequenceFinder).setSequence(report.getRandomNonce());
        verify(sequenceFinder).search(report.getReport());

        // Verify the VCEK was fetched
        verify(snpWrapper).fetchVCEK(EpycGeneration.MILAN, MOCK_CERTS_DIR, MOCK_REPORT_FILE);
        verify(directory).isVCEKAvailable();

        // Verify the certificate verification have happened
        verify(snpWrapper).verifyCertificates(MOCK_CERTS_DIR);

        // Verify the attestation verification have happened
        verify(snpWrapper).verifyAttestation(MOCK_CERTS_DIR, MOCK_REPORT_FILE);

        // Verify the report display have happened
        verify(snpWrapper).displayReport(MOCK_REPORT_FILE);
    }
}
