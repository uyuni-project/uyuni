/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.reactor.utils;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Convenience wrapper around a {@link Map}
 * to make it easier to convert values to various
 * data types.
 */
public class ValueMap {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(ValueMap.class);

    private Map<String, ?> valueMap;

    /**
     * Create a ValueMap from a {@link Map}
     * @param valueMapIn the {@link Map} to wrap
     */
    public ValueMap(Map<String, ?> valueMapIn) {
        this.valueMap = valueMapIn;
    }

    /**
     * Create an empty ValueMap
     */
    public ValueMap() {
        this.valueMap = Collections.emptyMap();
    }

    /**
     * Get the value as string.
     * @param key the key
     * @return a value converted to string or an
     * empty string if the value is missing.
     */
    public String getValueAsString(String key) {
        return get(key).flatMap(this::toString).orElse("");
    }

    /**
     * Get the value as string and chop of chars that exceed maxLength.
     * @param key the key
     * @param maxLength max allowed length
     * @return a value converted to string or an
     * empty string if the value is missing.
     */
    public String getValueAsString(String key, int maxLength) {
        return get(key).map(o -> StringUtils.substring(o.toString(), 0, maxLength))
                .orElse("");
    }

    /**
     * Get an optional string.
     * @param key the key
     * @return an {@link Optional} containing the values
     * as a string
     */
    public Optional<String> getOptionalAsString(String key) {
        return get(key).flatMap(this::toString);
    }

    /**
     * Get a value as a long (if possible)
     * @param key the key
     * @return an {@link Optional} containing the long value
     * or an empty {@link Optional} if the value could not be converted.
     */
    public Optional<Long> getValueAsLong(String key) {
        return get(key).flatMap(this::toLong);
    }

    /**
     * Convert a value to a string. Takes special care of {@link Double}
     * and {@link Long} values.
     * @param value a value
     * @return an {@link Optional} containing a string or empty if
     * the value could not be converted.
     */
    public Optional<String> toString(Object value) {
        return valueToString(0, value);
    }

    private Optional<String> valueToString(int depth, Object value) {
        if (depth > 10) {
            return Optional.empty();
        }
        if (value instanceof Double) {
            DecimalFormat formater = new DecimalFormat("#.##");
            return Optional.of(formater.format((double)value));
        }
        else if (value instanceof Long) {
            return Optional.of(Long.toString((long)value));
        }
        else if (value instanceof String) {
            return Optional.of((String)value);
        }
        if (value instanceof Collection) {
            return Optional.of(((Collection<?>)value).stream()
                    .map(v -> valueToString(depth + 1, v))
                    .map(opt -> opt.orElse(""))
                    .collect(Collectors.joining(" ")));
        }
        else {
            LOG.warn("Value '" + ObjectUtils.toString(value) +
                    "' could not be converted to string.");
            return Optional.empty();
        }
    }

    /**
     * Convert a value to a {@link Long} (if possible).
     * @param value a value
     * @return an {@link Optional} containing a {@link Long}  or empty if
     * the value could not be converted.
     */
    public Optional<Long> toLong(Object value) {
        if (value instanceof Double) {
            return Optional.of(((Double)value).longValue());
        }
        else if (value instanceof Long) {
            return Optional.of((Long)value);
        }
        else if (value instanceof Integer) {
            return Optional.of(((Integer)value).longValue());
        }
        else if (value instanceof String) {
            try {
                return Optional.of(Long.parseLong((String) value));
            }
            catch (NumberFormatException e) {
                LOG.warn("Error converting  '" + value + "' to long", e);
                return Optional.empty();
            }
        }
        else {
            LOG.warn("Value '" + ObjectUtils.toString(value) +
                    "' could not be converted to long.");
            return Optional.empty();
        }
    }

    /**
     * Get the value for a key as an {@link Optional}
     * @param key the key
     * @return an {@link Optional} wrapping the value
     */
    public Optional<Object> get(String key) {
        return Optional.ofNullable(valueMap.get(key));
    }

    /**
     * Get the value for a key as an {@link Optional} of ValueMap
     * @param key the key
     * @return a ValueMap
     */
    @SuppressWarnings("unchecked")
    public Optional<ValueMap> getMap(String key) {
        return get(key).map(value -> new ValueMap((Map<String, Object>) value));
    }

}
