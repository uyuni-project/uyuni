/*
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

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Convenience wrapper around a {@link Map}
 * to make it easier to convert values to various
 * data types.
 */
public class ValueMap {

    // Logger for this class
    private static final Logger LOG = LogManager.getLogger(ValueMap.class);

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
     * Check if ValueMap is empty
     * @return true if ValueMap is empty
     */
    public boolean isEmpty() {
        return this.valueMap.isEmpty();
    }

    /**
     * Check if ValueMap is present
     * @return true if ValueMap is present
     */
    public boolean isPresent() {
        return !this.valueMap.isEmpty();
    }

    /**
     * Get the value as string.
     * @param key the key
     * @return a value converted to string or an
     * empty string if the value is missing.
     */
    public String getValueAsString(String key) {
        return get(key).flatMap(ValueMap::toString).orElse("");
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
        return get(key).flatMap(ValueMap::toString);
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
     * Get a value as a boolean (if possible)
     * @param key the key
     * @return an {@link Optional} containing the boolean value
     * or an empty {@link Optional} if the value could not be converted.
     */
    public Optional<Boolean> getOptionalAsBoolean(String key) {
        return get(key).flatMap(this::toBoolean);
    }

    /**
     * Get a value as a collection (if possible)
     * @param key the key
     * @return an {@link Optional} containing the value as a {@link Collection}
     */
    public Optional<Collection<?>> getValueAsCollection(String key) {
        return get(key).filter(v -> v instanceof Collection).map(v -> (Collection<?>) v);
    }

    /**
     * Convert a value to a string. Takes special care of {@link Double}
     * and {@link Long} values.
     * @param value a value
     * @return an {@link Optional} containing a string or empty if
     * the value could not be converted.
     */
    public static Optional<String> toString(Object value) {
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
        else {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Value '{}' could not be converted to string.", ObjectUtils.toString(value));
            }
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
                LOG.warn("Error converting  '{}' to long", value, e);
                return Optional.empty();
            }
        }
        else {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Value '{}' could not be converted to long.", ObjectUtils.toString(value));
            }
            return Optional.empty();
        }
    }

    /**
     * Convert a value to a Boolean
     *
     * @param value a value
     * @return an {@link Optional} containing a boolean or empty if
     * the value could not be converted.
     */
    public Optional<Boolean> toBoolean(Object value) {
        if (value instanceof Boolean) {
            return Optional.of((Boolean) value);
        }
        else {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Value '{}' could not be converted to Boolean.", ObjectUtils.toString(value));
            }
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
