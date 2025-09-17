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

/*
 * AUTOMATICALLY GENERATED FILE, DO NOT EDIT.
 */
package com.redhat.rhn.frontend.xmlrpc;

import com.redhat.rhn.FaultException;

/**
 * some systems were not deleted
 * <p>

 *
 */
public class UnrecognizedCountryException extends FaultException  {


     /**
     * Constructor
     * @param message the message
     */
    public UnrecognizedCountryException(String message) {
        super(2280 , "unrecognizedCountryException" ,  "The specified country code '" +
                message + "' is not recognized.  Countries are specified using their " +
                   "two letter abbreviations.");

    }

    /**
     * Constructor
     * @param message the message
     * @param cause the cause (which is saved for later retrieval
     * by the Throwable.getCause() method). (A null value is
     * permitted, and indicates that the cause is nonexistent or
     * unknown.)
     */
    public UnrecognizedCountryException(String message ,   Throwable cause) {
        super(2280 , "systemsNotDeleted" ,  message, cause);

    }

}
