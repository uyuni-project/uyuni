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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.controllers;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.ParsePosition;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Date;

/**
 * Formats dates in the format expected by the toJSON() method in the
 * ECMAScript standard.
 *
 * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference
 * /Global_Objects/Date/toJSON
 *
 * This is needed because:
 *  - JSON does not dictate any date format, yet we need one
 *  - this format is the less evil option we have:
 *    - it conforms to ISO 8601
 *    - it is lexicographically sortable
 *    - it is legible
 *  - at the time of writing (2.3.1), gson does not support this out of the box.
 */
public final class ECMAScriptDateAdapter extends TypeAdapter<Date> {

    private static final DateTimeFormatter WRITE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    // accepts 1 to 9 fraction digits, toJSON() always sends 3
    private static final DateTimeFormatter READ_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
            .appendLiteral('Z')
            .toFormatter()
            .withZone(ZoneOffset.UTC);

    /** {@inheritDoc} */
    @Override
    public Date read(JsonReader in) throws IOException {
        try {
            if (in.peek().equals(JsonToken.NULL)) {
                in.nextNull();
                return null;
            }
            String date = in.nextString();
            return Date.from(Instant.from(READ_FORMATTER.parse(date, new ParsePosition(0))));
        }
        catch (DateTimeParseException e) {
            throw new JsonParseException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void write(JsonWriter out, Date date) throws IOException {
        if (date == null) {
            out.nullValue();
        }
        else {
            out.value(WRITE_FORMATTER.format(Instant.ofEpochMilli(date.getTime())));
        }
    }
}
