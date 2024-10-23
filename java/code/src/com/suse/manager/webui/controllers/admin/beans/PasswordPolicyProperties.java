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

    // Getters and Setters

    public Long getMinLength() {
        return minLength;
    }

    public void setMinLength(Long minLength) {
        this.minLength = minLength;
    }

    public Long getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Long maxLength) {
        this.maxLength = maxLength;
    }

    public Boolean getDigitsFlag() {
        return digitsFlag;
    }

    public void setDigitsFlag(Boolean digitsFlag) {
        this.digitsFlag = digitsFlag;
    }

    public Boolean getLowerCharFlag() {
        return lowerCharFlag;
    }

    public void setLowerCharFlag(Boolean lowerCharFlag) {
        this.lowerCharFlag = lowerCharFlag;
    }

    public Boolean getUpperCharFlag() {
        return upperCharFlag;
    }

    public void setUpperCharFlag(Boolean upperCharFlag) {
        this.upperCharFlag = upperCharFlag;
    }

    public Boolean getConsecutiveCharFlag() {
        return consecutiveCharFlag;
    }

    public void setConsecutiveCharFlag(Boolean consecutiveCharFlag) {
        this.consecutiveCharFlag = consecutiveCharFlag;
    }

    public Boolean getSpecialCharFlag() {
        return specialCharFlag;
    }

    public void setSpecialCharFlag(Boolean specialCharFlag) {
        this.specialCharFlag = specialCharFlag;
    }

    public String getSpecialCharList() {
        return specialCharList;
    }

    public void setSpecialCharList(String specialCharList) {
        this.specialCharList = specialCharList;
    }

    public Boolean getRestrictedOccurrenceFlag() {
        return restrictedOccurrenceFlag;
    }

    public void setRestrictedOccurrenceFlag(Boolean restrictedOccurrenceFlag) {
        this.restrictedOccurrenceFlag = restrictedOccurrenceFlag;
    }

    public Long getMaxCharOccurrence() {
        return maxCharOccurrence;
    }

    public void setMaxCharOccurrence(Long maxCharOccurrence) {
        this.maxCharOccurrence = maxCharOccurrence;
    }
}
