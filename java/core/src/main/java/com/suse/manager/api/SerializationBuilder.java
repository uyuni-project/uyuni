/*
 * Copyright (c) 2022 SUSE LLC
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
package com.suse.manager.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder class to conveniently populate a {@link SerializedApiResponse} object for API serialization
 */
public class SerializationBuilder {
    private final Map<String, Object> properties = new HashMap<>();

    /**
     * Adds a property to be serialized
     * @param key the key of the property
     * @param value the value of the property
     * @return a reference to this {@link SerializationBuilder} object to fulfill the "Builder" pattern
     */
    public SerializationBuilder add(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    /**
     * Creates a {@link SerializedApiResponse} object with the properties set by this builder
     * @return the {@link SerializedApiResponse} that contains the desired properties of the object to be serialized
     */
    public SerializedApiResponse build() {
        return new SerializedApiResponse(properties);
    }
}
