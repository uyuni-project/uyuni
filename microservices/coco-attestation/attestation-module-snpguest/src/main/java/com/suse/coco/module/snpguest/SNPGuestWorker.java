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

import com.suse.coco.model.AttestationResult;
import com.suse.coco.module.AttestationWorker;
import com.suse.coco.module.snpguest.execution.ProcessOutput;
import com.suse.coco.module.snpguest.execution.SNPGuestWrapper;
import com.suse.coco.module.snpguest.io.VerificationDirectoryProvider;
import com.suse.coco.module.snpguest.model.AttestationReport;
import com.suse.coco.module.snpguest.model.EpycGeneration;
import com.suse.common.io.ByteSequenceFinder;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Worker class for verifying the reports with SNPGuest
 */
public class SNPGuestWorker implements AttestationWorker {

    private static final Logger LOGGER = LogManager.getLogger(SNPGuestWorker.class);

    private static final int INDENT_SIZE = 4;

    private final VerificationDirectoryProvider directoryProvider;

    private final SNPGuestWrapper snpGuest;

    private final ByteSequenceFinder sequenceFinder;

    private final StringBuilder outputBuilder;

    /**
     * Default constructor.
     */
    public SNPGuestWorker() {
        this(new VerificationDirectoryProvider(), new SNPGuestWrapper(), new ByteSequenceFinder());
    }

    /**
     * Constructor with explicit dependencies, for unit test only.
     * @param directoryProviderIn the verification directory provider
     * @param snpGuestWrapperIn the snpguest executor
     * @param sequenceFinderIn the byte sequence finder
     */
    SNPGuestWorker(VerificationDirectoryProvider directoryProviderIn, SNPGuestWrapper snpGuestWrapperIn,
                   ByteSequenceFinder sequenceFinderIn) {
        this.directoryProvider = directoryProviderIn;
        this.snpGuest = snpGuestWrapperIn;
        this.sequenceFinder = sequenceFinderIn;
        this.outputBuilder = new StringBuilder();
    }

    @Override
    public boolean process(SqlSession session, AttestationResult result) {
        // Reset the output string builder
        outputBuilder.setLength(0);

        try {
            LOGGER.debug("Processing attestation result {}", result.getId());

            AttestationReport report = session.selectOne("SNPGuestModule.retrieveReport", result.getReportId());
            if (report == null) {
                appendError("Unable to retrieve attestation report for result");
                return false;
            }

             LOGGER.debug("Loaded report {}", report);
             if (report.getCpuGeneration() == EpycGeneration.UNKNOWN) {
                appendError("Unable to identify Epyc processor generation for attestation report");
                return false;
            }

            if (report.getRandomNonce() == null || report.getRandomNonce().length == 0) {
                appendError("Unable to verify: randomized nonce not found");
                return false;
            }

             if (report.getReport() == null || report.getReport().length == 0) {
                 appendError("Unable to verify: attestation report not found");
                 return false;
             }

            try (var workingDir = directoryProvider.createDirectoryFor(result.getId(), report)) {
                // Ensure the nonce is present in the report
                sequenceFinder.setSequence(report.getRandomNonce());
                if (sequenceFinder.search(report.getReport()) == -1) {
                    appendError("The report does not contain the expected random nonce");
                    return false;
                }
                else {
                    appendSuccess("The report contains the expected random nonce");
                }

                // Reference to the paths snpguest needs to work with
                Path certsPath = workingDir.getCertsPath();
                Path reportPath = workingDir.getReportPath();

                // Download the VCEK for this cpu model
                ProcessOutput processOutput = snpGuest.fetchVCEK(report.getCpuGeneration(), certsPath, reportPath);
                if (processOutput.getExitCode() != 0 || !workingDir.isVCEKAvailable()) {
                    appendError("Unable to retrieve VCEK file", processOutput);
                    return false;
                }
                else {
                    appendSuccess("VCEK fetched successfully", processOutput);
                }

                // Verify the certificates
                processOutput = snpGuest.verifyCertificates(certsPath);
                if (processOutput.getExitCode() != 0) {
                    appendError("Unable to verify the validity of the certificates", processOutput);
                    return false;
                }
                else {
                    appendSuccess("Certification chain validated successfully", processOutput);
                }

                // Verify the actual attestation report
                processOutput = snpGuest.verifyAttestation(certsPath, reportPath);
                if (processOutput.getExitCode() != 0) {
                    appendError("Unable to verify the attestation report", processOutput);
                    return false;
                }
                else {
                    appendSuccess("Attestation report correctly verified", processOutput);
                }

                processOutput = snpGuest.displayReport(reportPath);
                if (processOutput.getExitCode() != 0) {
                    appendError("Unable to get the attestation report in human readable format", processOutput);
                    return false;
                }

                result.setDetails(processOutput.getStandardOutput());
            }

            return true;
        }
        catch (Exception ex) {
            String exceptionMessage = Optional.ofNullable(ex.getMessage()).orElse(ex.getClass().getName());
            appendError("Unable to process attestation result: " + exceptionMessage, ex);
        }
        finally {
            result.setProcessOutput(outputBuilder.toString());
        }

        return false;
    }

    private void appendError(String message) {
        appendOutput(message, null);
        LOGGER.error(message);
    }

    private void appendError(String message, ProcessOutput processOutput) {
        appendOutput(message, processOutput);
        LOGGER.error(message);
    }

    private void appendError(String message, Exception ex) {
        appendOutput(message, null);
        LOGGER.error(message, ex);
    }

    private void appendSuccess(String message) {
        appendOutput(message, null);
    }
    private void appendSuccess(String message, ProcessOutput output) {
        appendOutput(message, output);
    }

    private void appendOutput(String message, ProcessOutput processOutput) {
        outputBuilder.append("- ").append(message);

        String processDetails = getProcessOutputDetails(processOutput);
        if (!processDetails.isEmpty()) {
            outputBuilder.append(":")
                .append(System.lineSeparator())
                .append(processDetails);
        }
        else {
            outputBuilder.append(System.lineSeparator());
        }
    }

    private static String getProcessOutputDetails(ProcessOutput processOutput) {
        if (processOutput == null) {
            return "";
        }

        StringBuilder processBuilder = new StringBuilder();
        if (processOutput.getExitCode() != 0) {
            processBuilder.append(" ".repeat(INDENT_SIZE)).append("- Exit code: ")
                .append(processOutput.getExitCode())
                .append(System.lineSeparator());
        }

        if (processOutput.hasStandardOutput()) {
            processBuilder.append(" ".repeat(INDENT_SIZE)).append("- Standard output: >")
                .append(System.lineSeparator());
            processOutput.getStandardOutput().lines()
                .forEach(line ->
                    processBuilder.append(" ".repeat(INDENT_SIZE * 2)).append(line).append(System.lineSeparator())
                );
        }

        if (processOutput.hasStandardError()) {
            processBuilder.append(" ".repeat(INDENT_SIZE)).append("- Standard error: >")
                .append(System.lineSeparator());
            processOutput.getStandardError().lines()
                .forEach(line ->
                    processBuilder.append(" ".repeat(INDENT_SIZE * 2)).append(line).append(System.lineSeparator())
                );
        }

        return processBuilder.toString();
    }
}
