/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.frontend.xmlrpc;

import com.redhat.rhn.FaultException;

/**
 * Exception to be thrown in case of problems with content synchronization.
 */
public class ContentSyncException extends FaultException {

    private static final int ERROR_CODE = 10104;
    private static final String ERROR_LABEL = "contentSyncError";

    /**
     * Constructor expecting a custom cause.
     * @param cause the cause
     */
    public ContentSyncException(Throwable cause) {
        super(ERROR_CODE, ERROR_LABEL, cause.getMessage(), cause);
    }

    /**
     * Constructor expecting a custom message.
     * @param message the message
     */
    public ContentSyncException(String message) {
        super(ERROR_CODE, ERROR_LABEL, message);
    }
}
