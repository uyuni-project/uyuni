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

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.validator.ValidatorError;

public class PasswordPolicyCheckFail {
    private final String localizedMessageId;
    private final String configurationParameter;

    protected PasswordPolicyCheckFail(String localizedMessageIdIn, String configurationParameterIn) {
        localizedMessageId = localizedMessageIdIn;
        configurationParameter = configurationParameterIn;
    }

    public String getLocalizedMessageId() {
        return localizedMessageId;
    }

    public String getConfigurationParameter() {
        return configurationParameter;
    }

    public String getLocalizedErrorMessage() {
        return LocalizationService.getInstance().getMessage(getLocalizedMessageId(), getConfigurationParameter());
    }

    /**
     * Helper method for converting to ValidatorError for UserCommands
     * @return the validator error
     */
    public ValidatorError toValidatorError() {
        return new ValidatorError(getLocalizedMessageId(), getConfigurationParameter());
    }

}
