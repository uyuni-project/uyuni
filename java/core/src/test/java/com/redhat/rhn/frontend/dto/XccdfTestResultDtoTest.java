/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.frontend.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

class XccdfTestResultDtoTest {

    @Test
    void testCompletedDate() {
        XccdfTestResultDto dut = new XccdfTestResultDto();

        Date date1 = Date.from(LocalDate.of(2025, 12, 25).atStartOfDay(ZoneId.systemDefault()).toInstant());

        dut.setCompleted(date1);
        assertEquals(date1, dut.getCompleted());

        LocalDate date2 = dut.getCompleted().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        assertEquals(2025, date2.getYear());
        assertEquals(12, date2.getMonthValue());
        assertEquals(25, date2.getDayOfMonth());
    }

    @Test
    void testCompletedDate2() {
        XccdfTestResultDto dut = new XccdfTestResultDto();

        dut.setCompleted(Date.from(LocalDateTime.of(1968, 10, 22, 22, 10, 59)
                .atZone(ZoneId.systemDefault()).toInstant()));

        LocalDateTime cal = dut.getCompleted().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        assertEquals(1968, cal.getYear());
        assertEquals(10, cal.getMonthValue());
        assertEquals(22, cal.getDayOfMonth());
        assertEquals(22, cal.getHour());
        assertEquals(10, cal.getMinute());
        assertEquals(59, cal.getSecond());

    }
}
