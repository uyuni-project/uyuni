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

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Custom {@link List} deserializer that handles arbitrary numbers properly
 */
public class ListDeserializer implements JsonDeserializer<List<Object>> {
    @Override
    public List<Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        List<Object> list = new ArrayList<>();
        JsonArray arr = json.getAsJsonArray();
        for (JsonElement el : arr) {
            if (el.isJsonObject()) {
                list.add(context.deserialize(el, Map.class));
            }
            else if (el.isJsonArray()) {
                list.add(context.deserialize(el, List.class));
            }
            else if (el.isJsonPrimitive()) {
                JsonPrimitive prim = el.getAsJsonPrimitive();
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
                                list.add(prim.getAsString());
                            }
                        }
                    }
                    if (num != null) {
                        list.add(num);
                    }
                }
                else if (prim.isBoolean()) {
                    list.add(prim.getAsBoolean());
                }
                else if (prim.isString()) {
                    list.add(prim.getAsString());
                }
                else {
                    list.add(null);
                }
            }
        }
        return list;
    }
}
