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

import com.redhat.rhn.frontend.context.Context;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Parses a ISO 8601 DateTime String into a LocalDateTime
 */
public class LocalDateTimeISOAdapter extends TypeAdapter<LocalDateTime> {

    @Override
    public void write(JsonWriter jsonWriter, LocalDateTime date) throws IOException {
        if (date == null) {
            throw new JsonParseException("null is not a valid value for LocalDateTime");
        }
        else {
            jsonWriter.value(date.toString());
        }
    }

    @Override
    public LocalDateTime read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            throw new JsonParseException("null is not a valid value for LocalDateTime");
        }
        String dateStr = jsonReader.nextString();
        try {
            ZonedDateTime p = ZonedDateTime.parse(dateStr);
            ZoneId zoneId = Optional.ofNullable(Context.getCurrentContext().getTimezone())
                    .orElse(TimeZone.getDefault()).toZoneId();
            return LocalDateTime.ofInstant(p.toInstant(), zoneId);
        }
        catch (DateTimeParseException e) {
            return LocalDateTime.parse(dateStr);
        }
    }
}
