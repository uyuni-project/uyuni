/*
 * Copyright (c) 2024 Red Hat, Inc.
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
package com.redhat.rhn.domain.common;

import com.redhat.rhn.domain.BaseDomainHelper;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Entity
@Table(name = "rhnConfiguration")
public class RhnConfiguration extends BaseDomainHelper {

    public enum KEYS {
        extauth_default_orgid,
        extauth_use_orgunit,
        extauth_keep_temproles,
        system_checkin_threshold,
        psw_check_length_min,
        psw_check_length_max,
        psw_check_lower_char_flag,
        psw_check_upper_char_flag,
        psw_check_digit_flag,
        psw_check_consecutive_char_flag,
        psw_check_special_char_flag,
        psw_check_restricted_occurrence_flag,
        psw_check_max_occurrence,
        psw_check_special_characters;
    }

    @Id
    @Column(name = "key", length = 64, nullable = false)
    @Enumerated(EnumType.STRING)
    private KEYS key;

    @Column(name = "description", length = 512, nullable = false)
    private String description;

    @Column(name = "value", length = 512)
    private String value;

    @Column(name = "default_value", length = 512)
    private String defaultValue;

    @Column(name = "created", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name = "modified", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    /**
     * Rhn Configuration Table
     */
    // Constructors
    public RhnConfiguration() {
    }

    /**
     * Parameter constructor
     * @param keyIn the key
     * @param descriptionIn the description
     * @param valueIn the value
     * @param defaultValueIn the default value
     */
    public RhnConfiguration(KEYS keyIn, String descriptionIn, String valueIn,
                            String defaultValueIn) {
        key = keyIn;
        description = descriptionIn;
        value = valueIn;
        defaultValue = defaultValueIn;
    }

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

    public void setDefaultValue(String defaultValueIn) {
        defaultValue = defaultValueIn;
    }

}
