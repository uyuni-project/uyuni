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

package com.suse.utils.gson;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapts a JSON payload that may be either a single string or an array of strings into a
 * {@link List} of strings.
 * <p>
 * When reading, the adapter accepts either a single JSON string, an array of strings, or
 * {@code null}. Both valid input forms are normalized to a list. When writing, the value is
 * serialized as a JSON array.
 * <p>
 * Examples of accepted input are {@code "value"}, {@code ["value1", "value2"]}, and
 * {@code null}. Any non-string element inside an array triggers a deserialization failure.
 */
public class StringOrArrayAdapter extends TypeAdapter<List<String>> {

    @Override
    public void write(JsonWriter out, List<String> value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        out.beginArray();
        for (String s : value) {
            out.value(s);
        }
        out.endArray();
    }

    @Override
    public List<String> read(JsonReader in) throws IOException {
        JsonToken peek = in.peek();
        List<String> result = new ArrayList<>();

        switch (peek) {
            case NULL:
                in.nextNull();
                return result;

            case STRING:
                result.add(in.nextString());
                return result;

            case BEGIN_ARRAY:
                in.beginArray();
                while (in.hasNext()) {
                    JsonToken elementToken = in.peek();
                    if (elementToken != JsonToken.STRING) {
                        throw new JsonSyntaxException("Expected string element in array, but was " + elementToken);
                    }

                    result.add(in.nextString());
                }
                in.endArray();
                return result;

            default:
                throw new JsonSyntaxException("Expected String or Array, but was " + peek);
        }
    }
}
