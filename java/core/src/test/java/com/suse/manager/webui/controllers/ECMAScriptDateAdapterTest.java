/*
 * Copyright (c) 2026 SUSE LLC
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

package com.suse.manager.webui.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

class ECMAScriptDateAdapterTest {

    private final Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
            .serializeNulls().create();

    private TimeZone originalDefault;

    @BeforeEach
    void setUp() {
        originalDefault = TimeZone.getDefault();
        // the format is always UTC, whatever the JVM time zone
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    }

    @AfterEach
    void tearDown() {
        TimeZone.setDefault(originalDefault);
    }

    @Test
    void testWrite() {
        Date date = Date.from(Instant.parse("2024-03-05T07:08:09.120Z"));

        assertEquals("\"2024-03-05T07:08:09.120Z\"", gson.toJson(date, Date.class));
        assertEquals("null", gson.toJson(null, Date.class));
    }

    @Test
    void testRead() {
        Date date = gson.fromJson("\"2024-03-05T07:08:09.123Z\"", Date.class);

        assertEquals(Instant.parse("2024-03-05T07:08:09.123Z"), date.toInstant());
        assertNull(gson.fromJson("null", Date.class));
    }

    @Test
    void testReadOtherFractionLengths() {
        assertEquals(Instant.parse("2024-03-05T07:08:09.500Z"),
                gson.fromJson("\"2024-03-05T07:08:09.5Z\"", Date.class).toInstant());
        assertEquals(Instant.parse("2024-03-05T07:08:09.123Z"),
                gson.fromJson("\"2024-03-05T07:08:09.123456Z\"", Date.class).toInstant());
    }

    @Test
    void testReadRejectsOtherFormats() {
        assertThrows(JsonParseException.class, () -> gson.fromJson("\"2024-03-05T07:08:09Z\"", Date.class));
        assertThrows(JsonParseException.class, () -> gson.fromJson("\"2024-03-05T07:08:09.123+01:00\"", Date.class));
    }
}
