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

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * A class that collects properties of an API handler result object for serialization
 */
public class SerializedApiResponse {
    private final Map<String, Object> properties;

    /**
     * Creates an instance with a map of key-value pairs
     *
     * The {@link SerializedApiResponse} object contains the serializable properties of an object as key-value pairs
     * @param propertiesIn the property map
     */
    public SerializedApiResponse(Map<String, Object> propertiesIn) {
        this.properties = propertiesIn;
    }

    /**
     * Performs the given action for each property until all entries have been processed or the action throws an
     * exception.
     * @param action the action to be performed on each property
     */
    public void forEach(BiConsumer<String, Object> action) {
        Objects.requireNonNull(action);
        for (Map.Entry<String, Object> e : properties.entrySet()) {
            action.accept(e.getKey(), e.getValue());
        }
    }
}
