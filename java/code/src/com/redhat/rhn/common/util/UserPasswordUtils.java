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
package com.redhat.rhn.common.util;

import static com.redhat.rhn.common.util.UserPasswordUtils.PasswordPolicy.buildPasswordPolicyFromSatFactory;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.common.SatConfigFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UserPasswordUtils {

    private UserPasswordUtils() {
    }

    private static final Pattern PSW_LOWERCHAR_REGEX = Pattern.compile("\\p{javaLowerCase}+");
    private static final Pattern PSW_UPPERCHAR_REGEX = Pattern.compile("\\p{javaUpperCase}+");
    private static final Pattern PSW_DIGIT_REGEX = Pattern.compile("\\p{javaDigit}+");
    private static final Pattern PSW_NO_SPACE_TAB_NEWLINE_REGEX = Pattern.compile("\\p{javaWhitespace}+");

    /**
     * Validate the password using the configurations from SatConfiguration
     * @param password the password to validate
     * @return the errors map
     */
    public static List<UserPasswordCheckFail> validatePasswordFromSatConfiguration(String password) {
        List<UserPasswordChecks> checks =  buildChecksFromPolicy(buildPasswordPolicyFromSatFactory());
        return executeValidation(password, checks);
    }

    /**
     * Validate the password using the settings class, useful for testing
     * @param password the password to validate
     * @param policy the password policy settings
     * @return the errors map
     */
    public static List<UserPasswordCheckFail> validatePasswordFromPolicy(String password,
                                                                 PasswordPolicy policy) {
        List<UserPasswordChecks> checks = buildChecksFromPolicy(policy);
        return executeValidation(password, checks);
    }

    private static List<UserPasswordChecks> buildChecksFromPolicy(PasswordPolicy policy) {
        List<UserPasswordChecks> checks = new ArrayList<>();
        // Check for uppercase letters
        if (policy.isUpperCharFlag()) {
            checks.add(UserPasswordUtils::upperCharCheck);
        }
        // Check for lowercase letters
        if (policy.isLowerCharFlag()) {
            checks.add(UserPasswordUtils::lowerCherCheck);
        }
        // Check for digits
        if (policy.isDigitFlag()) {
            checks.add(UserPasswordUtils::digitCharCheck);
        }
        // Check for consecutive characters
        if (policy.isConsecutiveCharsFlag()) {
            checks.add(UserPasswordUtils::consecutiveCharCheck);
        }
        // Check for maximum occurrences of any character
        if (policy.isRestrictedOccurrenceFlag()) {
            checks.add(password -> restrictedCharCheck(password, policy.getMaxCharacterOccurrence()));
        }
        // Check for special characters
        if (policy.isSpecialCharFlag()) {
            checks.add(password -> specialCharCheck(password, policy.getSpecialChars()));
        }
        // Always check for no spaces, tabs, or newlines
        checks.add(UserPasswordUtils::spaceCharCheck);
        // Always check for length requirements
        checks.add(password -> minLengthCheck(password, policy.getMinLength()));
        checks.add(password -> maxLengthCheck(password, policy.getMaxLength()));
        return checks;
    }

    private static Optional<UserPasswordCheckFail> lowerCherCheck(String password) {
        return PSW_LOWERCHAR_REGEX.matcher(password).find() ?
                Optional.empty() :
                Optional.of(new UserPasswordCheckFail("error.nolowercasepassword", ""));
    }

    private static Optional<UserPasswordCheckFail> digitCharCheck(String password) {
        return PSW_DIGIT_REGEX.matcher(password).find() ?
                Optional.empty() :
                Optional.of(new UserPasswordCheckFail("error.nodigitspassword", ""));
    }

    private static Optional<UserPasswordCheckFail> upperCharCheck(String password) {
        return PSW_UPPERCHAR_REGEX.matcher(password).find() ?
                Optional.empty() :
                Optional.of(new UserPasswordCheckFail("error.nouppercasepassword", ""));
    }

    private static Optional<UserPasswordCheckFail> spaceCharCheck(String password) {
        return !PSW_NO_SPACE_TAB_NEWLINE_REGEX.matcher(password).find() ?
                Optional.empty() :
                Optional.of(new UserPasswordCheckFail("error.spacesinpassword", ""));
    }

    private static Optional<UserPasswordCheckFail> consecutiveCharCheck(String password) {
        return !hasConsecutiveCharacters(password) ?
                Optional.empty() :
                Optional.of(new UserPasswordCheckFail("consecutive_characters_presents", ""));
    }

    private static Optional<UserPasswordCheckFail> restrictedCharCheck(String password, int maxOccurences) {
        return !exceedsMaxOccurrences(password, maxOccurences) ?
                Optional.empty() :
                Optional.of(
                        new UserPasswordCheckFail("error.occurrencecharacterspassword", String.valueOf(maxOccurences))
                );
    }

    private static Optional<UserPasswordCheckFail> minLengthCheck(String password, int minLength) {
        return password.length() < minLength ?
                Optional.of(new UserPasswordCheckFail("error.minpassword", String.valueOf(minLength))) :
                Optional.empty();
    }

    private static Optional<UserPasswordCheckFail> maxLengthCheck(String password, int maxLength) {
        return password.length() > maxLength ?
                Optional.of(new UserPasswordCheckFail("error.maxpassword", String.valueOf(maxLength))) :
                Optional.empty();
    }

    private static Optional<UserPasswordCheckFail> specialCharCheck(String password, String specialCharacters) {
        return hasSpecialCharacters(password, specialCharacters) ?
                Optional.empty() :
                Optional.of(new UserPasswordCheckFail("error.nospecialcharacterspassword", specialCharacters));
    }

    private static boolean hasConsecutiveCharacters(String password) {
        for (int i = 0; i < password.length() - 1; i++) {
            if (password.charAt(i) == password.charAt(i + 1)) {
                return true;
            }
        }
        return false;
    }

    private static boolean exceedsMaxOccurrences(String password, int maxOccurrences) {
        Map<Character, Integer> charCount = new HashMap<>();
        for (char c : password.toCharArray()) {
            charCount.put(c, charCount.getOrDefault(c, 0) + 1);
        }
        for (int count : charCount.values()) {
            if (count > maxOccurrences) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasSpecialCharacters(String password, String specialCharacters) {
        for (char c : specialCharacters.toCharArray()) {
            if (password.indexOf(c) != -1) {
                return true;
            }
        }
        return false;
    }

    private static List<UserPasswordCheckFail> executeValidation(String pswd, List<UserPasswordChecks> checks) {
        return checks.stream()
                .map(check -> check.validate(pswd))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @FunctionalInterface
    private interface UserPasswordChecks {
        Optional<UserPasswordCheckFail> validate(String password);
    }

    public static class PasswordPolicy {
        private final boolean upperCharFlag;
        private final boolean lowerCharFlag;
        private final boolean digitFlag;
        private final boolean specialCharFlag;
        private final String specialChars;
        private final boolean consecutiveCharsFlag;
        private final boolean restrictedOccurrenceFlag;
        private final int maxCharacterOccurrence;
        private final int minLength;
        private final int maxLength;

        /**
         * Class that holds the settings for the user password checks
         * @param upperCharFlagIn check upper char in password
         * @param lowerCharFlagIn check lower char in password
         * @param digitFlagIn check digit in password
         * @param specialCharFlagIn check special char in password
         * @param specialCharsIn string with special chars
         * @param consecutiveCharsFlagIn check consecutive characters
         * @param restrictedOccurrenceFlagIn check restricted occurrence
         * @param maxCharacterOccurrenceIn max number of occurrence
         * @param minLengthIn minimum password length
         * @param maxLengthIn maximum password length
         */
        public PasswordPolicy(
                boolean upperCharFlagIn, boolean lowerCharFlagIn,
                boolean digitFlagIn, boolean specialCharFlagIn,
                String specialCharsIn, boolean consecutiveCharsFlagIn,
                boolean restrictedOccurrenceFlagIn, int maxCharacterOccurrenceIn,
                int minLengthIn, int maxLengthIn
        ) {
            upperCharFlag = upperCharFlagIn;
            lowerCharFlag = lowerCharFlagIn;
            digitFlag = digitFlagIn;
            specialCharFlag = specialCharFlagIn;
            specialChars = specialCharsIn;
            consecutiveCharsFlag = consecutiveCharsFlagIn;
            restrictedOccurrenceFlag = restrictedOccurrenceFlagIn;
            maxCharacterOccurrence = maxCharacterOccurrenceIn;
            minLength = minLengthIn;
            maxLength = maxLengthIn;
        }

        public boolean isUpperCharFlag() {
            return upperCharFlag;
        }

        public boolean isLowerCharFlag() {
            return lowerCharFlag;
        }

        public boolean isDigitFlag() {
            return digitFlag;
        }

        public boolean isSpecialCharFlag() {
            return specialCharFlag;
        }

        public String getSpecialChars() {
            return specialChars;
        }

        public boolean isConsecutiveCharsFlag() {
            return consecutiveCharsFlag;
        }

        public boolean isRestrictedOccurrenceFlag() {
            return restrictedOccurrenceFlag;
        }

        public int getMaxCharacterOccurrence() {
            return maxCharacterOccurrence;
        }

        public int getMinLength() {
            return minLength;
        }

        public int getMaxLength() {
            return maxLength;
        }

        /**
         * Helper method to build checks settings from SatConfiguration
         * @return the settings for the user password settings
         */
        public static PasswordPolicy buildPasswordPolicyFromSatFactory() {
            return new PasswordPolicy(
                    SatConfigFactory.getSatConfigBooleanValue(SatConfigFactory.PSW_CHECK_UPPER_CHAR_FLAG),
                    SatConfigFactory.getSatConfigBooleanValue(SatConfigFactory.PSW_CHECK_LOWER_CHAR_FLAG),
                    SatConfigFactory.getSatConfigBooleanValue(SatConfigFactory.PSW_CHECK_DIGIT_FLAG),
                    SatConfigFactory.getSatConfigBooleanValue(SatConfigFactory.PSW_CHECK_SPECIAL_CHAR_FLAG),
                    SatConfigFactory.getSatConfigValue(SatConfigFactory.PSW_CHECK_SPECIAL_CHARACTERS),
                    SatConfigFactory.getSatConfigBooleanValue(SatConfigFactory.PSW_CHECK_CONSECUTIVE_CHAR_FLAG),
                    SatConfigFactory.getSatConfigBooleanValue(SatConfigFactory.PSW_CHECK_RESTRICTED_OCCURENCE_FLAG),
                    SatConfigFactory.getSatConfigIntValue(SatConfigFactory.PSW_CHECK_MAX_OCCURENCE, 2),
                    SatConfigFactory.getSatConfigIntValue(SatConfigFactory.PSW_CHECK_LENGHT_MIN, 8),
                    SatConfigFactory.getSatConfigIntValue(SatConfigFactory.PSW_CHECK_LENGHT_MAX, 32)
            );
        }
    }

    public static class UserPasswordCheckFail {
        private final String localizedMessageId;
        private final String configurationParameter;

        protected UserPasswordCheckFail(String localizedMessageIdIn, String configurationParameterIn) {
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

    /**
     * Exception class for password validation, extends IllegalArgumentException for compatibility with older code
     */
    public static class PasswordValidationException extends IllegalArgumentException {
        private final List<UserPasswordCheckFail> validationErrors;

        /**
         * Exception class for password validation
         * @param validationErrorsIn a list of validation errors
         */
        public PasswordValidationException(List<UserPasswordCheckFail> validationErrorsIn) {
            validationErrors = validationErrorsIn;
        }

        public List<UserPasswordCheckFail> getValidationErrors() {
            return validationErrors;
        }

        @Override
        public String getMessage() {
            return "Password validation errors: " + getValidationErrors().stream()
                    .map(UserPasswordCheckFail::getLocalizedMessageId)
                    .collect(Collectors.joining("; "));
        }
    }

}

