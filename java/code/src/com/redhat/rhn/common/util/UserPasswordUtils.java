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

import static com.redhat.rhn.common.util.UserPasswordUtils.UserPasswordCheckSettings.buildUserPasswordCheckSettingsFromSatFactory;

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
    private static final Pattern PSW_DIGIT_REGEX = Pattern.compile("\\d+");
    private static final Pattern PSW_NO_SPACE_TAB_NEWLINE_REGEX = Pattern.compile("\\s+");

    /**
     * Validate the password using the configurations from SatConfiguration
     * @param password the password to validate
     * @return the errors map
     */
    public static Map<String, String> validatePasswordFromSatConfiguration(String password) {
        List<IUserPasswordChecks> checks =  buildChecksFromSettings(buildUserPasswordCheckSettingsFromSatFactory());
        return executeValidation(password, checks);
    }

    /**
     * Validate the password using the settings class, useful for testing
     * @param password the password to validate
     * @param settings the settings
     * @return the errors map
     */
    public static Map<String, String> validatePasswordFromSettings(String password,
                                                                   UserPasswordCheckSettings settings) {
        List<IUserPasswordChecks> checks = buildChecksFromSettings(settings);
        return executeValidation(password, checks);
    }

    private static List<IUserPasswordChecks> buildChecksFromSettings(UserPasswordCheckSettings settings) {
        List<IUserPasswordChecks> checks = new ArrayList<>();
        // Check for uppercase letters
        if (settings.isUpperCharFlag()) {
            checks.add(password -> PSW_UPPERCHAR_REGEX.matcher(password).find() ?
                    Optional.empty() :
                    Optional.of(new UserPasswordCheckFail("no_uppercase", ""))
            );
        }
        // Check for lowercase letters
        if (settings.isLowerCharFlag()) {
            checks.add(password -> PSW_LOWERCHAR_REGEX.matcher(password).find() ?
                    Optional.empty() :
                    Optional.of(new UserPasswordCheckFail("no_lowercase", ""))
            );
        }
        // Check for digits
        if (settings.isDigitFlag()) {
            checks.add(password -> PSW_DIGIT_REGEX.matcher(password).find() ?
                    Optional.empty() :
                    Optional.of(new UserPasswordCheckFail("no_digit", ""))
            );
        }
        // Check for consecutive characters
        if (settings.isConsecutiveCharsFlag()) {
            checks.add(password -> !hasConsecutiveCharacters(password) ?
                    Optional.empty() :
                    Optional.of(new UserPasswordCheckFail("consecutive_characters_presents", ""))
            );
        }
        // Check for maximum occurrences of any character
        if (settings.isRestrictedOccurrenceFlag()) {
            checks.add(password -> !exceedsMaxOccurrences(password, settings.getMaxCharacterOccurrence()) ?
                    Optional.empty() :
                    Optional.of(new UserPasswordCheckFail(
                            "max_occurrences",
                            String.valueOf(settings.getMaxCharacterOccurrence())
                            )
                    )
            );
        }
        // Check for special characters
        if (settings.isSpecialCharFlag()) {
            checks.add(password -> hasSpecialCharacters(password, settings.getSpecialChars()) ?
                    Optional.empty() :
                    Optional.of(new UserPasswordCheckFail("special_characters", "1"))
            );
        }
        // Always check for no spaces, tabs, or newlines
        checks.add(password -> !PSW_NO_SPACE_TAB_NEWLINE_REGEX.matcher(password).find() ?
                Optional.empty() :
                Optional.of(new UserPasswordCheckFail("space_present", ""))
        );
        // Always check for length requirements
        checks.add(password -> password.length() < settings.minLength ?
                Optional.of(new UserPasswordCheckFail("min_lenght", "1")) :
                Optional.empty());
        checks.add(password -> password.length() > settings.maxLength ?
                Optional.of(new UserPasswordCheckFail("max_lenght", "1")) :
                Optional.empty());
        return checks;
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

    private static Map<String, String> executeValidation(String pswd, List<IUserPasswordChecks> checks) {
        return checks.stream()
                .map(check -> check.validate(pswd))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(
                        UserPasswordCheckFail::getLocalizedMessageId,
                        UserPasswordCheckFail::getConfigurationParameter)
                );
    }

    @FunctionalInterface
    private interface IUserPasswordChecks {
        Optional<UserPasswordCheckFail> validate(String password);
    }

    public static class UserPasswordCheckSettings {
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
        public UserPasswordCheckSettings(
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
        public static UserPasswordCheckSettings buildUserPasswordCheckSettingsFromSatFactory() {
            return new UserPasswordCheckSettings(
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

    private static class UserPasswordCheckFail {
        private final String localizedMessageId;
        private final String configurationParameter;

        private UserPasswordCheckFail(String localizedMessageIdIn, String configurationParameterIn) {
            localizedMessageId = localizedMessageIdIn;
            configurationParameter = configurationParameterIn;
        }

        public String getLocalizedMessageId() {
            return localizedMessageId;
        }

        public String getConfigurationParameter() {
            return configurationParameter;
        }
    }

}

