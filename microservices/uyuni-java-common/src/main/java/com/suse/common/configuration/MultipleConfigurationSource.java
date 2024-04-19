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

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MultipleConfigurationSource implements ConfigurationSource {

    // Using a deque since we need to traverse it in both directions
    private final Deque<ConfigurationSource> configurationSources;

    /**
     * Build a combined configuration source from the combination of all the ones specified in the list.
     * @param configurationSourcesIn the list of sources. The order of the sources will be respected when retrieving
     * the properties.
     */
    public MultipleConfigurationSource(List<ConfigurationSource> configurationSourcesIn) {
        this.configurationSources = new LinkedList<>(configurationSourcesIn);
    }

    @Override
    public Optional<Boolean> getBoolean(String property) {
        return getFirstValue(property, ConfigurationSource::getBoolean);
    }

    @Override
    public Optional<String> getString(String property) {
        return getFirstValue(property, ConfigurationSource::getString);
    }

    @Override
    public Optional<Integer> getInteger(String property) {
        return getFirstValue(property, ConfigurationSource::getInteger);
    }

    @Override
    public Optional<Long> getLong(String property) {
        return getFirstValue(property, ConfigurationSource::getLong);
    }

    @Override
    public Optional<Float> getFloat(String property) {
        return getFirstValue(property, ConfigurationSource::getFloat);
    }

    @Override
    public Optional<Double> getDouble(String property) {
        return getFirstValue(property, ConfigurationSource::getDouble);
    }

    @Override
    public <T> Optional<List<T>> getList(String property, Class<T> itemClass) {
        return configurationSources.stream()
            .flatMap(source -> source.getList(property, itemClass).stream())
            .findFirst();
    }

    private <T> Optional<T> getFirstValue(String property,
                                          BiFunction<ConfigurationSource, String, Optional<T>> propertyRetriever) {
        return configurationSources.stream()
            .flatMap(source -> propertyRetriever.apply(source, property).stream())
            .findFirst();
    }

    @Override
    public Set<String> getPropertyNames() {
        // The set of property names won't be ordered
        return configurationSources.stream()
            .flatMap(source -> source.getPropertyNames().stream())
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Properties toProperties() {
        Properties combineProperties = new Properties();

        // Iterate the source in the opposite order, so the values from the firsts take precedence over the lasts
        Iterable<ConfigurationSource> sources = configurationSources::descendingIterator;
        StreamSupport.stream(sources.spliterator(), false)
            .map(source -> source.toProperties())
            .forEach(properties -> combineProperties.putAll(properties));

        return combineProperties;
    }
}
