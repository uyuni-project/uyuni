/*
 * Copyright (c) 2024--2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.common;

import com.redhat.rhn.domain.BaseDomainHelper;

import java.util.Date;

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

    public enum KEYS {
        EXTAUTH_DEFAULT_ORGID,
        EXTAUTH_USE_ORGUNIT,
        EXTAUTH_KEEP_TEMPROLES,
        SYSTEM_CHECKIN_THRESHOLD,
        PSW_CHECK_LENGTH_MIN,
        PSW_CHECK_LENGTH_MAX,
        PSW_CHECK_LOWER_CHAR_FLAG,
        PSW_CHECK_UPPER_CHAR_FLAG,
        PSW_CHECK_DIGIT_FLAG,
        PSW_CHECK_CONSECUTIVE_CHAR_FLAG,
        PSW_CHECK_SPECIAL_CHAR_FLAG,
        PSW_CHECK_RESTRICTED_OCCURRENCE_FLAG,
        PSW_CHECK_MAX_OCCURRENCE,
        PSW_CHECK_SPECIAL_CHARACTERS,
        AUTO_GENERATE_BOOTSTRAP_REPO,
        BOOTSTRAP_REPO_FLUSH,
        CHECKSUM_PRIORITY_LIST,
        DISALLOWED_SUFFIXES,
        DISALLOW_USER_CREATION,
        DISCONNECTED,
        ENABLE_NVREA,
        ENABLE_SNAPSHOTS,
        FORCE_PACKAGE_UPLOAD,
        FORWARD_REGISTRATION,
        JAVA_ACTIONS_DISPLAY_LIMIT,
        JAVA_BASE_DOMAIN,
        JAVA_COBBLER_BOOTSTRAP_ARCH,
        JAVA_COBBLER_BOOTSTRAP_BREED,
        JAVA_COBBLER_BOOTSTRAP_EXTRA_KERNEL_OPTIONS,
        JAVA_COBBLER_BOOTSTRAP_INITRD,
        JAVA_COBBLER_BOOTSTRAP_KERNEL,
        JAVA_CONFIG_FILE_EDIT_SIZE,
        JAVA_CUSTOM_FOOTER,
        JAVA_CUSTOM_HEADER,
        JAVA_CVE_AUDIT_ENABLE_OVAL_METADATA,
        JAVA_DISABLE_REMOTE_COMMANDS_FROM_UI,
        JAVA_DISABLE_SUPPORTDATA_UPLOAD,
        JAVA_DISABLE_UPDATE_STATUS,
        JAVA_DOWNLOAD_URL_LIFETIME,
        JAVA_ERRATA_ADVISORY_MAP_CSV_DOWNLOAD_URL,
        JAVA_ERRATA_CACHE_COMPUTE_THRESHOLD,
        JAVA_KIWI_OS_IMAGE_BUILDING_ENABLED,
        JAVA_LEGAL_NOTE,
        JAVA_MAX_CHANGELOG_ENTRIES,
        JAVA_NON_EXPIRABLE_PACKAGE_URLS,
        JAVA_NOTIFICATIONS_LIFETIME,
        JAVA_NOTIFICATIONS_TYPE_DISABLED,
        JAVA_OVAL_DEFINITIONS_BULK_SIZE,
        JAVA_POWER_MANAGEMENT_TYPES,
        JAVA_REBOOT_DELAY,
        JAVA_SALT_BATCH_DELAY,
        JAVA_SALT_BATCH_SIZE,
        JAVA_SALT_CHECK_DOWNLOAD_TOKENS,
        JAVA_SALT_CONTENT_STAGING_ADVANCE,
        JAVA_SALT_CONTENT_STAGING_WINDOW,
        JAVA_SALT_PRESENCE_PING_GATHER_JOB_TIMEOUT,
        JAVA_SALT_PRESENCE_PING_TIMEOUT,
        JAVA_SANDBOX_LIFETIME,
        JAVA_SERVER_SATELLITE_TINY_URL_TIMEOUT,
        JAVA_TASKOMATIC_COBBLER_USER,
        KICKSTART_PACKAGES,
        PROXY_AUTH_TIMEOUT,
        REG_BATCH_SIZE,
        REG_ERROR_EXPIRE_TIME,
        REPOSYNC_DOWNLOAD_THREADS,
        REPOSYNC_MINRATE,
        REPOSYNC_NEVRA_FILTER,
        REPOSYNC_TIMEOUT,
        RESET_BASE_CHANNEL,
        SESSION_LIFETIME,
        SYNC_SOURCE_PACKAGES,
        SYNC_TO_TEMP,
        TEMP_TOKEN_LIFETIME,
        TOKEN_LIFETIME,
        TOKEN_REFRESH_AUTO_DEPLOY,
        UPDATE_HOST_DOMAIN,
        USE_TASKOMATIC_REPOMD,
        WEB_CONFIG_DELIM_END,
        WEB_CONFIG_DELIM_START,
        WEB_DEFAULT_MAIL_FROM,
        WEB_DISALLOWED_MAIL_DOMAINS,
        WEB_MAXIMUM_CONFIG_FILE_SIZE,
        WEB_RESTRICT_MAIL_DOMAINS,
        WEB_SESSION_DATABASE_LIFETIME,
        WEB_SPA_ENABLE,
        WEB_SPA_TIMEOUT,
        WEB_SUBSCRIBE_PROXY_CHANNEL;
    }

    @Id
    @Column(name = "key", length = 64, nullable = false)
    @Enumerated(EnumType.STRING)
    private KEYS key;

    @Column(name = "description", length = 512, nullable = false)
    private String description;

    @Column(name = "value", length = 512)
    private String value;

    // the default value is managed by schema migrations only
    @Column(name = "default_value", length = 512, insertable = false, updatable = false)
    private String defaultValue;

    @Column(name = "created", nullable = false, updatable = false)
    private Date created;

    @Column(name = "modified", nullable = false)
    private Date modified;

    public KEYS getKey() {
        return key;
    }

    public void setKey(KEYS keyIn) {
        key = keyIn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String descriptionIn) {
        description = descriptionIn;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String valueIn) {
        value = valueIn;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @return the value or the default value when no value is set
     */
    public String getEffectiveValue() {
        return value != null ? value : defaultValue;
    }

}
