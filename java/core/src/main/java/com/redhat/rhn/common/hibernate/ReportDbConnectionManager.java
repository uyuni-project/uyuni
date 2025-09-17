/*
 * Copyright (c) 2021 SUSE LLC
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
package com.redhat.rhn.common.hibernate;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;

import java.util.Collections;
import java.util.List;
import java.util.Properties;


/**
 * Manages the lifecycle of Hibernate SessionFactory and associated
 * thread-scoped Hibernate sessions related to the reporting database.
 */
public class ReportDbConnectionManager extends AbstractConnectionManager {

    private final String dbUser;
    private final String dbPass;
    private final String dbConnectionUrl;

    /**
     * Construct the default report connection manager that connects to the database specified by the configuration
     * properties.
     */
    ReportDbConnectionManager() {
        this(
            Config.get().getString(ConfigDefaults.REPORT_DB_USER),
            Config.get().getString(ConfigDefaults.REPORT_DB_PASSWORD),
            ConfigDefaults.get().getReportingJdbcConnectionString()
        );
    }

    /**
     * Construct a report connection manager using the specified connection parameters
     * @param dbUserIn the database user
     * @param dbPasswordIn the database password
     * @param dbConnectionUrlIn the connection url to reach the database
     */
    ReportDbConnectionManager(String dbUserIn, String dbPasswordIn, String dbConnectionUrlIn) {
        super(Collections.emptySet());

        dbUser = dbUserIn;
        dbPass = dbPasswordIn;
        this.dbConnectionUrl = dbConnectionUrlIn;
    }

    @Override
    protected Properties getConfigurationProperties() {
        final Properties hibProperties = Config.get().getNamespaceProperties("reporting.hibernate", "hibernate");
        hibProperties.put("hibernate.connection.username", dbUser);
        hibProperties.put("hibernate.connection.password", dbPass);
        hibProperties.put("hibernate.connection.url", dbConnectionUrl);
        return hibProperties;
    }

    @Override
    protected List<Class<?>> getAnnotatedClasses() {
        return Collections.emptyList();
    }
}
