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
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.common.validator.ValidatorResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Access to the configuration values stored in the rhnConfiguration table.
 *
 * Every key has a value and a default value. When the value is NULL, the default value is used.
 * The default value can only be changed by schema migrations.
 */
public class RhnConfigurationFactory extends HibernateFactory {

    private static final Logger LOG = LogManager.getLogger(RhnConfigurationFactory.class);

    private static final RhnConfigurationFactory SINGLETON = new RhnConfigurationFactory();

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
     * get all configurations
     *
     * @return the configurations ordered by key
     */
    public List<RhnConfiguration> listAll() {
        return getSession().createQuery("FROM RhnConfiguration ORDER BY key", RhnConfiguration.class).list();
    }

    /**
     * get all visible configurations
     *
     * @return the visible configurations ordered by key
     */
    public List<RhnConfiguration> listVisible() {
        return listAll().stream().filter(RhnConfiguration::isVisible).toList();
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
     * Lookup a configuration entry by its name
     *
     * @param keyName the key name
     * @return the configuration
     * @throws ValidatorException when the key is unknown or hidden
     */
    public RhnConfiguration lookupVisibleConfiguration(String keyName) throws ValidatorException {
        Optional<RhnConfiguration> optConfiguration = getSession().createNativeQuery("""
                        SELECT * FROM RhnConfiguration rc
                        WHERE rc.key = :keyName
                        """, RhnConfiguration.class)
                .setParameter("keyName", keyName)
                .uniqueResultOptional();
        if (optConfiguration.isEmpty()) {
            ValidatorResult result = new ValidatorResult();
            result.addError("server_settings.error.unknown_key", keyName);
            throw new ValidatorException(result);
        }
        else if (optConfiguration.get().isHidden()) {
            ValidatorResult result = new ValidatorResult();
            result.addError("server_settings.error.hidden_key", keyName);
            throw new ValidatorException(result);
        }
        return optConfiguration.get();
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
        configuration.setValue(valueIn);
        getSession().persist(configuration);
    }

    /**
     * Reset a configuration key to use the default value
     *
     * @param keyIn the key
     */
    public void resetToDefault(RhnConfiguration.KEYS keyIn) {
        getConfiguration(keyIn).resetToDefault();
    }

    /**
     * get the value of a configuration key, or the default value when no value is set
     * @param keyIn the key
     * @return the string value or null when empty
     */
    public String getString(RhnConfiguration.KEYS keyIn) {
        return getConfiguration(keyIn).getAsString();
    }

    /**
     * get the value of a configuration key, or the default value when no value is set
     * @param keyIn the key
     * @return the integer value or null when empty
     */
    public Integer getInteger(RhnConfiguration.KEYS keyIn) {
        return getConfiguration(keyIn).getAsInteger();
    }

    /**
     * get the value of a configuration key, or the default value when no value is set
     * @param keyIn the key
     * @return the long value or null when empty
     */
    public Long getLong(RhnConfiguration.KEYS keyIn) {
        return getConfiguration(keyIn).getAsLong();
    }

    /**
     * get the value of a configuration key, or the default value when no value is set
     * @param keyIn the key
     * @return the boolean value, false when empty
     */
    public boolean getBoolean(RhnConfiguration.KEYS keyIn) {
        return getConfiguration(keyIn).getAsBoolean();
    }

    /**
     * get the value of a configuration key, or the default value when no value is set
     * @param keyIn the key
     * @return the comma separated value as list, empty when the value is empty
     */
    public List<String> getList(RhnConfiguration.KEYS keyIn) {
        return getConfiguration(keyIn).getAsList();
    }

    /**
     * get the default value of a configuration key
     * @param keyIn the key
     * @return the string value or null when empty
     */
    public String getDefaultString(RhnConfiguration.KEYS keyIn) {
        return getConfiguration(keyIn).getDefaultAsString();
    }

    /**
     * get the default value of a configuration key
     * @param keyIn the key
     * @return the integer value or null when empty
     */
    public Integer getDefaultInteger(RhnConfiguration.KEYS keyIn) {
        return getConfiguration(keyIn).getDefaultAsInteger();
    }

    /**
     * get the default value of a configuration key
     * @param keyIn the key
     * @return the long value or null when empty
     */
    public Long getDefaultLong(RhnConfiguration.KEYS keyIn) {
        return getConfiguration(keyIn).getDefaultAsLong();
    }

    /**
     * get the default value of a configuration key
     * @param keyIn the key
     * @return the boolean value, false when empty
     */
    public boolean getDefaultBoolean(RhnConfiguration.KEYS keyIn) {
        return getConfiguration(keyIn).getDefaultAsBoolean();
    }
}
