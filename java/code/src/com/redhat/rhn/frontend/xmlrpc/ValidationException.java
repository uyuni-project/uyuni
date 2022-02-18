/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc;

import com.redhat.rhn.FaultException;


/**
 * ValidationException
 */
public class ValidationException extends FaultException {

    /**
     * Constructor
     * @param msg Message for exception, already localized by the validation
     * framework.
     */
    public ValidationException(String msg) {
        super(2800, "validationError", msg);
    }

    /**
     * Constructor
     * @param msg Message for exception, already localized by the validation
     * framework.
     * @param cause the cause (which is saved for later retrieval by the
     * Throwable.getCause() method). (A null value is permitted, and indicates
     * that the cause is nonexistent or unknown.)
     */
    public ValidationException(String msg, Throwable cause) {
        super(2800, "validationError", msg, cause);
    }

    /**
     * Constructor
     * @param cause the cause (must be non-null)
     */
    public ValidationException(Throwable cause) {
        this(cause.getMessage(), cause);
    }
}
