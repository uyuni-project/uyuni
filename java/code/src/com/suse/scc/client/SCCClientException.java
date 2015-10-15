/**
 * Copyright (c) 2014--2015 SUSE LLC
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
package com.suse.scc.client;

/**
 * Exception to be thrown in case of problems with SCC.
 */
public class SCCClientException extends Exception {

    /**
     * Constructor expecting a custom cause.
     * @param cause the cause
     */
    public SCCClientException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor expecting a custom message.
     * @param message the message
     */
    public SCCClientException(String message) {
        super(message);
    }
}
