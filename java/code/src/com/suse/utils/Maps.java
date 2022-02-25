/*
 * Copyright (c) 2021 SUSE LLC
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
package com.suse.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;

public class Maps {

    private Maps() {
    }

    /**
     * Get the value from a nested map structure by a colon separated path.
     * E.g. key1:key2:key3 for a map with a depth of 3.
     * @param data the nested map
     * @param path the path
     * @return a value if available
     */
    public static Optional<Object> getValueByPath(Map<String, Object> data, String path) {
        String[] tokens = StringUtils.split(path, ":");
        Map<String, Object> current = data;
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            Object val = current.get(token);
            if (i == tokens.length - 1) {
                return Optional.ofNullable(val);
            }
            if (val == null) {
                return Optional.empty();
            }
            if (val instanceof Map) {
                current = (Map<String, Object>)val;
            }
            else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
