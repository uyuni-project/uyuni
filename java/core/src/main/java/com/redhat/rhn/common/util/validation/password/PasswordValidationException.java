/*
 * Copyright (c) 2024 SUSE LLC
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

package com.redhat.rhn.common.util.validation.password;

import java.util.List;
import java.util.stream.Collectors;

public class PasswordValidationException extends IllegalArgumentException {
    private final transient List<PasswordPolicyCheckFail> validationErrors;

    /**
     * Exception class for password validation
     * @param validationErrorsIn a list of validation errors
     */
    public PasswordValidationException(List<PasswordPolicyCheckFail> validationErrorsIn) {
        validationErrors = validationErrorsIn;
    }

    public List<PasswordPolicyCheckFail> getValidationErrors() {
        return validationErrors;
    }

    @Override
    public String getMessage() {
        return "Password validation errors: " + getValidationErrors().stream()
                .map(PasswordPolicyCheckFail::getLocalizedMessageId)
                .collect(Collectors.joining("; "));
    }
}
