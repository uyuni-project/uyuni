/*
 * Copyright (c) 2025 SUSE LLC
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

package com.redhat.rhn.common;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a RHN general exception
 */
public class RhnGeneralException extends RuntimeException implements Serializable {
    private final List<RhnError> errors;

    /**
     * Constructor with a list of errors
     * @param errorsIn the list of errors
     */
    public RhnGeneralException(List<RhnError> errorsIn) {
        this.errors = errorsIn;
    }

    public List<RhnError> getErrors() {
        return errors;
    }

    /**
     * Returns all error messages as a string array
     * @return String array of error messages
     */
    public String[] getErrorMessages() {
        return errors.stream().map(RhnError::getMessage).toList().toArray(new String[0]);
    }

}
