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

package com.suse.common.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A configuration source that's retrieving the values from a resource file
 */
public class ResourceConfigurationSource extends BaseConfigurationSource {

    private static final Logger LOGGER = LogManager.getLogger(ResourceConfigurationSource.class);

    private final Properties properties;

    /**
     * Default constructor
     * @param resourceName the name of the resource, usable by the system class loader to locate it.
     */
    public ResourceConfigurationSource(String resourceName) {
        properties = new Properties();

        try (InputStream inputStream = ClassLoader.getSystemResourceAsStream(resourceName)) {
            properties.load(inputStream);
        }
        catch (Exception ex) {
            LOGGER.error("Unable to load configuration from resource {}", resourceName, ex);
            properties.clear();
        }
    }

    @Override
    protected String getRawValue(String property) {
        return properties.getProperty(property);
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.keySet().stream()
            .map(Objects::toString)
            .collect(Collectors.toUnmodifiableSet());
    }
}
