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

/**
 * Factory class to create the different instances of {@link ConnectionManager}.
 */
public final class ConnectionManagerFactory {

    private ConnectionManagerFactory() {
        // Prevent instantiation
    }

    /**
     * Creates the default instance of the connection manager to access the application master database.
     * @return a {@link ConnectionManager} that connects to the main db
     */
    public static ConnectionManager defaultConnectionManager() {
        return new DefaultConnectionManager();
    }

    /**
     * Creates a connection manager instance to connect to the local reporting database.
     * @return a {@link ConnectionManager} that connects to the local reporting db
     */
    public static ConnectionManager localReportingConnectionManager() {
        return new ReportDbConnectionManager();
    }

    /**
     * Creates a connection manager instance that connect to the specified peripheral reporting database.
     * @param user     the username
     * @param password the password of the user
     * @param url      the connection url
     * @return a {@link ConnectionManager} that connects to the peripheral database using the specified parameters.
     */
    public static ConnectionManager reportingConnectionManager(String user, String password, String url) {
        return new ReportDbConnectionManager(user, password, url);
    }
}
