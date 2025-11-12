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

package com.suse.utils.gson.test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.utils.gson.RecordTypeAdapterFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;

public class RecordTypeAdapterFactoryTest {

    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
        .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
        .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
        .serializeNulls()
        .create();


    @Test
    public void canSerializeSimpleRecord() {
        assertAll(
            () -> assertEquals(
                "{\"name\":\"Charlie\",\"age\":35,\"nationality\":\"Canadian\"}",
                GSON.toJson(new SimpleTestRecord("Charlie", 35, "Canadian"))
            ),
            () -> assertEquals(
                "{\"name\":\"Diana\",\"age\":28,\"nationality\":null}",
                GSON.toJson(new SimpleTestRecord("Diana", 28, null))
            ),
            () -> assertEquals(
                "{\"name\":\"\",\"age\":0,\"nationality\":null}",
                GSON.toJson(new SimpleTestRecord("", 0, null))
            )
        );
    }

    @Test
    public void canDeserializeSimpleRecord() {
        assertAll(
            () -> assertEquals(
                new SimpleTestRecord("Eve", 40, "Japanese"),
                GSON.fromJson("{\"name\":\"Eve\",\"age\":40,\"nationality\":\"Japanese\"}", SimpleTestRecord.class)
            ),
            () -> assertEquals(
                new SimpleTestRecord("Alice", 30, null),
                GSON.fromJson("{\"name\":\"Alice\",\"age\":30,\"nationality\":null}", SimpleTestRecord.class)
            ),
            () -> assertEquals(
                new SimpleTestRecord("", 21, null),
                GSON.fromJson("{\"name\":\"\",\"age\":21,\"nationality\":null}", SimpleTestRecord.class)
            )
        );
    }

    @Test
    public void canSerializeUsingOtherAdapters() {
        assertAll(
            () -> assertEquals(
                "{\"name\":\"Charlie\",\"dateOfBirth\":\"1982-06-27T00:00\",\"spouse\":\"Alice\"}",
                GSON.toJson(new CompositeTestRecord("Charlie", 1982, Month.JUNE, 27, "Alice"))
            ),
            () -> assertEquals(
                "{\"name\":\"Bob\",\"dateOfBirth\":\"1970-03-15T00:00\",\"spouse\":null}",
                GSON.toJson(new CompositeTestRecord("Bob", 1970, Month.MARCH, 15))
            )
        );
    }

    @Test
    public void canDeserializeUsingOtherAdapters() {
        assertAll(
            () -> assertEquals(
                new CompositeTestRecord("Charlie", 1982, Month.JUNE, 27, "Alice"),
                GSON.fromJson(
                    "{\"name\":\"Charlie\",\"dateOfBirth\":\"1982-06-27T00:00\",\"spouse\":\"Alice\"}",
                    CompositeTestRecord.class
                )
            ),
            () -> assertEquals(
                new CompositeTestRecord("Bob", 1970, Month.MARCH, 15),
                GSON.fromJson(
                    "{\"name\":\"Bob\",\"dateOfBirth\":\"1970-03-15T00:00\",\"spouse\":null}",
                    CompositeTestRecord.class
                )
            )
        );
    }

    public record SimpleTestRecord(String name, int age, String nationality) { }

    public record CompositeTestRecord(String name, LocalDateTime dateOfBirth, Optional<String> spouse) {

        public CompositeTestRecord(String nameIn, int yearIn, Month monthIn, int dayIn, String spouseIn) {
            this(nameIn, LocalDate.of(yearIn, monthIn, dayIn).atStartOfDay(), Optional.of(spouseIn));
        }

        public CompositeTestRecord(String nameIn, int yearIn, Month monthIn, int dayIn) {
            this(nameIn, LocalDate.of(yearIn, monthIn, dayIn).atStartOfDay(), Optional.empty());
        }
    }
}
