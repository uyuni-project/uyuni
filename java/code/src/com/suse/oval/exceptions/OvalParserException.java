/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.oval.exceptions;

/**
 * A runtime exception throne by {@link com.suse.oval.OvalParser} when it encounters errors while parsing an OVAL file
 * */
public class OvalParserException extends RuntimeException {

    /**
     * Constructs a new parser exception with null as its detail message. The cause is not initialized, and may
     * subsequently be initialized by a call to initCause.
     * */
    public OvalParserException() {
        super();
    }

    /**
     * Constructs a new parser exception with the specified detail message.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the getMessage() method.
     * */

    public OvalParserException(String message) {
        super(message);
    }

    /**
     * Constructs a new parser exception with the specified cause.
     *
     * @param cause the cause (which is saved for later retrieval by the getCause() method).
     * */
    public OvalParserException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new parser exception with the specified detail message and cause.
     *
     * @param cause the cause (which is saved for later retrieval by the getCause() method).
     * @param message the detail message. The detail message is saved for later retrieval by the getMessage() method.
     * */
    public OvalParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
