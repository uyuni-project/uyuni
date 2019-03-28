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

import java.util.regex.Pattern;

/**
 * Utility class for validations
 */
public class ValidationUtils {
    private static final String PROJECT_LABEL_REGEX =
            "^[a-z\\d][a-z\\d\\-\\.\\_]*$";

    private ValidationUtils() { }

    /**
     * validate label pattern
     * @param label label to validate
     * @return true if label is valid
     */
    public static Boolean isLabelValid(String label) {
        return Pattern.compile(PROJECT_LABEL_REGEX).matcher(label).find();
    }

}
