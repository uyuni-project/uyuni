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

import java.util.Objects;
import java.util.StringJoiner;

/**
 * Represent the output of the execution of a process
 */
public class ProcessOutput {

    private final int exitCode;

    private final String standardOutput;

    private final String standardError;

    /**
     * Default constructor
     * @param exitCodeIn process exit code
     * @param standardOutputIn the standard output
     * @param standardErrorIn the standard error
     */
    public ProcessOutput(int exitCodeIn, String standardOutputIn, String standardErrorIn) {
        this.exitCode = exitCodeIn;
        this.standardOutput = standardOutputIn;
        this.standardError = standardErrorIn;
    }

    /**
     * Builds an instance with only the exit code
     * @param exitCodeIn process exit code
     */
    public ProcessOutput(int exitCodeIn) {
        this(exitCodeIn, null, null);
    }

    public int getExitCode() {
        return exitCode;
    }

    /**
     * Check if this process output has a non-empty standard output
     * @return true if the standard output is not null and not blank
     */
    public boolean hasStandardOutput() {
        return standardOutput != null && !standardOutput.isBlank();
    }

    public String getStandardOutput() {
        return standardOutput;
    }

    /**
     * Check if this process output has a non-empty standard error
     * @return true if the standard error is not null and not blank
     */
    public boolean hasStandardError() {
        return standardError != null && !standardError.isBlank();
    }

    public String getStandardError() {
        return standardError;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProcessOutput)) {
            return false;
        }
        ProcessOutput that = (ProcessOutput) o;
        return exitCode == that.exitCode && Objects.equals(standardOutput,
            that.standardOutput) && Objects.equals(standardError, that.standardError);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exitCode, standardOutput, standardError);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ProcessOutput.class.getSimpleName() + "[", "]")
            .add("exitCode=" + getExitCode())
            .add("standerOutput='" + getStandardOutput() + "'")
            .add("standardError='" + getStandardError() + "'")
            .toString();
    }
}
