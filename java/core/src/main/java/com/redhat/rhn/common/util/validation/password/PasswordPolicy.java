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

import com.redhat.rhn.domain.common.RhnConfiguration;
import com.redhat.rhn.domain.common.RhnConfigurationFactory;

public class PasswordPolicy {
    private boolean upperCharFlag;
    private boolean lowerCharFlag;
    private boolean digitFlag;
    private boolean specialCharFlag;
    private String specialChars;
    private boolean consecutiveCharsFlag;
    private boolean restrictedOccurrenceFlag;
    private int maxCharacterOccurrence;
    private int minLength;
    private int maxLength;

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

    /**
     * Class that holds the settings for the user password checks
     */
    public PasswordPolicy() {
    }

    /**
     * Helper method to build checks settings from SatConfiguration
     * @return the settings for the user password settings
     */
    public static PasswordPolicy buildFromFactory() {
        RhnConfigurationFactory factory = RhnConfigurationFactory.getSingleton();
        return new PasswordPolicy(
                factory.getBooleanConfiguration(RhnConfiguration.KEYS.PSW_CHECK_UPPER_CHAR_FLAG).getValue(),
                factory.getBooleanConfiguration(RhnConfiguration.KEYS.PSW_CHECK_LOWER_CHAR_FLAG).getValue(),
                factory.getBooleanConfiguration(RhnConfiguration.KEYS.PSW_CHECK_DIGIT_FLAG).getValue(),
                factory.getBooleanConfiguration(RhnConfiguration.KEYS.PSW_CHECK_SPECIAL_CHAR_FLAG).getValue(),
                factory.getStringConfiguration(RhnConfiguration.KEYS.PSW_CHECK_SPECIAL_CHARACTERS).getValue(),
                factory.getBooleanConfiguration(RhnConfiguration.KEYS.PSW_CHECK_CONSECUTIVE_CHAR_FLAG).getValue(),
                factory.getBooleanConfiguration(RhnConfiguration.KEYS.PSW_CHECK_RESTRICTED_OCCURRENCE_FLAG).getValue(),
                factory.getIntegerConfiguration(RhnConfiguration.KEYS.PSW_CHECK_MAX_OCCURRENCE).getValue(),
                factory.getIntegerConfiguration(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN).getValue(),
                factory.getIntegerConfiguration(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MAX).getValue()
        );
    }


    /**
     * Helper method to get the defaults checks settings from SatConfiguration
     * @return the default settings for the user password settings
     */
    public static PasswordPolicy buildFromDefaults() {
            RhnConfigurationFactory factory = RhnConfigurationFactory.getSingleton();
            return new PasswordPolicy(
                    factory.getBooleanConfiguration(
                            RhnConfiguration.KEYS.PSW_CHECK_UPPER_CHAR_FLAG).getDefaultValue(),
                    factory.getBooleanConfiguration(RhnConfiguration.KEYS.PSW_CHECK_LOWER_CHAR_FLAG).getDefaultValue(),
                    factory.getBooleanConfiguration(RhnConfiguration.KEYS.PSW_CHECK_DIGIT_FLAG).getDefaultValue(),
                    factory.getBooleanConfiguration(
                            RhnConfiguration.KEYS.PSW_CHECK_SPECIAL_CHAR_FLAG).getDefaultValue(),
                    factory.getStringConfiguration(
                            RhnConfiguration.KEYS.PSW_CHECK_SPECIAL_CHARACTERS).getDefaultValue(),
                    factory.getBooleanConfiguration(
                            RhnConfiguration.KEYS.PSW_CHECK_CONSECUTIVE_CHAR_FLAG).getDefaultValue(),
                    factory.getBooleanConfiguration(
                            RhnConfiguration.KEYS.PSW_CHECK_RESTRICTED_OCCURRENCE_FLAG).getDefaultValue(),
                    factory.getIntegerConfiguration(RhnConfiguration.KEYS.PSW_CHECK_MAX_OCCURRENCE).getDefaultValue(),
                    factory.getIntegerConfiguration(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN).getDefaultValue(),
                    factory.getIntegerConfiguration(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MAX).getDefaultValue()
            );
        }

    public boolean isUpperCharFlag() {
        return upperCharFlag;
    }

    public void setUpperCharFlag(boolean upperCharFlagIn) {
        upperCharFlag = upperCharFlagIn;
    }

    public boolean isLowerCharFlag() {
        return lowerCharFlag;
    }

    public void setLowerCharFlag(boolean lowerCharFlagIn) {
        lowerCharFlag = lowerCharFlagIn;
    }

    public boolean isDigitFlag() {
        return digitFlag;
    }

    public void setDigitFlag(boolean digitFlagIn) {
        digitFlag = digitFlagIn;
    }

    public boolean isSpecialCharFlag() {
        return specialCharFlag;
    }

    public void setSpecialCharFlag(boolean specialCharFlagIn) {
        specialCharFlag = specialCharFlagIn;
    }

    public String getSpecialChars() {
        return specialChars;
    }

    public void setSpecialChars(String specialCharsIn) {
        specialChars = specialCharsIn;
    }

    public boolean isConsecutiveCharsFlag() {
        return consecutiveCharsFlag;
    }

    public void setConsecutiveCharsFlag(boolean consecutiveCharsFlagIn) {
        consecutiveCharsFlag = consecutiveCharsFlagIn;
    }

    public boolean isRestrictedOccurrenceFlag() {
        return restrictedOccurrenceFlag;
    }

    public void setRestrictedOccurrenceFlag(boolean restrictedOccurrenceFlagIn) {
        restrictedOccurrenceFlag = restrictedOccurrenceFlagIn;
    }

    public int getMaxCharacterOccurrence() {
        return maxCharacterOccurrence;
    }

    public void setMaxCharacterOccurrence(int maxCharacterOccurrenceIn) {
        maxCharacterOccurrence = maxCharacterOccurrenceIn;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLengthIn) {
        minLength = minLengthIn;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLengthIn) {
        maxLength = maxLengthIn;
    }
}
