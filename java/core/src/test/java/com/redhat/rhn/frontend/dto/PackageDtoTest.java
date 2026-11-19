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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.frontend.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageTest;
import com.redhat.rhn.manager.task.TaskManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * PackageDtoTest
 */
public class PackageDtoTest extends BaseTestCaseWithUser {

    @Test
    public void testPackageDtoBuildTimeTimezoneIndependence() {
        java.util.TimeZone originalTz = java.util.TimeZone.getDefault();
        try {
            // Create a java.sql.Timestamp representing Sat May 16 12:05:39 2026 GMT
            // Epoch: 1778933139000 ms
            java.sql.Timestamp dbTimestamp = new java.sql.Timestamp(1778933139000L);

            // Set JVM default timezone to Europe/Berlin (CEST/UTC+2 on May 16, 2026)
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Europe/Berlin"));

            PackageDto dto = new PackageDto();
            dto.setBuildTime(dbTimestamp);

            // Assert that the parsed buildTime still represents the exact same GMT epoch
            assertEquals(1778933139000L, dto.getBuildTime().getTime());

            // Set JVM default timezone to America/New_York (EDT/UTC-4 on May 16, 2026)
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("America/New_York"));

            dto.setBuildTime(dbTimestamp);
            assertEquals(1778933139000L, dto.getBuildTime().getTime());
        }
        finally {
            // Always restore the original timezone
            java.util.TimeZone.setDefault(originalTz);
        }
    }

    @Test
    public void testPackageDtoRoundTripWithTimezones() throws Exception {
        java.util.TimeZone originalTz = java.util.TimeZone.getDefault();
        try {
            long expectedEpoch = 1778933139000L; // Sat May 16 12:05:39 2026 GMT
            String[] timezonesToTest = {"UTC", "Europe/Berlin", "America/New_York", "Asia/Tokyo"};

            for (String saveTzId : timezonesToTest) {
                // Set JVM default timezone for saving
                java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone(saveTzId));

                // 1. Create a channel and a package under the saving timezone
                Channel channel = ChannelFactoryTest.createTestChannel(user);
                Package pkg = PackageTest.createTestPackage(user.getOrg());
                pkg.setBuildTime(new Date(expectedEpoch));
                com.redhat.rhn.testing.TestUtils.saveAndFlush(pkg);

                channel.addPackage(pkg);
                ChannelFactory.save(channel);

                // Flush and clear Hibernate session to load fresh from the database
                com.redhat.rhn.common.hibernate.HibernateFactory.getSession().flush();
                com.redhat.rhn.common.hibernate.HibernateFactory.getSession().clear();

                // 2. Loop through various JVM default timezones for reading
                for (String readTzId : timezonesToTest) {
                    java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone(readTzId));

                    // Fetch the DTOs from the database
                    DataResult<PackageDto> dtos = TaskManager.getChannelPackageDtos(channel);
                    dtos.elaborate();
                    boolean found = false;
                    for (PackageDto dto : dtos) {
                        if (dto.getId().equals(pkg.getId())) {
                            found = true;
                            // Assert that the fetched package DTO buildTime
                            // preserves the correct GMT epoch milliseconds
                            assertEquals(expectedEpoch, dto.getBuildTime().getTime(),
                                    "Mismatch when saved in " + saveTzId + " and read in " + readTzId);
                        }
                    }
                    assertTrue(found, "Package DTO not found when saved in " + saveTzId + " and read in " + readTzId);
                }
            }
        }
        finally {
            // Restore the original default JVM timezone
            java.util.TimeZone.setDefault(originalTz);
        }
    }
}
