/*
 * Copyright (c) 2016 SUSE LLC
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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * TypeAdaptorFactory creating TypeAdapters for Optional
 */
public class OptionalTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <A> TypeAdapter<A> create(Gson gson, TypeToken<A> typeToken) {
        Type type = typeToken.getType();
        boolean isOptional = typeToken.getRawType() == Optional.class;
        boolean isParameterized = type instanceof ParameterizedType;
        if (isOptional && isParameterized) {
            Type elementType = ((ParameterizedType) type).getActualTypeArguments()[0];
            TypeAdapter<?> elementAdapter = gson.getAdapter(TypeToken.get(elementType));
            return (TypeAdapter<A>) optionalAdapter(elementAdapter);
        }
        else {
            return null;
        }
    }

    private <A> TypeAdapter<Optional<A>> optionalAdapter(TypeAdapter<A> innerAdapter) {
        return new TypeAdapter<>() {
            @Override
            public Optional<A> read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return Optional.empty();
                }
                JsonElement json = TypeAdapters.JSON_ELEMENT.read(in);
                if (json.isJsonObject()) {
                    JsonObject jsonObject = json.getAsJsonObject();
                    if (jsonObject.size() == 0) {
                        return Optional.empty();
                    }
                    else if (jsonObject.size() == 1 && jsonObject.has("value")) {
                        JsonElement value = jsonObject.get("value");
                        A result = innerAdapter.fromJsonTree(value);
                        return Optional.of(result);
                    }
                }
                try {
                    A value = innerAdapter.fromJsonTree(json);
                    return Optional.of(value);
                }
                catch (JsonSyntaxException e) {
                    /*
                     * Note : This is a workaround and it only exists because salt doesn't differentiate between a
                     * non-existent grain and a grain which exists but has value set to empty String.
                     *
                     * If an object is expected but instead empty string comes in then we return empty Optional.
                     */
                    if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString() &&
                            json.getAsString().isEmpty()) {
                        return Optional.empty();
                    }
                    throw e;
                }
            }

            @Override
            public void write(JsonWriter out, Optional<A> optional) throws IOException {
                if (optional.isPresent()) {
                    innerAdapter.write(out, optional.get());
                }
                else {
                    out.nullValue();
                }
            }
        };
    }
}
