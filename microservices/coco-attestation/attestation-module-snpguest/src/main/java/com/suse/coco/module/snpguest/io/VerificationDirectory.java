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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * A class representing a verification directory, used to verify an attestation report. All files and the
 * directory itself will be removed on resource clean up.
 */
public class VerificationDirectory implements AutoCloseable {

    /**
     * The report binary file
     */
    public static final String REPORT_FILE = "report.bin";

    /**
     * The VCEK file name
     */
    public static final String VCEK_FILE = "vcek.der";

    private final Path path;

    VerificationDirectory(Path pathIn) {
        this.path = pathIn;
    }

    public Path getBasePath() {
        return path;
    }

    public Path getCertsPath() {
        return path.resolve("certs");
    }

    public Path getReportPath() {
        return path.resolve(REPORT_FILE);
    }

    public Path getVCEKPath() {
        return getCertsPath().resolve(VCEK_FILE);
    }

    /**
     * Check if the VCEK file exists
     * @return true if the file {@link #VCEK_FILE} exists.
     */
    public boolean isVCEKAvailable() {
        return Files.exists(getCertsPath().resolve(VCEK_FILE));
    }

    /**
     * Removes this verification directory and all the files contained within it
     * @throws IOException when the deletion fails.
     */
    @Override
    public void close() throws IOException {
        try (Stream<Path> fileStream = Files.walk(path)) {
            fileStream
                .sorted(Comparator.reverseOrder())
                .forEach(file -> {
                    try {
                        Files.delete(file);
                    }
                    catch (IOException ioEx) {
                        throw new UncheckedIOException(ioEx);
                    }
                });
        }
        catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }
}
