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

package com.suse.coco.configuration;

import com.suse.common.configuration.ConfigurationSource;
import com.suse.common.configuration.EnvironmentConfigurationSource;
import com.suse.common.configuration.MultipleConfigurationSource;
import com.suse.common.configuration.ResourceConfigurationSource;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default application configuration. Values are first read from the environment then from a property file
 * which defines the default values. A part for the mandatory properties, all the settings should have a value
 * inside the property file.
 */
public class DefaultConfiguration implements Configuration {

    private static final Stream<String> MANDATORY_PROPERTIES = Stream.of(
        "database_user",
        "database_password",
        "database_connection"
    );

    private final ConfigurationSource configurationSource;

    /**
     * Default constructor
     */
    public DefaultConfiguration() {
        configurationSource = new MultipleConfigurationSource(List.of(
            new EnvironmentConfigurationSource(),
            new ResourceConfigurationSource("configuration-defaults.properties")
        ));

        List<String> missingProperty = MANDATORY_PROPERTIES
            .filter(property -> configurationSource.getString(property).isEmpty())
            .collect(Collectors.toList());

        if (!missingProperty.isEmpty()) {
            throw new IllegalArgumentException("Mandatory configuration properties are missing: " + missingProperty);
        }
    }

    @Override
    public String getDatabaseUser() {
        return configurationSource.getString("database_user")
            .orElseThrow(() -> new MissingConfigurationException("No value set for database_user"));
    }

    @Override
    public String getDatabasePassword() {
        return configurationSource.getString("database_password")
            .orElseThrow(() -> new MissingConfigurationException("No value set for database_password"));
    }

    @Override
    public String getDatabaseConnectionString() {
        return configurationSource.getString("database_connection")
            .orElseThrow(() -> new MissingConfigurationException("No value set for database_connection"));
    }

    @Override
    public int getCorePoolSize() {
        return configurationSource.getInteger("processor_corePoolSize")
            .orElseThrow(() -> new MissingConfigurationException("No value set for database_connection"));
    }

    @Override
    public int getMaximumPoolSize() {
        return configurationSource.getInteger("processor_maxPoolSize")
            .orElseThrow(() -> new MissingConfigurationException("No value set for database_connection"));
    }

    @Override
    public long getThreadKeepAliveInSeconds() {
        return configurationSource.getInteger("processor_threadKeepAlive")
            .orElseThrow(() -> new MissingConfigurationException("No value set for database_connection"));
    }

    @Override
    public int getBatchSize() {
        return configurationSource.getInteger("processor_batchSize")
            .orElseThrow(() -> new MissingConfigurationException("No value set for database_connection"));
    }

    @Override
    public Properties toProperties() {
        return configurationSource.toProperties();
    }
}
