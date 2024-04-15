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
import com.suse.coco.module.snpguest.execution.ProcessOutput;
import com.suse.coco.module.snpguest.execution.SNPGuestWrapper;
import com.suse.coco.module.snpguest.io.VerificationDirectoryProvider;
import com.suse.coco.module.snpguest.model.AttestationReport;
import com.suse.coco.module.snpguest.model.EpycGeneration;
import com.suse.coco.modules.AttestationWorker;
import com.suse.common.io.ByteSequenceFinder;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

/**
 * Worker class for verifying the reports with SNPGuest
 */
public class SNPGuestWorker implements AttestationWorker {

    private static final Logger LOGGER = LogManager.getLogger(SNPGuestWorker.class);

    private final VerificationDirectoryProvider directoryProvider;

    private final SNPGuestWrapper snpGuest;

    private final ByteSequenceFinder sequenceFinder;

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
    }

    @Override
    public boolean process(SqlSession session, AttestationResult result) {

        try {
            LOGGER.debug("Processing attestation result {}", result.getId());

            AttestationReport report = session.selectOne("SNPGuestModule.retrieveReport", result.getReportId());
            if (report == null) {
                LOGGER.error("Unable to retrieve attestation report for result {}", result.getId());
                return false;
            }

             LOGGER.debug("Loaded report {}", report);
             if (report.getCpuGeneration() == EpycGeneration.UNKNOWN) {
                LOGGER.error("Unable to identify Epyc processor generation for attestation report {}", report.getId());
                return false;
            }

            try (var workingDir = directoryProvider.createDirectoryFor(result.getId(), report)) {
                // Ensure the nonce is present in the report
                sequenceFinder.setSequence(report.getRandomNonce());
                if (sequenceFinder.search(report.getReport()) == -1) {
                    LOGGER.error("The report does not contain the expected random nonce");
                    return false;
                }

                // Reference to the paths snpguest needs to work with
                Path certsPath = workingDir.getCertsPath();
                Path reportPath = workingDir.getReportPath();

                // Download the VCEK for this cpu model
                ProcessOutput processOutput = snpGuest.fetchVCEK(report.getCpuGeneration(), certsPath, reportPath);
                if (processOutput.getExitCode() != 0 || !workingDir.isVCEKAvailable()) {
                    LOGGER.error("Unable to retrieve VCEK file. SNPGuest return {}", processOutput.getExitCode());
                    return false;
                }

                // Verify the certificates
                processOutput = snpGuest.verifyCertificates(certsPath);
                if (processOutput.getExitCode() != 0) {
                    LOGGER.error("Unable to verify the validity of the certificates. SNPGuest return {}",
                        processOutput.getExitCode());
                    return false;
                }

                // Verify the actual attestation report
                processOutput = snpGuest.verifyAttestation(certsPath, reportPath);
                if (processOutput.getExitCode() != 0) {
                    LOGGER.error("Unable to verify the attestation report. SNPGuest return {}",
                        processOutput.getExitCode());
                    return false;
                }

                processOutput = snpGuest.displayReport(reportPath);
                if (processOutput.getExitCode() != 0) {
                    LOGGER.error("Unable to get the attestation report in human readable format. SNPGuest return {}",
                        processOutput.getExitCode());
                    return false;
                }

                result.setDetails(processOutput.getStandardOutput());
            }

            return true;
        }
        catch (Exception ex) {
            LOGGER.error("Unable to process attestation result {}", result.getId(), ex);
        }

        return false;
    }
}
