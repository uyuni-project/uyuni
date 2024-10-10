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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BaseConfigurationSource implements ConfigurationSource {
    private static final List<String> TRUE_VALUES = List.of("1", "y", "true", "yes", "on");

    @Override
    public Optional<String> getString(String property) {
        return Optional.ofNullable(getRawValue(property))
            .map(String::trim)
            .filter(value -> !value.isEmpty());
    }

    @Override
    public Optional<Integer> getInteger(String property) {
        return getString(property).map(value -> convertTo(value, Integer.class));
    }

    @Override
    public Optional<Long> getLong(String property) {
        return getString(property).map(value -> convertTo(value, Long.class));
    }
    @Override
    public Optional<Float> getFloat(String property) {
        return getString(property).map(value -> convertTo(value, Float.class));
    }

    @Override
    public Optional<Double> getDouble(String property) {
        return getString(property).map(value -> convertTo(value, Double.class));
    }

    @Override
    public Optional<Boolean> getBoolean(String property) {
        return getString(property).map(value -> convertTo(value, Boolean.class));
    }

    @Override
    public <T> Optional<List<T>> getList(String property, Class<T> itemClass) {
        return getString(property)
            .stream()
            .flatMap(value -> Arrays.stream(value.split(",")))
            .map(item -> convertTo(item, itemClass))
            .collect(Collectors.collectingAndThen(Collectors.toList(), Optional::of))
            .filter(values -> !values.isEmpty());
    }

    @Override
    public Properties toProperties() {
        Set<String> propertyNames = getPropertyNames();
        Properties properties = new Properties(propertyNames.size());

        propertyNames.forEach(property ->
            properties.put(property, getRawValue(property))
        );

        return properties;
    }

    protected abstract String getRawValue(String property);

    protected <T> T convertTo(String value, Class<T> valueClass) {
        Object convertedValue;

        if (valueClass.equals(String.class)) {
            convertedValue = value;
        }
        else if (valueClass.equals(Integer.class)) {
            convertedValue = Integer.valueOf(value);
        }
        else if (valueClass.equals(Long.class)) {
            convertedValue = Long.valueOf(value);
        }
        else if (valueClass.equals(Float.class)) {
            convertedValue = Float.valueOf(value);
        }
        else if (valueClass.equals(Double.class)) {
            convertedValue = Double.valueOf(value);
        }
        else if (valueClass.equals(Boolean.class)) {
            convertedValue = TRUE_VALUES.contains(value.toLowerCase());
        }
        else {
            throw new IllegalStateException("Unsupported class type " + valueClass.getName());
        }

        return valueClass.cast(convertedValue);
    }
}
