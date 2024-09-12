package com.redhat.rhn.common.util.validation.password;

import java.util.List;
import java.util.stream.Collectors;

public class PasswordValidationException extends IllegalArgumentException {
    private final List<PasswordPolicyCheckFail> validationErrors;

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
