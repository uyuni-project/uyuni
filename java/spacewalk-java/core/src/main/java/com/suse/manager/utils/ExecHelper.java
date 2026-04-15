/*
 * Copyright (c) 2022 SUSE LLC
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
package com.suse.manager.utils;

import com.redhat.rhn.common.RhnRuntimeException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.function.Supplier;

public class ExecHelper {

    public static final String TOOL_FAILED_MSG = "External tool failed: ";

    private static final Logger LOG = LogManager.getLogger(ExecHelper.class);

    private final Supplier<Runtime> runtimeSupplier;

    /**
     * Create a new helper instance mostly used for testing
     *
     * @param runtimeSupplierIn a runtime supplier
     */
    public ExecHelper(Supplier<Runtime> runtimeSupplierIn) {
       runtimeSupplier = runtimeSupplierIn;
    }

    /**
     * Default constructor using the current runtime
     */
    public ExecHelper() {
        runtimeSupplier = () -> Runtime.getRuntime();
    }

    /**
     * Execute an external process.
     *
     * @param command the command to run
     * @param input the value to pass as standard input of the process
     * @return the command output
     *
     * @throws RhnRuntimeException if anything wrong happens or if the exit code is not 0
     */
    public String exec(List<String> command, String input) throws RhnRuntimeException {
        Process process;
        try {
            process = runtimeSupplier.get().exec(command.toArray(new String[0]));
        }
        catch (IOException err) {
            throw new RhnRuntimeException("Failed to run external process", err);
        }

        // Write to the stdin of the program
        try (
                OutputStreamWriter outStream = new OutputStreamWriter(process.getOutputStream());
                BufferedWriter output = new BufferedWriter(outStream)
        ) {
            output.write(input);
            output.flush();
        }
        catch (IOException err) {
            throw new RhnRuntimeException("Failed to write to external process input");
        }

        try {
            if (process.waitFor() != 0) {
                String errMsg = new String(process.getErrorStream().readAllBytes());
                throw new RhnRuntimeException(TOOL_FAILED_MSG + errMsg);
            }
            return new String(process.getInputStream().readAllBytes());
        }
        catch (InterruptedException err) {
            LOG.error(err);
            Thread.currentThread().interrupt();
            throw new RhnRuntimeException("External tool interrupted", err);
        }
        catch (IOException err) {
            throw new RhnRuntimeException("Failed to read external process error output", err);
        }
    }
}
