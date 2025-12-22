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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.domain.common;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.util.List;

/**
 * SatConfigFactory
 */
public class RhnConfigurationFactory extends HibernateFactory {

    private static final Logger LOG = LogManager.getLogger(RhnConfigurationFactory.class);

    private static final RhnConfigurationFactory SINGLETON = new RhnConfigurationFactory();

    public static class ConfigurationValues<T> {
        private T value;
        private T defaultValue;

        /**
         * Configuration values helper class
         *
         * @param valueIn        the current value
         * @param defaultValueIn the default value
         */
        public ConfigurationValues(T valueIn, T defaultValueIn) {
            value = valueIn;
            defaultValue = defaultValueIn;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T valueIn) {
            value = valueIn;
        }

        public T getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(T defaultValueIn) {
            defaultValue = defaultValueIn;
        }
    }

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
        return getSession().byMultipleIds(RhnConfiguration.class).multiLoad(keysIn);
    }

    /**
     * get a configuration by key
     *
     * @param keyIn the key
     * @return the configuration
     */
    public RhnConfiguration getConfiguration(RhnConfiguration.KEYS keyIn) {
        //return getSession().byId(RhnConfiguration.class).load(keyIn);
        return getSession().find(RhnConfiguration.class, keyIn);
    }

    /**
     * update the current configurations
     *
     * @param config the configuration entity
     */
    public void update(RhnConfiguration config) {
        Session session = getSession();
        session.persist(config);
    }

    /**
     * perform a bulk update for a configuration list
     *
     * @param configs the configs
     */
    public void bulkUpdate(List<RhnConfiguration> configs) {
        configs.forEach(config -> updateConfigurationValue(config.getKey(), config.getValue()));
    }

    /**
     * set a configuration value by key
     *
     * @param keyIn the keys
     * @param value the value
     */
    public void updateConfigurationValue(RhnConfiguration.KEYS keyIn, Object value) {
        Session session = getSession();
        RhnConfiguration entity = getConfiguration(keyIn);
        entity.setValue(String.valueOf(value));
        session.persist(entity);
    }

    /**
     * get a configuration value by key
     * @param keyIn the keys
     * @return the string value
     */
    public ConfigurationValues<String> getStringConfiguration(RhnConfiguration.KEYS keyIn) {
        RhnConfiguration configuration = getConfiguration(keyIn);
        return new ConfigurationValues<>(configuration.getValue(), configuration.getDefaultValue());
    }

    /**
     * get a configuration value by key
     * @param keyIn the keys
     * @return the boolean value
     */
    public ConfigurationValues<Boolean> getBooleanConfiguration(RhnConfiguration.KEYS keyIn) {
        RhnConfiguration configuration = getConfiguration(keyIn);
        return new ConfigurationValues<>(
                Boolean.parseBoolean(configuration.getValue()),
                Boolean.parseBoolean(configuration.getDefaultValue())
        );
    }

    /**
     * get a configuration value by key
     * @param keyIn the keys
     * @return the integer value
     */
    public ConfigurationValues<Integer> getIntegerConfiguration(RhnConfiguration.KEYS keyIn) {
        RhnConfiguration configuration = getConfiguration(keyIn);
        return new ConfigurationValues<>(
                Integer.parseInt(configuration.getValue()),
                Integer.parseInt(configuration.getDefaultValue())
        );
    }

    /**
     * get a configuration value by key
     * @param keyIn the keys
     * @return the long value
     */
    public ConfigurationValues<Long> getLongConfiguration(RhnConfiguration.KEYS keyIn) {
        RhnConfiguration configuration = getConfiguration(keyIn);
        return new ConfigurationValues<>(
                Long.parseLong(configuration.getValue()),
                Long.parseLong(configuration.getDefaultValue())
        );
    }

}
