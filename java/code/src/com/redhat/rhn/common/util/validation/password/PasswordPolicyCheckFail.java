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
