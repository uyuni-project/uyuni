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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.common.errors;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a Uyuni general exception
 */
public class UyuniGeneralException extends RuntimeException implements Serializable {
    private final List<UyuniError> errors;

    /**
     * Constructor with a list of errors
     * @param errorsIn the list of errors
     */
    public UyuniGeneralException(List<UyuniError> errorsIn) {
        super(String.join(". ", errorsIn.stream().map(UyuniError::getMessage).toList()));
        this.errors = errorsIn;
    }

    public List<UyuniError> getErrors() {
        return errors;
    }

    /**
     * Returns the category of the whole report, used by the entry points to choose the response:
     * the category of its errors if they all share one, UNKNOWN if any error is UNKNOWN,
     * INVALID_ARGUMENT for a mix of the other categories.
     * @return the category of the report
     */
    public UyuniErrorCategory getCategory() {
        Set<UyuniErrorCategory> categories = errors.stream().map(UyuniError::getCategory).collect(Collectors.toSet());
        if (categories.size() == 1) {
            return categories.iterator().next();
        }
        if (categories.isEmpty() || categories.contains(UyuniErrorCategory.UNKNOWN)) {
            return UyuniErrorCategory.UNKNOWN;
        }
        return UyuniErrorCategory.INVALID_ARGUMENT;
    }

    /**
     * Returns all error messages as a string array
     * @return String array of error messages
     */
    public String[] getErrorMessages() {
        return errors.stream().map(UyuniError::getMessage).toList().toArray(new String[0]);
    }

}
