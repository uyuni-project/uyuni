/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.common.utilities;

import java.util.HashMap;
import java.util.Map;

public class JsonUtilities {

    private JsonUtilities() {
        // Utility classes should not have a public or default constructor
    }

    /**
     * Creates an empty JSON file
     *
     * @return an empty JSON
     */
    public static String createEmptyJson() {
        return "{}";
    }

    /**
     * Creates a JSON file with a first key-value pair
     *
     * @param key   to be added
     * @param value to be associated to the key
     * @return a created JSON file
     */
    public static String createJson(String key, String value) {
        return addToJson("", key, value);
    }

    /**
     * Adds a key-value pair to an existing JSON file
     *
     * @param json  file already existing
     * @param key   to be added
     * @param value to be associated to the key
     * @return the existing JSON file, with the added key-value pair
     */
    public static String addToJson(String json, String key, String value) {
        String body = json;
        if (body.startsWith("{")) {
            body = body.substring(1);
        }
        if (body.endsWith("}")) {
            body = body.substring(0, body.length() - 1);
        }

        if (body.isBlank()) {
            return "{\"%s\": \"%s\"}".formatted(key, value);
        }

        return "{%s, \"%s\": \"%s\"}".formatted(body, key, value);
    }

    /**
     * Decodes a simple JSON string into a key-value map
     *
     * @param simpleJsonString the string to decode
     * @return a decoded key-value map
     */
    public static Map<String, String> decodeSimpleJsonString(String simpleJsonString) {
        Map<String, String> result = new HashMap<>();

        String body = trimChars(simpleJsonString, "\n {}\n ");

        String[] splitArray = body.split("[:,]");

        for (int i = 0; i < splitArray.length; i += 2) {
            if (i < splitArray.length - 1) {
                decodeResult(result, splitArray[i], splitArray[i + 1]);
            }
        }

        return result;
    }

    private static void decodeResult(Map<String, String> result, String keyString, String valueString) {
        String key = trimChars(keyString, "\n \"");
        String value = trimChars(valueString, "\n \"");

        result.put(key, value);
    }

    private static String trimChars(String str, String toTrimChars) {
        for (int i = 0; i < toTrimChars.length(); i++) {
            String toTrim = String.valueOf(toTrimChars.charAt(i));
            while (str.startsWith(toTrim)) {
                str = str.substring(1);
            }

            while (str.endsWith(toTrim)) {
                str = str.substring(0, str.length() - toTrim.length());
            }
        }
        return str;
    }
}
