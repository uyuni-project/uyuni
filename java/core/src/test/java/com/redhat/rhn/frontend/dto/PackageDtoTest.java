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

import com.redhat.rhn.common.RhnRuntimeException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

class PackageDtoTest {

    private TimeZone originalDefault;

    @BeforeEach
    void setUp() {
        originalDefault = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
    }

    @AfterEach
    void tearDown() {
        TimeZone.setDefault(originalDefault);
    }

    @Test
    void testSetBuildTimeReadsDatabaseWallClockAsGmt() {
        PackageDto dto = new PackageDto();

        // the timestamp column holds the GMT wall clock, JDBC reads it in the JVM time zone
        dto.setBuildTime(Timestamp.valueOf("2024-03-05 07:08:09"));

        assertEquals(Instant.parse("2024-03-05T07:08:09Z"), dto.getBuildTime().toInstant());
    }

    @Test
    void testSetBuildTimeKeepsMilliseconds() {
        PackageDto dto = new PackageDto();

        dto.setBuildTime(Timestamp.valueOf("2024-03-05 07:08:09.123"));

        assertEquals(Instant.parse("2024-03-05T07:08:09.123Z"), dto.getBuildTime().toInstant());
    }

    @Test
    void testSetBuildTimeAcceptsNull() {
        PackageDto dto = new PackageDto();
        dto.setBuildTime(Timestamp.valueOf("2024-03-05 07:08:09"));

        dto.setBuildTime(null);

        assertNull(dto.getBuildTime());
    }

    @Test
    void testSetBuildTimeRejectsNonTimestampDate() {
        PackageDto dto = new PackageDto();
        Date notATimestamp = new Date(Timestamp.valueOf("2024-03-05 07:08:09").getTime());

        assertThrows(RhnRuntimeException.class, () -> dto.setBuildTime(notATimestamp));
    }

    @Test
    void testSetBuildTimeKeepsSingleDigitFraction() {
        PackageDto dto = new PackageDto();

        dto.setBuildTime(Timestamp.valueOf("2024-03-05 07:08:09.5"));

        assertEquals(Instant.parse("2024-03-05T07:08:09.500Z"), dto.getBuildTime().toInstant());
    }
}
