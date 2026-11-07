/*
 * Copyright (c) 2024--2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.redhat.rhn.domain.common;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


/**
 * Rhn Configuration Table
 */
@Entity
@Table(name = "rhnConfiguration")
public class RhnConfiguration extends BaseDomainHelper {

    /**
     * Type of a configuration value
     */
    private enum ValueType {
        STRING,
        INTEGER,
        BOOLEAN,
        LIST
    }

    /**
     * Visibility of a configuration key in the server settings UI.
     * Hidden keys are managed elsewhere and can't be changed there.
     */
    private enum Visibility {
        VISIBLE,
        HIDDEN
    }

    public enum KEYS {
        EXTAUTH_DEFAULT_ORGID(ValueType.INTEGER, Visibility.HIDDEN),
        EXTAUTH_USE_ORGUNIT(ValueType.BOOLEAN, Visibility.HIDDEN),
        EXTAUTH_KEEP_TEMPROLES(ValueType.BOOLEAN, Visibility.HIDDEN),
        SYSTEM_CHECKIN_THRESHOLD(ValueType.INTEGER),
        PSW_CHECK_LENGTH_MIN(ValueType.INTEGER, Visibility.HIDDEN),
        PSW_CHECK_LENGTH_MAX(ValueType.INTEGER, Visibility.HIDDEN),
        PSW_CHECK_LOWER_CHAR_FLAG(ValueType.BOOLEAN, Visibility.HIDDEN),
        PSW_CHECK_UPPER_CHAR_FLAG(ValueType.BOOLEAN, Visibility.HIDDEN),
        PSW_CHECK_DIGIT_FLAG(ValueType.BOOLEAN, Visibility.HIDDEN),
        PSW_CHECK_CONSECUTIVE_CHAR_FLAG(ValueType.BOOLEAN, Visibility.HIDDEN),
        PSW_CHECK_SPECIAL_CHAR_FLAG(ValueType.BOOLEAN, Visibility.HIDDEN),
        PSW_CHECK_RESTRICTED_OCCURRENCE_FLAG(ValueType.BOOLEAN, Visibility.HIDDEN),
        PSW_CHECK_MAX_OCCURRENCE(ValueType.INTEGER, Visibility.HIDDEN),
        PSW_CHECK_SPECIAL_CHARACTERS(ValueType.STRING, Visibility.HIDDEN),
        HTTP_PROXY(ValueType.STRING, Visibility.HIDDEN),
        HTTP_PROXY_USERNAME(ValueType.STRING, Visibility.HIDDEN),
        HTTP_PROXY_PASSWORD(ValueType.STRING, Visibility.HIDDEN),
        NO_PROXY(ValueType.LIST),
        USER_LOGIN_LENGTH_MIN(ValueType.INTEGER),
        USER_LOGIN_LENGTH_MAX(ValueType.INTEGER),
        USER_EMAIL_LENGTH_MAX(ValueType.INTEGER),
        REPOSYNC_DOWNLOAD_THREADS(ValueType.INTEGER),
        REPOSYNC_TIMEOUT(ValueType.INTEGER),
        REPOSYNC_MINRATE(ValueType.INTEGER),
        REPOSYNC_NEVRA_FILTER(ValueType.BOOLEAN),
        SCC_FORWARD_REGISTRATION,
        SCC_MIRROR_URL,
        SCC_REG_BATCH_SIZE,
        SCC_REG_ERROR_EXPIRE_TIME,
        SCC_URL,
        SYNC_SOURCE_PACKAGES(ValueType.BOOLEAN),
        TEMP_CHANNEL_TOKEN_LIFETIME,
        TOKEN_LIFETIME,
        URLGRABBER_LOGSPEC(ValueType.STRING),
        BUFFER_SIZE(ValueType.INTEGER),
        FORCE_PACKAGE_UPLOAD(ValueType.BOOLEAN),
        PROXY_AUTH_TIMEOUT(ValueType.INTEGER),
        PUSH_SESSION_LIFETIME(ValueType.INTEGER);

        private final ValueType type;
        private final Visibility visibility;

        KEYS(ValueType typeIn) {
            this(typeIn, Visibility.VISIBLE);
        }

        KEYS(ValueType typeIn, Visibility visibilityIn) {
            type = typeIn;
            visibility = visibilityIn;
        }

        private ValueType getType() {
            return type;
        }

        public boolean isHidden() {
            return visibility == Visibility.HIDDEN;
        }

        private boolean isBoolean() {
            return type == ValueType.BOOLEAN;
        }
    }

    @Id
    @Column(length = 64, nullable = false)
    @Enumerated(EnumType.STRING)
    private KEYS key;

    @Column(length = 512, nullable = false)
    private String description;

    @Column(length = 512)
    private String value;

    // the default value is managed by schema migrations only
    @Column(name = "default_value", length = 512, insertable = false, updatable = false)
    private String defaultValue;

    public String getDescription() {
        return description;
    }

    /**
     * @return true if the key is visible in the server settings UI
     */
    public boolean isVisible() {
        return !key.isHidden();
    }

    /**
     * @return true if the key is hidden in the server settings UI
     */
    public boolean isHidden() {
        return key.isHidden();
    }

