package com.redhat.rhn.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.common.SatConfigFactory;

public class UserPasswordUtils {

    private UserPasswordUtils() {
    }

    private static final String PSW_LOWERCHAR_REGEX = "(?<lowercase>.*[a-z])?";
    private static final String PSW_UPPERCHAR_REGEX = "(?<uppercase>.*[A-Z])?";
    private static final String PSW_DIGIT_REGEX = "(?<digit>.*\\d)?";
    private static final String PSW_SPECIAL_CHAR_REGEX = "(?<specialChar>.*[%s])?";
    private static final String PSW_NO_SPACE_TAB_NEWLINE_REGEX = "(?<spaces>=.[\\t\\n\\s+$])";

    /**
     * @param errorsMap
     * @param password
     * @return
     */
    public static Map<String, String> validatePassword(Map<String, String> errorsMap, String password) {
        UserPasswordValidation userPasswordValidation = UserPasswordValidation.buildPasswordValidation(
                password, UserPasswordCheckSettings.buildUserPasswordCheckSettingsFromSatFactory()
        );
        return userPasswordValidation.validate(password);
    }

    @FunctionalInterface
    private interface IUserPasswordChecks {

        Optional<UserPasswordCheckFail> validate(Matcher matcher);

        static Optional<UserPasswordCheckFail> matchRegexFailOrEmpty(
                Matcher matcher, String groupName, String localizedMessage, String configParams
        ) {
            return matcher.group(groupName) != null ?
                    Optional.empty() :
                    Optional.of(new UserPasswordCheckFail(localizedMessage, configParams));
        }

    }

    private static class UserPasswordValidation {
        private final String regex;
        private final List<IUserPasswordChecks> checks;

        UserPasswordValidation(String regexIn, List<IUserPasswordChecks> checksIn) {
            regex = regexIn;
            checks = checksIn;
        }

        public String getRegex() {
            return regex;
        }

        public List<IUserPasswordChecks> getChecks() {
            return checks;
        }

        public static UserPasswordValidation buildPasswordValidation(String password, UserPasswordCheckSettings settings) {
            List<IUserPasswordChecks> checks = new ArrayList<>();
            StringBuilder regexBuilder = new StringBuilder("^");
            LocalizationService localizationService = LocalizationService.getInstance();
            // Check for uppercase letters

            if (settings.isUpperCharFlag()) {
                regexBuilder.append(PSW_UPPERCHAR_REGEX);
                checks.add(matcher -> IUserPasswordChecks.matchRegexFailOrEmpty(
                        matcher, "uppercase", localizationService.getMessage("error.uppercharpswd"), "")
                );
            }

            // Check for lowercase letters
            if (settings.isLowerCharFlag()) {
                regexBuilder.append(PSW_LOWERCHAR_REGEX);
                checks.add(matcher -> IUserPasswordChecks.matchRegexFailOrEmpty(
                        matcher, "lowercase", localizationService.getMessage("error.lowercasecharpswd"), "")
                );
            }

            // Check for digits
            if (settings.isDigitFlag()) {
                regexBuilder.append(PSW_DIGIT_REGEX);
                checks.add(matcher -> IUserPasswordChecks.matchRegexFailOrEmpty(
                        matcher, "digit", localizationService.getMessage("error.digitpswd"), "")
                );
            }

            // Check for special characters
            if (settings.isSpecialCharFlag()) {
                regexBuilder.append(String.format(PSW_SPECIAL_CHAR_REGEX, settings.getSpecialChars()));
                checks.add(matcher -> IUserPasswordChecks.matchRegexFailOrEmpty(
                        matcher, "specialChar", localizationService.getMessage("error.specialcharpswd"), "")
                );
            }

            // Always check for no spaces, tabs, or newlines
            regexBuilder.append(PSW_NO_SPACE_TAB_NEWLINE_REGEX);
            checks.add(matcher -> IUserPasswordChecks.matchRegexFailOrEmpty(
                    matcher, "spaces", localizationService.getMessage("error.spacespswd"), "")
            );

            // Always check for no spaces, tabs, or newlines
            regexBuilder.append(PSW_NO_SPACE_TAB_NEWLINE_REGEX);
            checks.add(matcher -> IUserPasswordChecks.matchRegexFailOrEmpty(
                    matcher, "spaces", localizationService.getMessage("error.spacespswd"), "")
            );

            // Check for consecutive characters
            if (settings.isConsecutiveCharsFlag()) {
                checks.add(_unused -> hasConsecutiveCharacters(password) ?
                        Optional.of(new UserPasswordCheckFail("error.consecutivecharspwd", "")) :
                        Optional.empty()
                );
            }

            // Check for maximum occurrences of any character
            if (settings.isRestrictedOccurrenceFlag()) {
                checks.add(_unused -> exceedsMaxOccurrences(password, settings.getMaxCharacterOccurrence()) ?
                        Optional.of(new UserPasswordCheckFail("error.maxoccurencepswd", "1")) :
                        Optional.empty()
                );
            }
            // Always check for length requirements
            checks.add(_unused -> password.length() < settings.minLength ?
                    Optional.of(new UserPasswordCheckFail("error.minlengthpswd", "1")) :
                    Optional.empty());
            checks.add(_unused -> password.length() > settings.maxLength ?
                    Optional.of(new UserPasswordCheckFail("error.maxlengthpswd", "1")) :
                    Optional.empty());

            regexBuilder.append("$");
            return new UserPasswordValidation(regexBuilder.toString(), checks);
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

        public Map<String, String> validate(String pswd) {
            Pattern pattern = Pattern.compile(this.getRegex());
            Matcher matcher = pattern.matcher(pswd);
            matcher.matches();
            return this.getChecks().stream()
                    .map(check -> check.validate(matcher))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toMap(
                            UserPasswordCheckFail::getLocalizedMessageId,
                            UserPasswordCheckFail::getConfigurationParameter)
                    );
        }
    }

    private static class UserPasswordCheckSettings {
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

        UserPasswordCheckSettings(boolean upperCharFlagIn, boolean lowerCharFlagIn, boolean digitFlagIn,
                                  boolean specialCharFlagIn, String specialCharsIn, boolean consecutiveCharsFlagIn,
                                  boolean restrictedOccurrenceFlagIn, int maxCharacterOccurrenceIn,
                                  int minLengthIn, int maxLengthIn) {
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
