/*
 * Copyright (c) 2014 Red Hat, Inc.
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

package com.redhat.rhn.domain.common;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * Access to the configuration values stored in the rhnConfiguration table.
 *
 * Every key has a value and a default value. When the value is NULL, the default value is used.
 * The default value can only be changed by schema migrations.
 */
public class RhnConfigurationFactory extends HibernateFactory {

    private static final Logger LOG = LogManager.getLogger(RhnConfigurationFactory.class);

    private static final RhnConfigurationFactory SINGLETON = new RhnConfigurationFactory();

    private static final List<String> TRUE_VALUES = List.of("1", "y", "true", "yes", "on");

    /**
     * Configuration Factory default Constructor
     */
    private RhnConfigurationFactory() {
        super();
    }

    public static RhnConfigurationFactory getSingleton() {
        return SINGLETON;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * get a list of configurations by key
     *
     * @param keysIn the keys
     * @return the configurations
     */
    public List<RhnConfiguration> getConfiguration(List<RhnConfiguration.KEYS> keysIn) {
        return getSession().findMultiple(RhnConfiguration.class, keysIn);
    }

    /**
     * get a configuration by key
     *
     * @param keyIn the key
     * @return the configuration
     * @throws IllegalStateException when the key does not exist in the database
     */
    public RhnConfiguration getConfiguration(RhnConfiguration.KEYS keyIn) {
        RhnConfiguration configuration = getSession().find(RhnConfiguration.class, keyIn);
        if (configuration == null) {
            throw new IllegalStateException("Configuration key not found in database: " + keyIn);
        }
        return configuration;
    }

    /**
     * Set the value of a configuration key.
     * Setting null, a blank value or the default value removes the value, so the default value is used.
     *
     * @param keyIn the key
     * @param valueIn the value
     */
    public void setValue(RhnConfiguration.KEYS keyIn, Object valueIn) {
        RhnConfiguration configuration = getConfiguration(keyIn);
        String newValue = valueIn == null ? null : StringUtils.trimToNull(String.valueOf(valueIn));
        if (Objects.equals(newValue, configuration.getDefaultValue())) {
            newValue = null;
        }
        configuration.setValue(newValue);
        getSession().persist(configuration);
    }

    /**
     * Reset a configuration key to use the default value
     *
     * @param keyIn the key
     */
    public void resetToDefault(RhnConfiguration.KEYS keyIn) {
        setValue(keyIn, null);
    }

    /**
     * get the value of a configuration key, or the default value when no value is set
     * @param keyIn the key
     * @return the string value or null when empty
     */
    public String getString(RhnConfiguration.KEYS keyIn) {
        return StringUtils.trimToNull(getConfiguration(keyIn).getEffectiveValue());
    }

    /**
     * get the value of a configuration key, or the default value when no value is set
     * @param keyIn the key
     * @return the integer value or null when empty
     */
    public Integer getInteger(RhnConfiguration.KEYS keyIn) {
        return toInteger(getString(keyIn));
    }

    /**
     * get the value of a configuration key, or the default value when no value is set
     * @param keyIn the key
     * @return the long value or null when empty
     */
    public Long getLong(RhnConfiguration.KEYS keyIn) {
        return toLong(getString(keyIn));
    }

    /**
     * get the value of a configuration key, or the default value when no value is set
     * @param keyIn the key
     * @return the boolean value, false when empty
     */
    public boolean getBoolean(RhnConfiguration.KEYS keyIn) {
        return toBoolean(getString(keyIn));
    }

    /**
     * get the value of a configuration key, or the default value when no value is set
     * @param keyIn the key
     * @return the comma separated value as list, empty when the value is empty
     */
    public List<String> getList(RhnConfiguration.KEYS keyIn) {
        return toList(getString(keyIn));
    }

    /**
     * get the default value of a configuration key
     * @param keyIn the key
     * @return the string value or null when empty
     */
    public String getDefaultString(RhnConfiguration.KEYS keyIn) {
        return StringUtils.trimToNull(getConfiguration(keyIn).getDefaultValue());
    }

    /**
     * get the default value of a configuration key
     * @param keyIn the key
     * @return the integer value or null when empty
     */
    public Integer getDefaultInteger(RhnConfiguration.KEYS keyIn) {
        return toInteger(getDefaultString(keyIn));
    }

    /**
     * get the default value of a configuration key
     * @param keyIn the key
     * @return the long value or null when empty
     */
    public Long getDefaultLong(RhnConfiguration.KEYS keyIn) {
        return toLong(getDefaultString(keyIn));
    }

    /**
     * get the default value of a configuration key
     * @param keyIn the key
     * @return the boolean value, false when empty
     */
    public boolean getDefaultBoolean(RhnConfiguration.KEYS keyIn) {
        return toBoolean(getDefaultString(keyIn));
    }

    /**
     * get the default value of a configuration key
     * @param keyIn the key
     * @return the comma separated value as list, empty when the value is empty
     */
    public List<String> getDefaultList(RhnConfiguration.KEYS keyIn) {
        return toList(getDefaultString(keyIn));
    }

    private static Integer toInteger(String value) {
        return value == null ? null : Integer.valueOf(value);
    }

    private static Long toLong(String value) {
        return value == null ? null : Long.valueOf(value);
    }

    private static boolean toBoolean(String value) {
        return value != null && TRUE_VALUES.contains(value.toLowerCase());
    }

    private static List<String> toList(String value) {
        if (value == null) {
            return List.of();
        }
        return Arrays.stream(value.split(",")).map(String::trim).filter(v -> !v.isEmpty()).toList();
    }
}
