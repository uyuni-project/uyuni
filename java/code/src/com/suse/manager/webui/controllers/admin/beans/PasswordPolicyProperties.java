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

package com.suse.manager.webui.controllers.admin.beans;

public class PasswordPolicyProperties {

    private Long minLength;
    private Long maxLength;
    private Boolean digitsFlag;
    private Boolean lowerCharFlag;
    private Boolean upperCharFlag;
    private Boolean consecutiveCharFlag;
    private Boolean specialCharFlag;
    private String specialCharList;
    private Boolean restrictedOccurrenceFlag;
    private Long maxCharOccurrence;

    public Long getMinLength() {
        return minLength;
    }

    public void setMinLength(Long minLengthIn) {
        minLength = minLengthIn;
    }

    public Long getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Long maxLengthIn) {
        maxLength = maxLengthIn;
    }

    public Boolean getDigitsFlag() {
        return digitsFlag;
    }

    public void setDigitsFlag(Boolean digitsFlagIn) {
        digitsFlag = digitsFlagIn;
    }

    public Boolean getLowerCharFlag() {
        return lowerCharFlag;
    }

    public void setLowerCharFlag(Boolean lowerCharFlagIn) {
        lowerCharFlag = lowerCharFlagIn;
    }

    public Boolean getUpperCharFlag() {
        return upperCharFlag;
    }

    public void setUpperCharFlag(Boolean upperCharFlagIn) {
        upperCharFlag = upperCharFlagIn;
    }

    public Boolean getConsecutiveCharFlag() {
        return consecutiveCharFlag;
    }

    public void setConsecutiveCharFlag(Boolean consecutiveCharFlagIn) {
        consecutiveCharFlag = consecutiveCharFlagIn;
    }

    public Boolean getSpecialCharFlag() {
        return specialCharFlag;
    }

    public void setSpecialCharFlag(Boolean specialCharFlagIn) {
        specialCharFlag = specialCharFlagIn;
    }

    public String getSpecialCharList() {
        return specialCharList;
    }

    public void setSpecialCharList(String specialCharListIn) {
        specialCharList = specialCharListIn;
    }

    public Boolean getRestrictedOccurrenceFlag() {
        return restrictedOccurrenceFlag;
    }

    public void setRestrictedOccurrenceFlag(Boolean restrictedOccurrenceFlagIn) {
        restrictedOccurrenceFlag = restrictedOccurrenceFlagIn;
    }

    public Long getMaxCharOccurrence() {
        return maxCharOccurrence;
    }

    public void setMaxCharOccurrence(Long maxCharOccurrenceIn) {
        maxCharOccurrence = maxCharOccurrenceIn;
    }
}
