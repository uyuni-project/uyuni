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

package com.redhat.rhn.frontend.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

class HistoryEventTest {

    @Test
    void testSetCompletedFromString() {
        HistoryEvent event = new HistoryEvent();

        event.setCompleted("2024-03-05 07:08:09");
        assertEquals(localDate(2024, 3, 5, 7, 8, 9), event.getCompleted());

        // java.sql.Timestamp.toString() format, reaches this setter when the datasource converts a raw timestamp
        event.setCompleted("2024-03-05 00:08:09.123456");
        assertEquals(localDate(2024, 3, 5, 0, 8, 9), event.getCompleted());
    }

    @Test
    void testSetCompletedFromNullString() {
        HistoryEvent event = new HistoryEvent();
        event.setCompleted("2024-03-05 07:08:09");

        event.setCompleted((String) null);

        assertNull(event.getCompleted());
    }

    @Test
    void testSetCompletedFromInvalidStringKeepsPreviousValue() {
        HistoryEvent event = new HistoryEvent();
        event.setCompleted("2024-03-05 07:08:09");

        event.setCompleted("05/03/2024");

        assertEquals(localDate(2024, 3, 5, 7, 8, 9), event.getCompleted());
    }

    private static Date localDate(int year, int month, int day, int hour, int minute, int second) {
        LocalDateTime local = LocalDateTime.of(year, month, day, hour, minute, second);
        return Date.from(local.atZone(ZoneId.systemDefault()).toInstant());
    }
}
