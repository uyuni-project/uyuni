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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.manager.webui.controllers.admin.service;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.util.validation.password.PasswordPolicy;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.domain.common.RhnConfiguration;
import com.redhat.rhn.domain.common.RhnConfigurationFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PasswordPolicyService {

    /**
     * Public explicit constructor for password policy service
     * enforces composition pattern
     */
    public PasswordPolicyService() { }

    /**
     * Validate the password policy
     * @param passwordPolicyIn the password policy
     * @throws ValidatorException exception when validating the policy
     */
    public void validatePasswordPolicy(PasswordPolicy passwordPolicyIn) throws ValidatorException {
        ValidatorResult result = new ValidatorResult();
        List<PasswordPolicyModelValidation> checks = buildChecks();
        executeValidation(checks, passwordPolicyIn).forEach(result::addError);
        if (result.hasErrors()) {
            throw new ValidatorException(result);
        }
    }

    /**
     * Stores the password policy in the database
     * @param passwordPolicyIn the password policy
     */
    public void savePasswordPolicy(PasswordPolicy passwordPolicyIn) {
        RhnConfigurationFactory factory = RhnConfigurationFactory.getSingleton();
        List<RhnConfiguration> configs = new ArrayList<>();
        // Build configurations using the buildConfiguration method and add them to the configs list
        configs.add(buildConfiguration(RhnConfiguration.KEYS.psw_check_length_min, passwordPolicyIn.getMinLength()));
        configs.add(buildConfiguration(RhnConfiguration.KEYS.psw_check_length_max, passwordPolicyIn.getMaxLength()));
        configs.add(buildConfiguration(RhnConfiguration.KEYS.psw_check_digit_flag, passwordPolicyIn.isDigitFlag()));
        configs.add(buildConfiguration(RhnConfiguration.KEYS.psw_check_lower_char_flag,
                passwordPolicyIn.isLowerCharFlag()));
        configs.add(buildConfiguration(RhnConfiguration.KEYS.psw_check_upper_char_flag,
                passwordPolicyIn.isUpperCharFlag()));
        configs.add(buildConfiguration(RhnConfiguration.KEYS.psw_check_consecutive_char_flag,
                passwordPolicyIn.isConsecutiveCharsFlag()));
        configs.add(buildConfiguration(RhnConfiguration.KEYS.psw_check_special_char_flag,
                passwordPolicyIn.isSpecialCharFlag()));
        configs.add(buildConfiguration(RhnConfiguration.KEYS.psw_check_restricted_occurrence_flag,
                passwordPolicyIn.isRestrictedOccurrenceFlag()));
        configs.add(buildConfiguration(RhnConfiguration.KEYS.psw_check_max_occurrence,
                passwordPolicyIn.getMaxCharacterOccurrence()));
        configs.add(buildConfiguration(RhnConfiguration.KEYS.psw_check_special_characters,
                passwordPolicyIn.getSpecialChars()));
        // Perform bulk update
        factory.bulkUpdate(configs);
    }

    private List<PasswordPolicyModelValidation> buildChecks() {
        List<PasswordPolicyModelValidation> checks = new ArrayList<>();
        checks.add(policyIn -> policyIn.getMinLength() <= 0 ?
                Optional.of(new PasswordPolicyModelValidationFail(
                        "error.field_equal_or_less_than_zero", new String[]{"Min Password Length"})
                ) :
                Optional.empty()
        );
        checks.add(policyIn -> policyIn.getMaxLength() <= 0 ?
                Optional.of(new PasswordPolicyModelValidationFail(
                        "error.field_equal_or_less_than_zero", new String[]{"Max Password Length"})
                ) :
                Optional.empty()
        );
        checks.add(policyIn -> policyIn.getMinLength() >= policyIn.getMaxLength() ?
                Optional.of(new PasswordPolicyModelValidationFail(
                        "error.illegal_min_max_fields",
                        new String[]{"Min Password Length", "Max Password Length"})
                ) :
                Optional.empty()
        );
        checks.add(policyIn -> policyIn.getMaxCharacterOccurrence() <= 0 ?
                Optional.of(new PasswordPolicyModelValidationFail(
                        "error.field_equal_or_less_than_zero",
                        new String[]{"Max Characters Occurrences"})
                ) :
                Optional.empty()
        );
        return checks;
    }

    private List<ValidatorError> executeValidation(List<PasswordPolicyModelValidation> checks,
                                                   PasswordPolicy policyIn) {
        return checks.stream()
                .map(check -> check.validate(policyIn))
                .flatMap(Optional::stream)
                .map(PasswordPolicyModelValidationFail::toValidatorError)
                .collect(Collectors.toList());
    }

    @FunctionalInterface
    private interface PasswordPolicyModelValidation {
        Optional<PasswordPolicyModelValidationFail> validate(PasswordPolicy policyIn);
    }

    private RhnConfiguration buildConfiguration(RhnConfiguration.KEYS keysIn, Object value) {
        RhnConfiguration configuration = new RhnConfiguration();
        configuration.setKey(keysIn);
        configuration.setValue(String.valueOf(value));
        return configuration;
    }

    private static class PasswordPolicyModelValidationFail {
        private final String localizedMessageId;
        private final String[] configurationParameter;

        private PasswordPolicyModelValidationFail(String localizedMessageIdIn, String[] configurationParameterIn) {
            localizedMessageId = localizedMessageIdIn;
            configurationParameter = configurationParameterIn;
        }

        public String getLocalizedMessageId() {
            return localizedMessageId;
        }

        public String[] getConfigurationParameter() {
            return configurationParameter;
        }

        public String getLocalizedErrorMessage() {
            return LocalizationService.getInstance()
                    .getMessage(getLocalizedMessageId(), (Object[])getConfigurationParameter());
        }

        /**
         * Helper method for converting to ValidatorError
         *
         * @return the validator error
         */
        public ValidatorError toValidatorError() {
            return new ValidatorError(getLocalizedMessageId(), (Object[])getConfigurationParameter());
        }
    }

}
