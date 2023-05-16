/*
 * Copyright (c) 2023 SUSE LLC
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
package com.redhat.rhn.frontend.xmlrpc.util;

import java.util.HashMap;
import java.util.Map;

/**
 * PillarUtils - converts "size" for xml-rpc
 */
public class PillarUtils {
    private PillarUtils() {
    }

    /**
     * Convert all "size" entries in the pillar from Long to String
     * @param pillar the input pillar
     * @return processed copy of the input
     */
    public static Map<String, Object> convertSizeToString(Map<String, Object> pillar) {
        Map<String, Object> copy = new HashMap<>();
        for (String key : pillar.keySet()) {
            Object value = pillar.get(key);
            if (key.equals("size") && (value instanceof Integer || value instanceof Long)) {
                value = String.valueOf(value);
            }
            else if (value instanceof Map) {
                value = convertSizeToString((Map<String, Object>) value);
            }
            copy.put(key, value);
        }
        return copy;
    }

    /**
     * Convert all "size" entries in the pillar from String to Long
     * @param pillar the input pillar
     * @return processed copy of the input
     */
    public static Map<String, Object> convertSizeToLong(Map<String, Object> pillar) {
        Map<String, Object> copy = new HashMap<>();
        for (String key : pillar.keySet()) {
            Object value = pillar.get(key);
            if (key.equals("size") && value instanceof String) {
                value = Long.parseLong((String)value);
            }
            else if (value instanceof Map) {
                value = convertSizeToLong((Map<String, Object>) value);
            }
            copy.put(key, value);
        }
        return copy;
    }
}
