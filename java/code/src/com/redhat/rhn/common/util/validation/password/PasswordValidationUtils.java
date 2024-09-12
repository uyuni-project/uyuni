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
package com.redhat.rhn.common.util.validation.password;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PasswordValidationUtils {

    private PasswordValidationUtils() {
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
    public static List<PasswordPolicyCheckFail> validatePasswordFromSatConfiguration(String password) {
        List<PasswordPolicyCheck> checks =  buildChecksFromPolicy(PasswordPolicy.buildPasswordPolicyFromSatFactory());
        return executeValidation(password, checks);
    }

    /**
     * Validate the password using the settings class, useful for testing
     * @param password the password to validate
     * @param policy the password policy settings
     * @return the errors map
     */
    public static List<PasswordPolicyCheckFail> validatePasswordFromPolicy(String password,
                                                                 PasswordPolicy policy) {
        List<PasswordPolicyCheck> checks = buildChecksFromPolicy(policy);
        return executeValidation(password, checks);
    }

    private static List<PasswordPolicyCheck> buildChecksFromPolicy(PasswordPolicy policy) {
        List<PasswordPolicyCheck> checks = new ArrayList<>();
        // Check for uppercase letters
        if (policy.isUpperCharFlag()) {
            checks.add(PasswordValidationUtils::upperCharCheck);
        }
        // Check for lowercase letters
        if (policy.isLowerCharFlag()) {
            checks.add(PasswordValidationUtils::lowerCherCheck);
        }
        // Check for digits
        if (policy.isDigitFlag()) {
            checks.add(PasswordValidationUtils::digitCharCheck);
        }
        // Check for consecutive characters
        if (policy.isConsecutiveCharsFlag()) {
            checks.add(PasswordValidationUtils::consecutiveCharCheck);
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
        checks.add(PasswordValidationUtils::spaceCharCheck);
        // Always check for length requirements
        checks.add(password -> minLengthCheck(password, policy.getMinLength()));
        checks.add(password -> maxLengthCheck(password, policy.getMaxLength()));
        return checks;
    }

    protected static Optional<PasswordPolicyCheckFail> lowerCherCheck(String password) {
        return PSW_LOWERCHAR_REGEX.matcher(password).find() ?
                Optional.empty() :
                Optional.of(new PasswordPolicyCheckFail("error.nolowercasepassword", ""));
    }

    protected static Optional<PasswordPolicyCheckFail> digitCharCheck(String password) {
        return PSW_DIGIT_REGEX.matcher(password).find() ?
                Optional.empty() :
                Optional.of(new PasswordPolicyCheckFail("error.nodigitspassword", ""));
    }

    protected static Optional<PasswordPolicyCheckFail> upperCharCheck(String password) {
        return PSW_UPPERCHAR_REGEX.matcher(password).find() ?
                Optional.empty() :
                Optional.of(new PasswordPolicyCheckFail("error.nouppercasepassword", ""));
    }

    protected static Optional<PasswordPolicyCheckFail> spaceCharCheck(String password) {
        return !PSW_NO_SPACE_TAB_NEWLINE_REGEX.matcher(password).find() ?
                Optional.empty() :
                Optional.of(new PasswordPolicyCheckFail("error.spacesinpassword", ""));
    }

    protected static Optional<PasswordPolicyCheckFail> consecutiveCharCheck(String password) {
        return !hasConsecutiveCharacters(password) ?
                Optional.empty() :
                Optional.of(new PasswordPolicyCheckFail("consecutive_characters_presents", ""));
    }

    protected static Optional<PasswordPolicyCheckFail> restrictedCharCheck(String password, int maxOccurences) {
        return !exceedsMaxOccurrences(password, maxOccurences) ?
                Optional.empty() :
                Optional.of(
                        new PasswordPolicyCheckFail("error.occurrencecharacterspassword", String.valueOf(maxOccurences))
                );
    }

    protected static Optional<PasswordPolicyCheckFail> minLengthCheck(String password, int minLength) {
        return password.length() < minLength ?
                Optional.of(new PasswordPolicyCheckFail("error.minpassword", String.valueOf(minLength))) :
                Optional.empty();
    }

    protected static Optional<PasswordPolicyCheckFail> maxLengthCheck(String password, int maxLength) {
        return password.length() > maxLength ?
                Optional.of(new PasswordPolicyCheckFail("error.maxpassword", String.valueOf(maxLength))) :
                Optional.empty();
    }

    protected static Optional<PasswordPolicyCheckFail> specialCharCheck(String password, String specialCharacters) {
        return hasSpecialCharacters(password, specialCharacters) ?
                Optional.empty() :
                Optional.of(new PasswordPolicyCheckFail("error.nospecialcharacterspassword", specialCharacters));
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

    private static List<PasswordPolicyCheckFail> executeValidation(String pswd, List<PasswordPolicyCheck> checks) {
        return checks.stream()
                .map(check -> check.validate(pswd))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    @FunctionalInterface
    private interface PasswordPolicyCheck {
        Optional<PasswordPolicyCheckFail> validate(String password);
    }

}

