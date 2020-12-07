/**
 * Copyright (c) 2019 SUSE LLC
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
package com.suse.manager.webui.controllers.contentmanagement.handlers;

import com.redhat.rhn.common.validator.ValidatorException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for validations
 */
public class ValidationUtils {

    private ValidationUtils() { }

    /**
     * Extract validation errors from {@link ValidatorException} and convert them into localized messages.
     * @param exc the {@link ValidatorException}
     * @return the list of localized messages
     */
    public static List<String> convertValidationErrors(ValidatorException exc) {
        return exc.getResult().getErrors().stream()
                .map(e -> e.getLocalizedMessage())
                .collect(Collectors.toList());
    }
}
