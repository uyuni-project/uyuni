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

/**
 * Fault exception for content management validation errors
 */
public class ContentValidationFaultException extends FaultException {

    private static final int ERROR_CODE = 10103;
    private static final String ERROR_LABEL = "invalidContentProject";

    /**
     * Standard constructor
     *
     * @param message validation message
     */
    public ContentValidationFaultException(String message) {
        super(ERROR_CODE, ERROR_LABEL, message);
    }
}
