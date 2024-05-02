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
import java.io.StringWriter;
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
    public ProcessOutput fetchVCEK(EpycGeneration generation, Path certsDir, Path report) throws ExecutionException {
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
    public ProcessOutput verifyCertificates(Path certsDir) throws ExecutionException {
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
    public ProcessOutput verifyAttestation(Path certsDir, Path report) throws ExecutionException {
        return executeProcess(
            SNPGUEST.toString(),
            "verify",
            "attestation",
            certsDir.toString(),
            report.toString()
        );
    }

    /**
     * Display the attestation report.
     * @param report Path to attestation report to use for validation.
     * @return the exit code of the verification process
     * @throws ExecutionException when an error happens during the process execution
     */
    public ProcessOutput displayReport(Path report) throws ExecutionException {
        return executeProcess(
            SNPGUEST.toString(),
            "display",
            "report",
            report.toString()
        );
    }

    /**
     * Executes a commandline process
     * @param command the command line to execute
     * @return the exit code returned by the process
     * @throws ExecutionException when an error happens during the process execution
     */
    protected ProcessOutput executeProcess(String... command) throws ExecutionException {
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
            int exitCode = snpguestProcess.waitFor();

            String standardOutputIn = getOutput(snpguestProcess.getInputStream(), STDOUT_MARKER);
            String standardErrorIn = getOutput(snpguestProcess.getErrorStream(), STDERR_MARKER);

            return new ProcessOutput(
                exitCode,
                standardOutputIn,
                standardErrorIn
            );
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ExecutionException("Unable to get snpguest execution result", ex);
        }
        catch (IOException ex) {
            throw new ExecutionException("Unable to get snpguest execution output", ex);
        }
        finally {
            executor.shutdown();
        }
    }

    private static String getOutput(InputStream stream, Marker logMarker) throws IOException {
        StringWriter writer = new StringWriter();

        try (BufferedReader inErr = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = inErr.readLine()) != null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(logMarker, line);
                }

                writer.write(line);
                writer.write(System.lineSeparator());
            }

            return writer.toString();
        }
    }

}
