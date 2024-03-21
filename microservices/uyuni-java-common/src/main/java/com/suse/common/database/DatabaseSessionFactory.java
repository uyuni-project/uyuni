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

package com.suse.common.database;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.Properties;

/**
 * A wrapper to initialize and store the Mybatis {@link SqlSessionFactory}
 */
public class DatabaseSessionFactory {

    private static SqlSessionFactory sessionFactory = null;

    private DatabaseSessionFactory() {
        // Prevent instantiation
    }

    /**
     * Initialize the database session
     * @param configResource the path of the configuration file, processable by an invocation of
     * {@link ClassLoader#getSystemResourceAsStream(String)}
     * @param properties runtime configuration properties to customize the configuration
     */
    public static synchronized void initialize(String configResource, Properties properties) {
        try (InputStream stream = ClassLoader.getSystemResourceAsStream(configResource)) {
            sessionFactory = new SqlSessionFactoryBuilder().build(stream, properties);
        }
        catch (Exception ex) {
            throw new IllegalStateException("Unable to initialize database session factory", ex);
        }
    }

    /**
     * Check if the database session factory is initialized
     * @return true if the session factory has been initialized
     */
    public static boolean isInitialized() {
        return sessionFactory != null;
    }

    /**
     * Get the database session factory
     * @return the static instance
     */
    public static SqlSessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
