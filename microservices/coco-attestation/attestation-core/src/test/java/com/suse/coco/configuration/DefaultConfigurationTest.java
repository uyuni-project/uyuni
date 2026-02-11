/*
 * Copyright (c) 2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.coco.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.suse.common.configuration.BaseConfigurationSource;
import com.suse.common.configuration.ConfigurationSource;
import com.suse.common.configuration.MultipleConfigurationSource;
import com.suse.common.configuration.ResourceConfigurationSource;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultConfigurationTest {
    @Test
    void testConnectionConfig() {
        Map<String, String> config = Map.of(
                "database_user", "foo",
                "database_password", "bar",
                "database_connection", "jdbc:postgresql://myhost:5432/mydb");
        Configuration configuration = new DefaultConfiguration(buildFakeConfigurationSource(config));
        assertEquals("jdbc:postgresql://myhost:5432/mydb",
                configuration.toProperties().getProperty("database_connection"));
    }

    @Test
    void testSplitNoPortConfig() {
        Map<String, String> config = Map.of(
                "database_user", "foo",
                "database_password", "bar",
                "database_host", "myhost",
                "database_name", "mydb");

        Configuration configuration = new DefaultConfiguration(buildFakeConfigurationSource(config));
        assertEquals("jdbc:postgresql://myhost:5432/mydb",
                configuration.toProperties().getProperty("database_connection"));
    }

    @Test
    void testSplitPortConfig() {
        Map<String, String> config = Map.of(
                "database_user", "foo",
                "database_password", "bar",
                "database_host", "myhost",
                "database_port", "1234",
                "database_name", "mydb");
        Configuration configuration = new DefaultConfiguration(buildFakeConfigurationSource(config));
        assertEquals("jdbc:postgresql://myhost:1234/mydb",
                configuration.toProperties().getProperty("database_connection"));
    }

    @Test
    void testNoConfig() {
        Map<String, String> config = Map.of(
                "database_user", "foo",
                "database_password", "bar"
        );
        assertThrows(IllegalArgumentException.class, () -> {
            Configuration configuration = (new DefaultConfiguration(buildFakeConfigurationSource(config)));
            configuration.toProperties().getProperty("database_connection");
        });
    }

    ConfigurationSource buildFakeConfigurationSource(Map<String, String> rawValues) {
        ConfigurationSource fakeSource = new BaseConfigurationSource() {
            private Map<String, String> values = rawValues;

            @Override
            protected String getRawValue(String property) {
                return values.get(property);
            }

            @Override
            public Set<String> getPropertyNames() {
                return values.keySet();
            }
        };
        return new MultipleConfigurationSource(List.of(
                fakeSource,
                new ResourceConfigurationSource("configuration-defaults.properties")
        ));
    }
}
