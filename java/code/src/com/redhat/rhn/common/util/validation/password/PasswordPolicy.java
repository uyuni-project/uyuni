package com.redhat.rhn.common.util.validation.password;

import com.redhat.rhn.domain.common.SatConfigFactory;

public class PasswordPolicy {
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
                SatConfigFactory.getSatConfigBooleanValue(SatConfigFactory.PSW_CHECK_RESTRICTED_OCCURRENCE_FLAG),
                SatConfigFactory.getSatConfigIntValue(SatConfigFactory.PSW_CHECK_MAX_OCCURRENCE, 2),
                SatConfigFactory.getSatConfigIntValue(SatConfigFactory.PSW_CHECK_LENGTH_MIN, 4),
                SatConfigFactory.getSatConfigIntValue(SatConfigFactory.PSW_CHECK_LENGTH_MAX, 32)
        );
    }

}
