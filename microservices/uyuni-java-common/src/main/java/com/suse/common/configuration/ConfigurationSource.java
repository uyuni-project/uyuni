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

package com.suse.common.configuration;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

public interface ConfigurationSource {

    /**
     * Retrieve the boolean value of a property.
     * @param property the property name
     * @return the boolean wrapped into an {@link Optional}
     */
    Optional<Boolean> getBoolean(String property);

    /**
     * Retrieve the boolean value of a required property.
     * @param property the property name
     * @return the boolean value
     * @throws MissingConfigurationException if the property is not available in this source
     */
    default Boolean requireBoolean(String property) {
        return getBoolean(property)
            .orElseThrow(() -> new MissingConfigurationException("No value set for " + property));
    }

    /**
     * Retrieve the string value of a property.
     * @param property the property name
     * @return the string wrapped into an {@link Optional}
     */
    Optional<String> getString(String property);

    /**
     * Retrieve the string value of a required property.
     * @param property the property name
     * @return the string value
     * @throws MissingConfigurationException if the property is not available in this source
     */
    default String requireString(String property) {
        return getString(property)
            .orElseThrow(() -> new MissingConfigurationException("No value set for " + property));
    }

    /**
     * Retrieve the integer value of a property.
     * @param property the property name
     * @return the integer wrapped into an {@link Optional}
     */
    Optional<Integer> getInteger(String property);

    /**
     * Retrieve the integer value of a required property.
     * @param property the property name
     * @return the integer value
     * @throws MissingConfigurationException if the property is not available in this source
     */
    default Integer requireInteger(String property) {
        return getInteger(property)
            .orElseThrow(() -> new MissingConfigurationException("No value set for " + property));
    }

    /**
     * Retrieve the long value of a property.
     * @param property the property name
     * @return the long wrapped into an {@link Optional}
     */
    Optional<Long> getLong(String property);

    /**
     * Retrieve the long value of a required property.
     * @param property the property name
     * @return the long value
     * @throws MissingConfigurationException if the property is not available in this source
     */
    default Long requireLong(String property) {
        return getLong(property)
            .orElseThrow(() -> new MissingConfigurationException("No value set for " + property));
    }

    /**
     * Retrieve the float value of a property.
     * @param property the property name
     * @return the float wrapped into an {@link Optional}
     */
    Optional<Float> getFloat(String property);

    /**
     * Retrieve the float value of a required property.
     * @param property the property name
     * @return the float value
     * @throws MissingConfigurationException if the property is not available in this source
     */
    default Float requireFloat(String property) {
        return getFloat(property)
            .orElseThrow(() -> new MissingConfigurationException("No value set for " + property));
    }

    /**
     * Retrieve the double value of a property.
     * @param property the property name
     * @return the double wrapped into an {@link Optional}
     */
    Optional<Double> getDouble(String property);

    /**
     * Retrieve the double value of a required property.
     * @param property the property name
     * @return the double value
     * @throws MissingConfigurationException if the property is not available in this source
     */
    default Double requireDouble(String property) {
        return getDouble(property)
            .orElseThrow(() -> new MissingConfigurationException("No value set for " + property));
    }

    /**
     * Retrieve the list value of a property.
     * @param property the property name
     * @param itemClass the type of item of the list.
     * @return the list wrapped into an {@link Optional}
     * @param <T> the type of item of the list
     */
    <T> Optional<List<T>> getList(String property, Class<T> itemClass);

    /**
     * Retrieve the list value of a required property.
     * @param property the property name
     * @param itemClass the type of item of the list.
     * @return the list value
     * @param <T> the type of item of the list
     * @throws MissingConfigurationException if the property is not available in this source
     */
    default <T> List<T> requireList(String property, Class<T> itemClass) {
        return getList(property, itemClass)
            .orElseThrow(() -> new MissingConfigurationException("No value set for " + property));
    }

    /**
     * Retrieves the names of all the available properties.
     * @return the set of property names
     */
    Set<String> getPropertyNames();

    /**
     * Converts this configuration source to a {@link Properties} object.
     * @return all the properties value stored into {@link Properties} object
     */
    Properties toProperties();
}
