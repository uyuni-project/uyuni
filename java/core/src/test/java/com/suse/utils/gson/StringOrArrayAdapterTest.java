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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

class StringOrArrayAdapterTest {

    private StringOrArrayAdapter adapter;

    @BeforeEach
    void setup() {
        adapter = new StringOrArrayAdapter();
    }

    @Test
    void canReadSingleString() throws Exception {
        JsonReader reader = new JsonReader(new StringReader("\"alpha\""));

        assertEquals(List.of("alpha"), adapter.read(reader));
    }

    @Test
    void canReadStringArray() throws Exception {
        JsonReader reader = new JsonReader(new StringReader("[\"alpha\",\"beta\"]"));

        assertEquals(List.of("alpha", "beta"), adapter.read(reader));
    }

    @Test
    void canReadNull() throws Exception {
        JsonReader reader = new JsonReader(new StringReader("null"));

        assertEquals(List.of(), adapter.read(reader));
    }

    @Test
    void canReadEmptyArray() throws Exception {
        JsonReader reader = new JsonReader(new StringReader("[]"));

        assertEquals(List.of(), adapter.read(reader));
    }

    @Test
    void canWriteStringListAsArray() throws Exception {
        StringWriter out = new StringWriter();
        JsonWriter writer = new JsonWriter(out);

        adapter.write(writer, List.of("alpha", "beta"));
        writer.close();

        assertEquals("[\"alpha\",\"beta\"]", out.toString());
    }

    @Test
    void rejectsNonStringArrayEntries() {
        JsonReader reader = new JsonReader(new StringReader("[\"alpha\", 1]"));

        assertThrows(JsonSyntaxException.class, () -> adapter.read(reader));
    }
}
