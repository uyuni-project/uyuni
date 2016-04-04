package com.suse.manager.reactor.utils;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * Adapter to convert an ISO formatted string to ZonedDateTime
 */
public class ZonedDateTimeISOAdapter extends TypeAdapter<ZonedDateTime> {

    @Override
    public void write(JsonWriter jsonWriter, ZonedDateTime date) throws IOException {
        if (date == null) {
            throw new JsonParseException("null is not a valid value for ZonedDateTime");
        }
        else {
            jsonWriter.value(date.toString());
        }
    }

    @Override
    public ZonedDateTime read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            throw new JsonParseException("null is not a valid value for ZonedDateTime");
        }
        String dateStr = jsonReader.nextString();
        return ZonedDateTime.parse(dateStr);
    }
}
