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

import com.suse.coco.model.AttestationResult;
import com.suse.coco.module.AttestationWorker;
import com.suse.coco.module.pvattest.execution.PvattestWrapper;
import com.suse.coco.module.pvattest.model.AttestationReport;
import com.suse.coco.module.pvattest.model.AttestationRequest;
import com.suse.coco.module.pvattest.model.AttestationResponse;
import com.suse.coco.module.pvattest.model.IbmZGeneration;
import com.suse.common.io.process.ProcessOutput;
import com.suse.common.security.CertificateHelper;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.cert.CertificateException;
import java.util.Optional;

/**
 * Worker class for verifying the reports with SNPGuest
 */
public class PvattestWorker implements AttestationWorker {

    private static final Logger LOGGER = LogManager.getLogger(PvattestWorker.class);

    private static final int INDENT_SIZE = 4;

    private final PvattestWrapper pvattest;

    private final StringBuilder outputBuilder;

    /**
     * Default constructor.
     */
    public PvattestWorker() {
        this(new PvattestWrapper());
    }

    /**
     * Constructor with explicit dependencies, for unit test only.
     *
     * @param pvattestWrapperIn the pvattest executor
     */
    PvattestWorker(PvattestWrapper pvattestWrapperIn) {
        this.pvattest = pvattestWrapperIn;
        this.outputBuilder = new StringBuilder();
    }

    public static final String ATTESTATION_REQUEST_BIN_TAG = "attestation_request";
    public static final String ATTESTATION_PROTECTION_KEY_TAG = "attestation_protection_key";

    @Override
    public boolean processRequest(SqlSession session, AttestationResult result) {
        // Reset the output string builder
        outputBuilder.setLength(0);

        try {
            LOGGER.debug("Processing attestation request {}", result.getId());

            AttestationReport report = session.selectOne("PvattestModule.retrieveReport", result.getReportId());
            if (report == null) {
                appendError("Unable to retrieve attestation report for request");
                return false;
            }

            LOGGER.debug("Loaded report {}", report);
            if (report.getCpuGeneration() == IbmZGeneration.UNKNOWN) {
                appendError("Unable to identify IBM processor generation for attestation request");
                return false;
            }

            //retrieve and parse host key document
            String hostKeyDocument = report.getHostKeyDocument();
            if (null == hostKeyDocument || hostKeyDocument.isBlank()) {
                appendError("Unable to create request: host key document not found");
                return false;
            }

            //create attestation request
            String hostKeyDocumentContent = hostKeyDocument.replace("\\n", "\n");

            try {
                CertificateHelper.parse(hostKeyDocumentContent);
            }
            catch (CertificateException eIn) {
                appendError("Unable to create request: could not parse host key certificate");
                return false;
            }

            AttestationRequest attestationRequest =
                    pvattest.createVerifyDownloadCertificates(hostKeyDocumentContent);

            //compute attestation request data
            String attestationRequestData = "{\"%s\": \"%s\",\"%s\": \"%s\"}"
                    .formatted(ATTESTATION_REQUEST_BIN_TAG, attestationRequest.base64AttestationRequest(),
                            ATTESTATION_PROTECTION_KEY_TAG, attestationRequest.base64AttestationProtectionKey());

            //set and save attestation request data
            result.setInData(attestationRequestData);
            return true;
        }
        catch (Exception ex) {
            String exceptionMessage = Optional.ofNullable(ex.getMessage()).orElse(ex.getClass().getName());
            appendError("Unable to create request: " + exceptionMessage, ex);
        }
        finally {
            result.setProcessOutput(outputBuilder.toString());
        }

        return false;
    }

    @Override
    public boolean processVerification(SqlSession session, AttestationResult result) {
        // Reset the output string builder
        outputBuilder.setLength(0);

        try {
            LOGGER.debug("Processing attestation response {}", result.getId());

            AttestationReport report = session.selectOne("PvattestModule.retrieveReport", result.getReportId());
            if (report == null) {
                appendError("Unable to retrieve attestation report for request");
                return false;
            }

            LOGGER.debug("Loaded report {}", report);
            if (report.getCpuGeneration() == IbmZGeneration.UNKNOWN) {
                appendError("Unable to identify IBM processor generation for attestation request");
                return false;
            }

            //retrieve and check attestation response
            if ((null == report.getAttestationResponse()) || (report.getAttestationResponse().length == 0)) {
                appendError("Unable to verify: attestation response not found in report output data");
                return false;
            }

            //retrieve and check secure extension header
            if ((null == report.getSecureExecutionHeader()) || (report.getSecureExecutionHeader().length == 0)) {
                appendError("Unable to verify: secure extension header not found in report config data");
                return false;
            }

            //retrieve and check attestation protection key
            if ((null == report.getAttestationProtectionKey()) || (report.getAttestationProtectionKey().length == 0)) {
                appendError("Unable to verify: attestation protection key not found in report input data");
                return false;
            }

            // Verify the actual attestation report
            AttestationResponse verificationResult =
                    pvattest.verifyAttestationResponse(report.getAttestationResponse(),
                            report.getSecureExecutionHeader(),
                            report.getAttestationProtectionKey());

            if (verificationResult.failed()) {
                appendError("Unable to verify: verification failed");
                return false;
            }

            String attestationResponseContent = verificationResult.attestationResponseContent();

            appendSuccess("Attestation report correctly verified");
            result.setDetails(attestationResponseContent);

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

    private void appendError(String message, Exception ex) {
        appendOutput(message, null);
        LOGGER.error(message, ex);
    }

    private void appendSuccess(String message) {
        appendOutput(message, null);
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
        if (processOutput.exitCode() != 0) {
            processBuilder.append("- Exit code: %d".formatted(processOutput.exitCode()).indent(INDENT_SIZE));
        }

        if (processOutput.hasStandardOutput()) {
            processBuilder.append("- Standard output: >".indent(INDENT_SIZE));
            processBuilder.append(processOutput.standardOutput().indent(INDENT_SIZE * 2));
        }

        if (processOutput.hasStandardError()) {
            processBuilder.append("- Standard error: >".indent(INDENT_SIZE));
            processBuilder.append(processOutput.standardError().indent(INDENT_SIZE * 2));
        }

        return processBuilder.toString();
    }
}
