/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.frontend.dto;

import com.redhat.rhn.common.localization.LocalizationService;

import org.apache.commons.lang3.BooleanUtils;


/**
 * DTO for a com.redhat.rhn.domain.ActivationKey
 */
public class ActivationKeyDto extends BaseDto {

    private Long id;
    private boolean keyDisabled;
    private String note;
    private String token;
    private Long usageLimit;
    private Long systemCount;
    private String orgDefault;

    /**
     * Gets the value of id
     *
     * @return the value of id
     */
    @Override
    public Long getId() {
        return this.id;
    }

    /**
     * Sets the value of id
     *
     * @param argId Value to assign to this.id
     */
    public void setId(Long argId) {
        this.id = argId;
    }

    /**
     * Is the key disabled?
     * @return Returns true if disabled, false if enabled.
     */
    public boolean isKeyDisabled() {
        return keyDisabled;
    }

    /**
     * Disable (or enable) the key
     * @param value 1 if the key is disabled
     */
    public void setKeyDisabled(Integer value) {
        this.keyDisabled = value != null && (value.equals(1));
    }

    /**
     * Gets the value of note
     *
     * @return the value of note
     */
    public String getNote() {
        return this.note;
    }

    /**
     * Sets the value of note
     *
     * @param argNote Value to assign to this.note
     */
    public void setNote(String argNote) {
        this.note = argNote;
    }

    /**
     * Gets the value of token
     *
     * @return the value of token
     */
    public String getToken() {
        return this.token;
    }

    /**
     * Sets the value of token
     *
     * @param argToken Value to assign to this.token
     */
    public void setToken(String argToken) {
        this.token = argToken;
    }

    /**
     * Gets the value of usageLimit
     *
     * @return the value of usageLimit
     */
    public Long getUsageLimit() {
        return this.usageLimit;
    }

    /**
     * Sets the value of usageLimit
     *
     * @param argUsageLimit Value to assign to this.usageLimit
     */
    public void setUsageLimit(Long argUsageLimit) {
        this.usageLimit = argUsageLimit;
    }

    /**
     * Gets the value of systemCount
     *
     * @return the value of systemCount
     */
    public Long getSystemCount() {
        return this.systemCount;
    }

    /**
     * Sets the value of systemCount
     *
     * @param argSystemCount Value to assign to this.systemCount
     */
    public void setSystemCount(Long argSystemCount) {
        this.systemCount = argSystemCount;
    }

    /**
     * Gets the i18ned of Yes/No if its the org default
     *
     * @return the value of orgDefault
     */
    public String getFormattedOrgDefault() {
        LocalizationService ls = LocalizationService.getInstance();
        if (isOrgDefault()) {
            return ls.getMessage("yes");
        }
        return ls.getMessage("no");
    }

    /**
     *
     * @return true if this is the org default
     */
    public boolean isOrgDefault() {
        return BooleanUtils.toBoolean(orgDefault);
    }

    /**
     * Sets the value of orgDefault
     *
     * @param argOrgDefault Value to assign to this.orgDefault
     */
    public void setOrgDefault(String argOrgDefault) {
        this.orgDefault = argOrgDefault;
    }
}
