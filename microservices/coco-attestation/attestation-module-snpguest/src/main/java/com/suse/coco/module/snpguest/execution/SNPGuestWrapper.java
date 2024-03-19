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

package com.suse.coco.module.snpguest.execution;

import com.suse.coco.module.snpguest.model.EpycGeneration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Wrapper to execute the command line tool SNPGuest.
 */
public class SNPGuestWrapper {

    private static final Marker STDOUT_MARKER = MarkerManager.getMarker("stdout");

    private static final Marker STDERR_MARKER = MarkerManager.getMarker("stderr");

    private static final Logger LOGGER = LogManager.getLogger(SNPGuestWrapper.class);

    private static final Path SNPGUEST = Path.of("/usr/bin/snpguest");

    private final Runtime runtime;

    /**
     * Default constructor.
     */
    public SNPGuestWrapper() {
        this.runtime = Runtime.getRuntime();
    }

    /**
     * Constructor to specify a runtime. For unit testing.
     * @param runtimeIn the runtime used to execute processes
     */
    SNPGuestWrapper(Runtime runtimeIn) {
        this.runtime = runtimeIn;
    }

    /**
     * Fetch the VCEK from the KDS.
     * @param generation Specify the processor model for the certificate chain.
     * @param certsDir Directory to store the certificates in
     * @param report Path to attestation report to use to request VCEK
     * @return the exit code of the fetching process
     * @throws ExecutionException when an error happens during the process execution
     */
    public int fetchVCEK(EpycGeneration generation, Path certsDir, Path report) throws ExecutionException {
        return executeProcess(
            SNPGUEST.toString(),
            "fetch",
            "vcek",
            "DER",
            generation.name().toLowerCase(),
            certsDir.toString(),
            report.toString()
        );
    }

    /**
     * Verify the certificate chain.
     * @param certsDir Path to directory containing certificate chain
     * @return the exit code of the verification process
     * @throws ExecutionException when an error happens during the process execution
     */
    public int verifyCertificates(Path certsDir) throws ExecutionException {
        return executeProcess(
            SNPGUEST.toString(),
            "verify",
            "certs",
            certsDir.toString()
        );
    }

    /**
     * Verify the attestation report.
     * @param certsDir Path to directory containing VCEK.
     * @param report Path to attestation report to use for validation.
     * @return the exit code of the verification process
     * @throws ExecutionException when an error happens during the process execution
     */
    public int verifyAttestation(Path certsDir, Path report) throws ExecutionException {
        return executeProcess(
            SNPGUEST.toString(),
            "verify",
            "attestation",
            certsDir.toString(),
            report.toString()
        );
    }

    /**
     * Executes a commandline process
     * @param command the command line to execute
     * @return the exit code returned by the process
     * @throws ExecutionException when an error happens during the process execution
     */
    private int executeProcess(String... command) throws ExecutionException {
        Process snpguestProcess;

        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Executing {}", Arrays.toString(command));
            }

            snpguestProcess = runtime.exec(command);
        }
        catch (IOException ex) {
            throw new ExecutionException("Unable to create snpguest process", ex);
        }

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            // Process the standard output and error in background and log it
            if (LOGGER.isDebugEnabled()) {
                executor.submit(() -> logProcessOutput(STDOUT_MARKER, snpguestProcess.getInputStream()));
                executor.submit(() -> logProcessOutput(STDERR_MARKER, snpguestProcess.getErrorStream()));
            }

            return snpguestProcess.waitFor();
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ExecutionException("Unable to get snpguest execution result", ex);
        }
        finally {
            executor.shutdown();
        }
    }

    private static void logProcessOutput(Marker marker, InputStream stream) {
        try (BufferedReader inErr = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = inErr.readLine()) != null) {
                LOGGER.debug(marker, line);
            }
        }
        catch (IOException e) {
            LOGGER.error("Error reading stderr from external process", e);
        }
    }

}
