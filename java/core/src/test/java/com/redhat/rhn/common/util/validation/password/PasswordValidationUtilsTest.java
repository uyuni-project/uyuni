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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class PasswordValidationUtilsTest {

    private PasswordPolicy strictPolicy;

    @BeforeEach
    public void setUp() {
        strictPolicy = new PasswordPolicy(
                true,       // upperCharFlagIn
                true,       // lowerCharFlagIn
                true,       // digitFlagIn
                true,       // specialCharFlagIn
                "!@#$%",    // specialCharsIn
                true,       // consecutiveCharsFlagIn
                true,       // restrictedOccurrenceFlagIn
                2,          // maxCharacterOccurrenceIn
                8,          // minLengthIn
                16          // maxLengthIn
        );
    }

    @Test
    public void testValidPassword() {
        String password = "Ab1!Ab1!";
        List<PasswordPolicyCheckFail> errors =
                PasswordValidationUtils.validatePasswordFromPolicy(password, strictPolicy);
        assertTrue(errors.isEmpty(), "Password should be valid");
    }

    @Test
    public void testPasswordTooShort() {
        String password = "A1a!";
        List<PasswordPolicyCheckFail> errors =
                PasswordValidationUtils.validatePasswordFromPolicy(password, strictPolicy);
        assertFalse(errors.isEmpty(), "Password should be too short");
        assertTrue(errors.stream().anyMatch(e -> e.getLocalizedMessageId().contains("error.minpassword")));
    }

    @Test
    public void testPasswordTooLong() {
        String password = "A1!aA1!aA1!aA1!aA1!";
        List<PasswordPolicyCheckFail> errors =
                PasswordValidationUtils.validatePasswordFromPolicy(password, strictPolicy);
        assertFalse(errors.isEmpty(), "Password should be too long");
        assertTrue(errors.stream().anyMatch(e -> e.getLocalizedMessageId().contains("error.maxpassword")));
    }

    @Test
    public void testPasswordMissingUppercase() {
        String password = "ab1!ab1!";
        List<PasswordPolicyCheckFail> errors =
                PasswordValidationUtils.validatePasswordFromPolicy(password, strictPolicy);
        assertFalse(errors.isEmpty(), "Password should require uppercase letters");
        assertTrue(errors.stream().anyMatch(e -> e.getLocalizedMessageId().contains("error.nouppercasepassword")));
    }

    @Test
    public void testPasswordMissingLowercase() {
        String password = "AB1!AB1!";
        List<PasswordPolicyCheckFail> errors =
                PasswordValidationUtils.validatePasswordFromPolicy(password, strictPolicy);
        assertFalse(errors.isEmpty(), "Password should require lowercase letters");
        assertTrue(errors.stream().anyMatch(e -> e.getLocalizedMessageId().contains("error.nolowercasepassword")));
    }

    @Test
    public void testPasswordMissingDigit() {
        String password = "Abc!Abc!";
        List<PasswordPolicyCheckFail> errors =
                PasswordValidationUtils.validatePasswordFromPolicy(password, strictPolicy);
        assertFalse(errors.isEmpty(), "Password should require digits");
        assertTrue(errors.stream().anyMatch(e -> e.getLocalizedMessageId().contains("error.nodigitspassword")));
    }

    @Test
    public void testPasswordMissingSpecialChar() {
        String password = "Ab1Ab1Ab";
        List<PasswordPolicyCheckFail> errors =
                PasswordValidationUtils.validatePasswordFromPolicy(password, strictPolicy);
        assertFalse(errors.isEmpty(), "Password should require special characters");
        assertTrue(errors.stream().anyMatch(
                e -> e.getLocalizedMessageId().contains("error.nospecialcharacterspassword"))
        );
    }

    @Test
    public void testPasswordWithSpaces() {
        String password = "Ab1! Ab1!";
        List<PasswordPolicyCheckFail> errors =
                PasswordValidationUtils.validatePasswordFromPolicy(password, strictPolicy);
        assertFalse(errors.isEmpty(), "Password should not contain spaces");
        assertTrue(errors.stream().anyMatch(e -> e.getLocalizedMessageId().contains("error.spacesinpassword")));
    }

    @Test
    public void testPasswordWithConsecutiveChars() {
        String password = "AAbb11!!";
        List<PasswordPolicyCheckFail> errors =
                PasswordValidationUtils.validatePasswordFromPolicy(password, strictPolicy);
        assertFalse(errors.isEmpty(), "Password should not contain consecutive characters");
        assertTrue(errors.stream().anyMatch(
                e -> e.getLocalizedMessageId().contains("consecutive_characters_presents"))
        );
    }

    @Test
    public void testPasswordExceedingMaxCharOccurrences() {
        String password = "Ab1!b!!Ab";
        List<PasswordPolicyCheckFail> errors =
                PasswordValidationUtils.validatePasswordFromPolicy(password, strictPolicy);
        assertFalse(errors.isEmpty(), "Password should not exceed max character occurrences");
        assertTrue(errors.stream().anyMatch(
                e -> e.getLocalizedMessageId().contains("error.occurrencecharacterspassword"))
        );
    }

    @Test
    public void testPasswordUsingConfigurations() {
        String password = "Abc123!er}{hjxc";
        List<PasswordPolicyCheckFail> errors =
                PasswordValidationUtils.validatePasswordFromConfiguration(password);
        assertTrue(errors.isEmpty(), "Default valid password");
    }

    @Test
    public void testPasswordUsingDefaults() {
        String password = "Abc123!er}{hjxc";
        List<PasswordPolicyCheckFail> errors =
                PasswordValidationUtils.validatePasswordFromPolicy(password, PasswordPolicy.buildFromDefaults());
        assertTrue(errors.isEmpty(), "Default valid password");

    }
}
