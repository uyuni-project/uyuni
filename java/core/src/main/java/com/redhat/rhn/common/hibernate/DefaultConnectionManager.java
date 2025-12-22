/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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

import java.util.List;
import java.util.Properties;

/**
 * Manages the lifecycle of Hibernate SessionFactory and associated
 * thread-scoped Hibernate sessions.
 */
class DefaultConnectionManager extends AbstractConnectionManager {

    DefaultConnectionManager() {
    }

    @Override
    protected Properties getConfigurationProperties() {
        final Properties hibProperties = Config.get().getNamespaceProperties("hibernate");
        hibProperties.put("hibernate.connection.username", Config.get().getString(ConfigDefaults.DB_USER));
        hibProperties.put("hibernate.connection.password", Config.get().getString(ConfigDefaults.DB_PASSWORD));
        hibProperties.put("hibernate.connection.url", ConfigDefaults.get().getJdbcConnectionString());
//        hibProperties.put("hibernate.cache.ehcache.missing_cache_strategy", "create");
//        hibProperties.put("hibernate.cache.use_second_level_cache", "false");
//        hibProperties.put("hibernate.bytecode.use_reflection_optimizer", "false");
        return hibProperties;
    }

    @Override
    protected List<Class<?>> getAnnotatedClasses() {
        return AnnotationRegistry.getAnnotationClasses();
    }
}
