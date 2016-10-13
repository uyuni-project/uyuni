/**
 * Copyright (c) 2016 SUSE LLC
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
package com.redhat.rhn.frontend.xmlrpc.activationkey;

import com.redhat.rhn.FaultException;

/**
 * Token creation failed.
 *
 * @version $Rev$
 */
public class TokenCreationException extends FaultException {

    /**
     * Constructor
     */
    public TokenCreationException() {
        super(10000 , "tokenCreation" , "token_creation");
    }

    /**
     * Constructor
     *
     * @param message exception message
     */
    public TokenCreationException(String message) {
        super(10000 , "tokenCreation" , message);
    }

    /**
     * Constructor
     * @param cause the cause (which is saved for later retrieval
     * by the Throwable.getCause() method). (A null value is
     * permitted, and indicates that the cause is nonexistent or
     * unknown.)
     */
    public TokenCreationException(Throwable cause) {
        super(10000 , "tokenCreation" , "token_creation", cause);
    }

}
