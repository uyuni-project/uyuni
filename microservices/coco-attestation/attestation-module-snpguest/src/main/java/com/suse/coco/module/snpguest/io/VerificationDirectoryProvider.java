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

package com.suse.coco.module.snpguest.io;

import com.suse.coco.module.snpguest.model.AttestationReport;
import com.suse.coco.module.snpguest.model.EpycGeneration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Creates {@link VerificationDirectory} used to store the data needed by the SNPGuest tool
 * to perform validation.
 *
 * @see com.suse.coco.module.snpguest.execution.SNPGuestWrapper
 */
public class VerificationDirectoryProvider {
    public static final Path DEFAULT_CERTIFICATION_PATH = Path.of("/usr/share/coco-attestation/certs");

    private static final Logger LOGGER = LogManager.getLogger(VerificationDirectoryProvider.class);

    private final Path baseWorkingDir;

    private final Path sourceCertificatesDir;

    /**
     * Default constructor. Creates the working directory in system temporary folder and reads certificates
     * from {@link #DEFAULT_CERTIFICATION_PATH}.
     */
    public VerificationDirectoryProvider() {
        this.baseWorkingDir = null;
        this.sourceCertificatesDir = DEFAULT_CERTIFICATION_PATH;
    }

    VerificationDirectoryProvider(Path baseWorkingDirIn, Path sourceCertificatesDirIn) {
        this.baseWorkingDir = baseWorkingDirIn;
        this.sourceCertificatesDir = sourceCertificatesDirIn;
    }

    /**
     * Creates and prepares a new verification directory. The creation process involves:
     * <ol>
     *     <li>Creating a folder</li>
     *     <li>Copying the certificates into the certs/ subdirectory</li>
     *     <li>Save the report under the file "report.bin"</li>
     * </ol>
     * @param resultId the id of the attestation result linked to this report
     * @param report the attestation report
     * @return the created {@link VerificationDirectory}
     * @throws IOException when something went wrong during the preparation of the directory.
     */
    public VerificationDirectory createDirectoryFor(long resultId, AttestationReport report) throws IOException {
        EpycGeneration cpuGeneration = report.getCpuGeneration();

        // Verify if the source directory exist for this generation
        Path cpuSpecificCerts = sourceCertificatesDir.resolve(cpuGeneration.name().toLowerCase());
        if (!Files.isReadable(cpuSpecificCerts) || !Files.isDirectory(cpuSpecificCerts)) {
            throw new FileNotFoundException("Cannot find certificate for cpu generation " + cpuGeneration);
        }

        // Create the temporary base directory
        Path basePath = createTemporaryPath("snp-guest-worker-" + resultId + "-");

        // Create the certs sub-directory
        Path certs = basePath.resolve("certs");
        Files.createDirectories(certs);

        // Copy the standard certificates
        try (Stream<Path> certsStream = Files.list(cpuSpecificCerts)) {
            certsStream
                .filter(file -> !Files.isDirectory(file) && Files.isReadable(file))
                .forEach(file -> {
                    try {
                        Files.copy(file, certs.resolve(file.getFileName()));
                    }
                    catch (IOException ioEx) {
                        throw new UncheckedIOException(ioEx);
                    }
                });
        }
        catch (UncheckedIOException ex) {
            throw ex.getCause();
        }

        // Store the report in the file report.bin
        Files.write(basePath.resolve("report.bin"), report.getReport());

        LOGGER.debug("Created directory {} for verifying of result {} using {} certs", basePath, resultId, cpuGeneration);
        return new VerificationDirectory(basePath);
    }

    private Path createTemporaryPath(String prefix) throws IOException {
        if (baseWorkingDir == null) {
            return Files.createTempDirectory(prefix);
        }

        return Files.createTempDirectory(baseWorkingDir, prefix);
    }
}
