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
package com.suse.manager.xmlrpc;

import com.redhat.rhn.FaultException;

/**
 * no such history event
 */
public class NoSuchHistoryEventException extends FaultException {

    private static final long serialVersionUID = 2027804516811995016L;

    /**
     * Constructor
     */
    public NoSuchHistoryEventException() {
        this("No such history event for server");
    }

    /**
     * Constructor
     * @param cause the cause (which is saved for later retrieval by the
     * Throwable.getCause() method). (A null value is permitted, and indicates
     * that the cause is nonexistent or unknown.)
     */
    public NoSuchHistoryEventException(Throwable cause) {
        super(-216, "noSuchHistoryEvent", "No such history event for server", cause);
    }

    /**
     * Constructor
     * @param message exception message
     */
    public NoSuchHistoryEventException(String message) {
        super(-216, "noSuchHistoryEvent", message);
    }
}
