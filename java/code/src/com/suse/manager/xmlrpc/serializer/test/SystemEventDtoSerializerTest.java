/*
 * Copyright (c) 2021 SUSE LLC
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
package com.suse.manager.xmlrpc.serializer.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.frontend.dto.SystemEventDto;

import com.suse.manager.xmlrpc.serializer.SystemEventDtoSerializer;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import redstone.xmlrpc.XmlRpcSerializer;

public class SystemEventDtoSerializerTest {

    @Test
    public void testSerializeSystemEventDto() throws IOException {

        final SystemEventDtoSerializer serializer = new SystemEventDtoSerializer();

        final SystemEventDto dto = new SystemEventDto();

        dto.setId(25L);
        dto.setHistoryType(ActionFactory.TYPE_HARDWARE_REFRESH_LIST.getLabel());
        dto.setHistoryTypeName(ActionFactory.TYPE_HARDWARE_REFRESH_LIST.getName());
        dto.setHistoryStatus(ActionFactory.STATUS_COMPLETED.getName());
        dto.setSummary("Hardware List Refresh scheduled by (none)");
        dto.setCompleted(Date.from(LocalDateTime.of(2021, 10, 5, 17, 0)
                .atZone(ZoneOffset.systemDefault())
                .toInstant()));

        final Writer output = new StringWriter();

        serializer.serialize(dto, output, new XmlRpcSerializer());

        final String xml = output.toString();

        assertTrue(xml.contains("<name>id</name>"));
        assertTrue(xml.contains("<i4>25</i4>"));

        assertTrue(xml.contains("<name>history_type</name>"));
        assertTrue(xml.contains("<string>Hardware List Refresh</string>"));

        assertTrue(xml.contains("<name>status</name>"));
        assertTrue(xml.contains("<string>Completed</string>"));

        assertTrue(xml.contains("<name>summary</name>"));
        assertTrue(xml.contains("<string>Hardware List Refresh scheduled by (none)</string>"));

        assertTrue(xml.contains("<name>completed</name>"));
        assertTrue(xml.contains("<dateTime.iso8601>20211005T17:00:00</dateTime.iso8601>"));
    }

}
