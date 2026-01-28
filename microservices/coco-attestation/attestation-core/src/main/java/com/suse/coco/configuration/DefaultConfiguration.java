/*
 * Copyright (c) 2024--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.coco.configuration;

import com.suse.common.configuration.ConfigurationSource;
import com.suse.common.configuration.EnvironmentConfigurationSource;
import com.suse.common.configuration.MultipleConfigurationSource;
import com.suse.common.configuration.ResourceConfigurationSource;

import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Default application configuration. Values are first read from the environment then from a property file
 * which defines the default values. A part for the mandatory properties, all the settings should have a value
 * inside the property file.
 */
public class DefaultConfiguration implements Configuration {

    // These are the only properties that do not have a default value in configuration-defaults.properties
    private static final Set<String> MANDATORY_PROPERTIES = Set.of(
        "database_user",
        "database_password"
    );

    // These properties need to be all defined if database_connection is not set
    private static final Set<String> CONNECTION_PROPERTIES = Set.of(
        "database_host",
        "database_port",
        "database_name"
    );

    private final ConfigurationSource configurationSource;

    /**
     * Default constructor
     */
    public DefaultConfiguration() {
        this(new MultipleConfigurationSource(List.of(
                new EnvironmentConfigurationSource(),
                new ResourceConfigurationSource("configuration-defaults.properties")
        )));
    }

    protected DefaultConfiguration(ConfigurationSource configSource) {
        configurationSource = configSource;
        List<String> missingProperty = MANDATORY_PROPERTIES.stream()
            .filter(property -> configurationSource.getString(property).isEmpty())
            .toList();

        if (!missingProperty.isEmpty()) {
            throw new IllegalArgumentException("Mandatory configuration properties are missing: " + missingProperty);
        }

        List<String> missingCnxProps = CONNECTION_PROPERTIES.stream()
                .filter(property ->  configurationSource.getString(property).isEmpty())
                .toList();
        if (configurationSource.getString("database_connection").isEmpty() && !missingCnxProps.isEmpty()) {
            throw new IllegalArgumentException("Missing either database connection or " + missingCnxProps);
        }
    }

    @Override
    public String getDatabaseUser() {
        return configurationSource.requireString("database_user");
    }

    @Override
    public String getDatabasePassword() {
        return configurationSource.requireString("database_password");
    }

    @Override
    public String getDatabaseConnectionString() {
        return configurationSource.requireString("database_connection");
    }

    @Override
    public String getDatabaseHostString() {
        return configurationSource.getString("database_host").orElse("");
    }

    @Override
    public int getDatabasePort() {
        return configurationSource.getInteger("database_port").orElse(5432);
    }

    @Override
    public String getDatabaseNameString() {
        return configurationSource.getString("database_name").orElse("");
    }

    @Override
    public int getCorePoolSize() {
        return configurationSource.requireInteger("processor_corePoolSize");
    }

    @Override
    public int getMaximumPoolSize() {
        return configurationSource.requireInteger("processor_maxPoolSize");
    }

    @Override
    public long getThreadKeepAliveInSeconds() {
        return configurationSource.requireInteger("processor_threadKeepAlive");
    }

    @Override
    public int getBatchSize() {
        return configurationSource.requireInteger("processor_batchSize");
    }

    @Override
    public Properties toProperties() {
        Properties properties = configurationSource.toProperties();

        String host = getDatabaseHostString();
        int port = getDatabasePort();
        String name = getDatabaseNameString();

        if (!host.isEmpty() && port != 0 && !name.isEmpty()) {
            String cnx = String.format("jdbc:postgresql://%s:%s/%s", host, port, name);
            properties.setProperty("database_connection", cnx);
        }
        return properties;
    }
}
