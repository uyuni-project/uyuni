/**
 * Copyright (c) 2009--2014 Red Hat, Inc.
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

/*
 * AUTOMATICALLY GENERATED FILE, DO NOT EDIT.
 */
package com.redhat.rhn.domain.rhnpackage;

import com.redhat.rhn.common.RhnRuntimeException;

/**
 * No Base Channel was found for a particular server.
 * <p>
 *
 *
 * @version definition($Rev: 76724 $)/template($Rev: 67725 $)
 */
public class MissingArchitectureException extends RhnRuntimeException {

    // ///////////////////////
    // Constructors
    // ///////////////////////
    /**
     * Constructor
     * @param message exception message
     */
    public MissingArchitectureException(String message) {
        super(message);
        // begin member variable initialization
    }

    /**
     * Constructor
     * @param message exception message
     * @param cause the cause (which is saved for later retrieval by the
     * Throwable.getCause() method). (A null value is permitted, and indicates
     * that the cause is nonexistent or unknown.)
     */
    public MissingArchitectureException(String message, Throwable cause) {
        super(message, cause);
        // begin member variable initialization
    }

    // ///////////////////////
    // Getters/Setters
    // ///////////////////////
}
