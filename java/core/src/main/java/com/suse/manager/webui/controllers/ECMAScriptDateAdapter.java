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
package com.suse.manager.webui.controllers;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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

    /** Static format object. */
    private final SimpleDateFormat format;

    /**
     * Default constructor.
     */
    public ECMAScriptDateAdapter() {
        format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /** {@inheritDoc} */
    @Override
    public Date read(JsonReader in) throws IOException {
        try {
            if (in.peek().equals(JsonToken.NULL)) {
                in.nextNull();
                return null;
            }
            String date = in.nextString();
            return format.parse(date);
        }
        catch (ParseException e) {
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
            out.value(format.format(date));
        }
    }
}
