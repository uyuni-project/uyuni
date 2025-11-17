/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.utils.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Creates the adapters for serializing and deserializing the record types
 */
public class RecordTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Class<? super T> rawType = typeToken.getRawType();
        if (!rawType.isRecord()) {
            return null;
        }

        return (TypeAdapter<T>) new RecordTypeAdapter<>(gson, rawType);
    }

    // Adapter for a specific record type
    private static class RecordTypeAdapter<T> extends TypeAdapter<T> {

        private final String recordName;

        private final RecordComponent[] components;

        private final Constructor<T> constructor;

        private final Map<String, Type> componentTypeMap;

        private final Gson gson;

        RecordTypeAdapter(Gson gsonIn, Class<T> recordClassIn) {
            this.gson = gsonIn;
            this.recordName = recordClassIn.getName();

            try {
                // Find all the record components
                this.components = recordClassIn.getRecordComponents();
                // Cache the type of each component
                this.componentTypeMap = Arrays.stream(components)
                    .collect(Collectors.toMap(RecordComponent::getName, RecordComponent::getGenericType));
                // Retrieve the canonical constructor
                this.constructor = recordClassIn.getDeclaredConstructor(
                    Arrays.stream(components).map(RecordComponent::getType).toArray(Class<?>[]::new)
                );
            }
            catch (ReflectiveOperationException | RuntimeException ex) {
                throw new IllegalStateException("Unable to collect the information of record " + recordName, ex);
            }
        }

        @Override
        public void write(JsonWriter jsonWriter, T value) throws IOException {
            jsonWriter.beginObject();

            for (RecordComponent field : components) {
                writeField(jsonWriter, value, field);
            }

            jsonWriter.endObject();
        }

        private void writeField(JsonWriter jsonWriter, T value, RecordComponent field) throws IOException {
            String fieldName = field.getName();

            try {
                Object fieldValue = field.getAccessor().invoke(value);

                // This suppression is unavoidable as we don't know at compile-time the generic type of the component
                @SuppressWarnings("unchecked")
                TypeToken<Object> tokenType = (TypeToken<Object>) TypeToken.get(field.getGenericType());
                TypeAdapter<Object> adapter = gson.getAdapter(tokenType);

                jsonWriter.name(fieldName);
                adapter.write(jsonWriter, fieldValue);
            }
            catch (ReflectiveOperationException | RuntimeException ex) {
                throw new IOException("Unable to process field '%s' of record %s".formatted(fieldName, recordName), ex);
            }
        }

        @Override
        public T read(JsonReader jsonReader) throws IOException {
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                return null;
            }

            Map<String, Object> componentValues = new HashMap<>();

            // Read the values from the json, matching the record component names
            jsonReader.beginObject();

            while (jsonReader.hasNext()) {
                String fieldName = jsonReader.nextName();
                if (componentTypeMap.containsKey(fieldName)) {
                    TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(componentTypeMap.get(fieldName)));
                    componentValues.put(fieldName, adapter.read(jsonReader));
                }
                else {
                    jsonReader.skipValue();
                }
            }

            jsonReader.endObject();

            // Convert the extract values into the constructor parameters
            Object[] constructorParameters = Arrays.stream(components)
                .map(component -> componentValues.get(component.getName()))
                .toArray();

            try {
                return constructor.newInstance(constructorParameters);
            }
            catch (ReflectiveOperationException e) {
                throw new IOException("Unable to create instance of record " + recordName, e);
            }
        }
    }
}
