/*
 * Copyright (c) 2021 SUSE LLC
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
package com.suse.manager.webui.controllers.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

/**
 * Exception class that provides detailed information on a shell command execution
 */
public class CommandExecutionException extends Exception {

    private final Integer exitValue;
    private final String stdout;
    private final String stderr;

    /**
     * Create an exception with a message and a subprocess handle. Detailed output will only be appended if the command
     * has already returned.
     *
     * @param message the exception message
     * @param execProcess the handle for the subprocess that runs the shell command
     */
    public CommandExecutionException(String message, Process execProcess) {
        this(message, execProcess, null);
    }

    /**
     * Create an exception with a message, a subprocess handle and a cause. Detailed output will only be appended if the
     * command has already returned.
     *
     * @param message the exception message
     * @param execProcess the handle for the subprocess that runs the shell command
     * @param cause the cause
     */
    public CommandExecutionException(String message, Process execProcess, Throwable cause) {
        super(message, cause);

        // Fill in output details if the process is finished
        if (!execProcess.isAlive()) {
            this.exitValue = execProcess.exitValue();
            String out = null;
            try {
                out = IOUtils.toString(execProcess.getInputStream(), StandardCharsets.UTF_8);
            }
            catch (IOException ignored) {
                // Ignored
            }
            stdout = out;

            String err = null;
            try {
                err = IOUtils.toString(execProcess.getErrorStream(), StandardCharsets.UTF_8);
            }
            catch (IOException ignored) {
                // Ignored
            }
            stderr = err;
        }
        else {
            stdout = null;
            stderr = null;
            exitValue = null;
        }
    }

    /**
     * Returns the exeption message with exit code and output information on the command's result appended
     *
     * @return the message
     */
    @Override
    public String getMessage() {
        StringJoiner sj = new StringJoiner("\n");
        if (StringUtils.isNotEmpty(super.getMessage())) {
            sj.add(super.getMessage());
        }
        if (exitValue != null) {
            sj.add("Process exited with code: " + exitValue);
        }
        if (StringUtils.isNotEmpty(stdout)) {
            sj.add("STDOUT: " + stdout);
        }
        if (StringUtils.isNotEmpty(stderr)) {
            sj.add("STDERR: " + stderr);
        }

        return sj.toString();
    }
}
