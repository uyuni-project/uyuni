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
package com.redhat.rhn.common.util.test;

import com.redhat.rhn.common.util.UserPasswordUtils;

import org.junit.jupiter.api.Test;

import java.util.List;

public class UserPasswordUtilsTest {

    @Test
    public void testPasswordMatcher() throws Exception {
        boolean upperCharFlagIn = false;
        boolean lowerCharFlagIn = true;
        boolean digitFlagIn = false;
        boolean specialCharFlagIn = false;
        String specialCharsIn = "';[].,";
        boolean consecutiveCharsFlagIn = false;
        boolean restrictedOccurrenceFlagIn = false;
        int maxCharacterOccurrenceIn = 10;
        int minLengthIn = 4;
        int maxLengthIn = 10;
        UserPasswordUtils.PasswordPolicy settings =
                new UserPasswordUtils.PasswordPolicy(
                        upperCharFlagIn,
                        lowerCharFlagIn,
                        digitFlagIn,
                        specialCharFlagIn,
                        specialCharsIn,
                        consecutiveCharsFlagIn,
                        restrictedOccurrenceFlagIn,
                        maxCharacterOccurrenceIn,
                        minLengthIn,
                        maxLengthIn
                );
        String password = "\t";
        List<UserPasswordUtils.UserPasswordCheckFail> errors =
                UserPasswordUtils.validatePasswordFromPolicy(password, settings);
        errors.forEach((k) -> System.out.println(k.getLocalizedErrorMessage()));
    }

    @Test
    public void validateFromDefaultSatConfigurations() throws Exception {
        String password = "\t";
        List<UserPasswordUtils.UserPasswordCheckFail> errors =
                UserPasswordUtils.validatePasswordFromSatConfiguration(password);
        errors.forEach((k) -> System.out.println(k.getLocalizedErrorMessage()));
    }

    @Test
    public void lowerCharacterRequired() {
        boolean upperCharFlagIn = true;
        boolean lowerCharFlagIn = true;
        boolean digitFlagIn = true;
        boolean specialCharFlagIn = true;
        String specialCharsIn = "';[].,";
        boolean consecutiveCharsFlagIn = true;
        boolean restrictedOccurrenceFlagIn = true;
        int maxCharacterOccurrenceIn = 2;
        int minLengthIn = 1;
        int maxLengthIn = 6;
        UserPasswordUtils.PasswordPolicy settings =
                new UserPasswordUtils.PasswordPolicy(
                        upperCharFlagIn,
                        lowerCharFlagIn,
                        digitFlagIn,
                        specialCharFlagIn,
                        specialCharsIn,
                        consecutiveCharsFlagIn,
                        restrictedOccurrenceFlagIn,
                        maxCharacterOccurrenceIn,
                        minLengthIn,
                        maxLengthIn
                );
    }
}
