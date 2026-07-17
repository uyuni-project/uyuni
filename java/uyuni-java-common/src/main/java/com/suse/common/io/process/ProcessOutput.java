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

public record ProcessOutput(int exitCode, String standardOutput, String standardError) {
    /**
     * Checks if process output is succeeded
     *
     * @return true if process output is succeeded
     */
    public boolean succeeded() {
        return 0 == exitCode;
    }

    /**
     * Checks if process output is failed
     *
     * @return true if process output is failed
     */
    public boolean failed() {
        return 0 != exitCode;
    }

    /**
     * Retrieves the error message, if any
     *
     * @return the error message
     */
    public String getErrorMessage() {
        int maxMessageLength = 2300;
        String msg = standardError;
        if (msg.isBlank()) {
            msg = standardOutput;
        }

        if (msg.length() > maxMessageLength) {
            return "... " + msg.substring(standardError.length() - maxMessageLength);
        }
        else {
            return msg;
        }
    }

    /**
     * Checks if there is a meaningful standard output
     *
     * @return true if there is a meaningful standard output
     */
    public boolean hasStandardOutput() {
        return standardOutput != null && !standardOutput.isBlank();
    }

    /**
     * Checks if there is a meaningful standard error
     *
     * @return true if there is a meaningful standard error
     */
    public boolean hasStandardError() {
        return standardError != null && !standardError.isBlank();
    }
}
