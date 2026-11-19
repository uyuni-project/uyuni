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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

class SnapshotTagDtoTest {

    @Test
    void testSetCreatedParsesDbFormat() {
        SnapshotTagDto dto = new SnapshotTagDto();

        dto.setCreated("2024-03-05 07:08:09");

        Date expected = Date.from(LocalDateTime.of(2024, 3, 5, 7, 8, 9).atZone(ZoneId.systemDefault()).toInstant());
        assertEquals(expected, dto.getCreated());
    }

    @Test
    void testSetCreatedAcceptsNull() {
        SnapshotTagDto dto = new SnapshotTagDto();
        dto.setCreated("2024-03-05 07:08:09");

        dto.setCreated(null);

        assertNull(dto.getCreated());
    }

    @Test
    void testSetCreatedRejectsOtherFormats() {
        SnapshotTagDto dto = new SnapshotTagDto();

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> dto.setCreated("05/03/2024"));

        assertEquals("lastCheckin must be of the: [yyyy-MM-dd HH:mm:ss] it was: 05/03/2024", e.getMessage());
    }
}