    /**
     * Set the value
     * @param valueIn the value
     */
    public void setValue(Object valueIn) {
        String newValue = valueIn == null ? null : StringUtils.trimToNull(String.valueOf(valueIn));
        boolean isDefault = key.isBoolean() ?
                toBoolean(newValue) == toBoolean(defaultValue) :
                newValue == null || newValue.equals(StringUtils.trimToNull(defaultValue));

        if (isDefault) {
            newValue = null;
        }
        value = newValue;
    }

    /**
     * Validate the value and add errors to the result
     * @param valueIn the value
     * @param result the result
     */
    public void validate(String valueIn, ValidatorResult result) {
        String val = StringUtils.trimToEmpty(valueIn);
        if (val.isEmpty()) {
            return;
        }
        switch (key.getType()) {
            case INTEGER -> {
                // read as Integer by some consumers, e.g. UserDefaults
                try {
                    Integer.parseInt(val);
                }
                catch (NumberFormatException e) {
                    result.addFieldError(key.name(), "server_settings.error.integer", key.name());
                }
            }
            case BOOLEAN -> {
                // stored as Boolean, so it is compared with the default value by meaning
                if (!val.equals("true") && !val.equals("false")) {
                    result.addFieldError(key.name(), "server_settings.error.boolean", key.name());
                }
            }
            default -> {
                // STRING and LIST accept any value
            }
        }
    }

    /**
     * Reset the value to the default value
     */
    public void resetToDefault() {
        value = null;
    }

    /**
     * @return the value as string, fallback to default value when the value is empty
     */
    public String getAsString() {
        return StringUtils.trimToNull(StringUtils.isBlank(value) ? defaultValue : value);
    }

    /**
     * @return the value as integer, fallback to default value when the value is empty
     */
    public Integer getAsInteger() {
        return toInteger(getAsString());
    }

    /**
     * @return the value as long, fallback to default value when the value is empty
     */
    public Long getAsLong() {
        return toLong(getAsString());
    }

    /**
     * @return the value as boolean, fallback to default value when the value is empty
     */
    public boolean getAsBoolean() {
        return toBoolean(getAsString());
    }

    /**
     * @return the value as list, empty when the value is empty
     */
    public List<String> getAsList() {
        return toList(getAsString());
    }

    /**
     * @return the default value as string, null when the value is empty
     */
    public String getDefaultAsString() {
        return StringUtils.trimToNull(defaultValue);
    }

    /**
     * @return the default value as integer, null when the value is empty
     */
    public Integer getDefaultAsInteger() {
        return toInteger(getDefaultAsString());
    }

    /**
     * @return the default value as long, null when the value is empty
     */
    public Long getDefaultAsLong() {
        return toLong(getDefaultAsString());
    }

    /**
     * @return the default value as boolean, false when the value is empty
     */
    public boolean getDefaultAsBoolean() {
        return toBoolean(getDefaultAsString());
    }

    /**
     * @return the default value as list, empty when the value is empty
     */
    public List<String> getDefaultAsList() {
        return toList(getDefaultAsString());
    }


    private static Integer toInteger(String value) {
        return value == null ? null : Integer.valueOf(value);
    }

    private static Long toLong(String value) {
        return value == null ? null : Long.valueOf(value);
    }

    private static final List<String> TRUE_VALUES = List.of("1", "y", "true", "yes", "on");

    private static boolean toBoolean(String value) {
        return value != null && TRUE_VALUES.contains(value.toLowerCase());
    }

    private static List<String> toList(String value) {
        if (value == null) {
            return List.of();
        }
        return Arrays.stream(value.split(",")).map(String::trim).filter(v -> !v.isEmpty()).toList();
    }

    /**
     * @return the translated description
     */
    public String getTranslatedDescription() {
        LocalizationService ls = LocalizationService.getInstance();
        String messageId = getDescriptionMessageId();
        return ls.hasMessage(messageId) ? ls.getMessage(messageId) : description;
    }

    /**
     * @return the message id of the description
     */
    public String getDescriptionMessageId() {
        return "server_settings.description." + key.name();
    }

    /**
     * @return the key name
     */
    public String getUIName() {
        return key.name();
    }

    /**
     * @return the type of the value as string
     */
    public String getUIType() {
        return key.type.toString();
    }

    /**
     * @return the current value or the default value as fallback
     */
    public String getUIValue() {
        return key.isBoolean() ? String.valueOf(getAsBoolean()) : getAsString();
    }

    /**
     * @return the default value
     */
    public String getUIDefaultValue() {
        return key.isBoolean() ? String.valueOf(getDefaultAsBoolean()) : getDefaultAsString();
    }

    /**
     * @return true if the value is null and the default value would be used in getters
     */
    public boolean usesDefaultValue() {
        return (null == value);
    }

    /**
     * @return true if the value differ from its default
     */
    public boolean isUIModified() {
        return StringUtils.isNotBlank(value);
    }

    /**
     * FOR TESTS ONLY!
     * @param descriptionIn the new description
     */
    public void testSetDescription(String descriptionIn) {
        description = descriptionIn;
    }
}
