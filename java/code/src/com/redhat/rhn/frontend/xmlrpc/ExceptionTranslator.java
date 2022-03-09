/*
 * Copyright (c) 2020 SUSE LLC
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
import com.redhat.rhn.manager.errata.RetractedErrataException;

import com.suse.manager.maintenance.NotInMaintenanceModeException;

import java.nio.channels.OverlappingFileLockException;

import redstone.xmlrpc.XmlRpcFault;

/**
 * Translation between backend exceptions and XMLRPC faults.
 */
public class ExceptionTranslator {

    // forbid instantiation
    private ExceptionTranslator() { }

    /**
     * Check given exception and re-return appropriate {@link FaultException}.
     *
     * @param t the exception
     * @return XmlRpcFault the {@link FaultException}
     */
    public static XmlRpcFault translateException(Throwable t) {
        // Generic fault exception
        if (t instanceof FaultException) {
            FaultException fe = (FaultException) t;
            return new XmlRpcFault(fe.getErrorCode(), fe.getMessage());
        }

        if (t instanceof OverlappingFileLockException) {
            return new XmlRpcFault(-1, "Operation already running. Please try later again.");
        }

        if (t instanceof NotInMaintenanceModeException) {
            return new XmlRpcFault(11334, t.getMessage());
        }

        if (t instanceof RetractedErrataException) {
            return new XmlRpcFault(2602, t.getMessage());
        }

        // For any other unhandled exception, we still need to send something to the client.
        // If we can get the cause of the exception, then display the message
        if (t != null) {
            return new XmlRpcFault(-1, "unhandled internal exception: " + t.getLocalizedMessage());
        }

        // Otherwise, return the generic unhandled internal exception
        return new XmlRpcFault(-1, "unhandled internal exception");
    }
}
