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

import java.util.Properties;

/**
 * Application configuration
 */
public interface Configuration {

    /**
     * Retrieve the database user
     * @return the user to access the database
     */
    String getDatabaseUser();

    /**
     * Retrieves the database password
     * @return the unencrypted password to access the database
     */
    String getDatabasePassword();

    /**
     * Retrieves the connection string
     * @return the jdbc string to connect to the database
     */
    String getDatabaseConnectionString();

    /**
     * Retrieves the number of attestation results to process at a time.
     * @return the maximum number of attestation results to process.
     */
    int getBatchSize();

    /**
     * Retrieves the number of core threads for the queue processor
     * @return the number of core threads
     */
    int getCorePoolSize();

    /**
     * Retrieves the maximum number of threads for the queue processor
     * @return the maximum number of threads
     */
    int getMaximumPoolSize();

    /**
     * The number of seconds a thread will remain alive even without work to do.
     * @return the number of seconds a thread will be kept idle.
     */
    long getThreadKeepAliveInSeconds();

    /**
     * Convert this configuration to a Properties
     * @return a {@link Properties} representing the configuration.
     */
    Properties toProperties();

}
