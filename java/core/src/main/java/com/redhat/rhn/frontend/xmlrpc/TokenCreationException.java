/*
 * Copyright (c) 2016--2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.redhat.rhn.frontend.xmlrpc;

import com.redhat.rhn.FaultException;

/**
 * Token creation failed.
 *
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
