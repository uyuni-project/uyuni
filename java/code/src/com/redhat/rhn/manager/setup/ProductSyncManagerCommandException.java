/**
 * Copyright (c) 2014 SUSE
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
package com.redhat.rhn.manager.setup;

/**
 * Represents an error while working with SUSE products.
 */
public class ProductSyncManagerCommandException extends Exception {

    /** The error code. */
    private int errorCode;

    /** The command output. */
    private String commandOutput;

    /** The command error message. */
    private String commandErrorMessage;

    /**
     * Message constructor.
     * @param messageIn descriptive exception message
     * @param errorCodeIn the error code in
     * @param commandOutputIn the command output in
     * @param commandErrorMessageIn the command error message in
     */
    public ProductSyncManagerCommandException(String messageIn, int errorCodeIn,
            String commandOutputIn, String commandErrorMessageIn) {
        super(messageIn);
        errorCode = errorCodeIn;
        commandOutput = commandOutputIn;
        commandErrorMessage = commandErrorMessageIn;
    }

    /**
     * Gets the error code.
     * @return the error code
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Gets the command output.
     * @return the command output
     */
    public String getCommandOutput() {
        return commandOutput;
    }

    /**
     * Gets the command error message.
     * @return the command error message
     */
    public String getCommandErrorMessage() {
        return commandErrorMessage;
    }
}
