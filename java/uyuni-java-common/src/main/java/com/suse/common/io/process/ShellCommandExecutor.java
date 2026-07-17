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

package com.suse.common.io.process;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShellCommandExecutor {
    private static final Logger LOGGER = LogManager.getLogger(ShellCommandExecutor.class);
    private final Runtime runtime;
    protected String lastExecutedCommand;

    /**
     * Constructor
     */
    public ShellCommandExecutor() {
        runtime = Runtime.getRuntime();
        lastExecutedCommand = "";
    }

    /**
     * Gets the last executed command
     * @return the last executed command string
     */
    public String getLastExecutedCommand() {
        return lastExecutedCommand;
    }

    /**
     * Executes a command in a process
     * @param command list of strings to be joined as a command
     * @return a process output object
     * @throws ExecutionException if anything goes wrong
     */
    public ProcessOutput executeProcess(String... command) throws ExecutionException {
        lastExecutedCommand = String.join(" ", command);

        Process process;
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Executing {}", Arrays.toString(command));
            }

            process = runtime.exec(command);
        }
        catch (IOException ex) {
            throw new ExecutionException("Unable to create process", ex);
        }

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            int exitCode = process.waitFor();

            String standardOutputIn = getOutput(process.getInputStream(), "stdout");
            String standardErrorIn = getOutput(process.getErrorStream(), "stderr");

            return new ProcessOutput(
                    exitCode,
                    standardOutputIn,
                    standardErrorIn
            );
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ExecutionException("Unable to get process execution result", ex);
        }
        catch (IOException ex) {
            throw new ExecutionException("Unable to get process execution output", ex);
        }
        finally {
            executor.shutdown();
        }
    }

    private String getOutput(InputStream stream, String logPrefix) throws IOException {
        StringWriter writer = new StringWriter();

        try (BufferedReader inErr = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = inErr.readLine()) != null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(logPrefix, line);
                }

                writer.write(line);
                writer.write(System.lineSeparator());
            }

            return writer.toString();
        }
    }
}
