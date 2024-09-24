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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom {@link Map} deserializer that handles arbitrary numbers properly
 */
public class MapDeserializer implements JsonDeserializer<Map<String, Object>> {
    @Override
    public Map<String, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        Map<String, Object> map = new LinkedHashMap<>(obj.size());
        for (Map.Entry<String, JsonElement> el : obj.entrySet()) {
            String key = el.getKey();
            JsonElement val = el.getValue();
            if (val.isJsonArray()) {
                map.put(key, context.deserialize(val, List.class));
            }
            else if (val.isJsonPrimitive()) {
                JsonPrimitive prim = val.getAsJsonPrimitive();
                if (prim.isNumber()) {
                    Number num = null;
                    try {
                        num = Integer.parseInt(prim.getAsString());
                    }
                    catch (NumberFormatException eInt) {
                        try {
                            num = Long.parseLong(prim.getAsString());
                        }
                        catch (NumberFormatException eLong) {
                            try {
                                num = Double.parseDouble(prim.getAsString());
                            }
                            catch (NumberFormatException eDouble) {
                                // Not a valid number
                                map.put(key, prim.getAsString());
                            }
                        }
                    }
                    if (num != null) {
                        map.put(key, num);
                    }
                }
                else if (prim.isBoolean()) {
                    map.put(key, prim.getAsBoolean());
                }
                else if (prim.isString()) {
                    map.put(key, prim.getAsString());
                }
                else {
                    map.put(key, null);
                }

            }
            else if (val.isJsonObject()) {
                map.put(key, context.deserialize(val, Map.class));
            }
        }
        return map;
    }
}
