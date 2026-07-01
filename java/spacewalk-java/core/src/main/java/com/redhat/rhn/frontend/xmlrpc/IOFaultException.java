/*
 * Copyright (c) 2016--2025 SUSE LLC
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

import java.io.IOException;

/**
 * Wrapper for IOException.
 */
public class IOFaultException extends FaultException {

    /**
     * Standard constructor
     *
     * @param causeIn the cause
     */
    public IOFaultException(IOException causeIn) {
        super(3601, "IOExcepion", causeIn.getMessage(), causeIn);
    }

    /**
     * Standard constructor
     *
     * @param messageIn the message
     */
    public IOFaultException(String messageIn) {
        super(3601, "IOExcepion", messageIn, (Throwable) null);
    }
}
