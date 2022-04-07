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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.action.ActionFactory;

import com.suse.manager.xmlrpc.dto.SystemEventDetailsDto;
import com.suse.manager.xmlrpc.serializer.SystemEventDetailsDtoSerializer;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import redstone.xmlrpc.XmlRpcSerializer;

public class SystemEventDetailsDtoSerializerTest {

    /**
     * Verify if the serialization to xml contains the expected tags
     */
    @Test
    public void testSerializeSystemEventDetailDto() throws IOException {

        final SystemEventDetailsDtoSerializer serializer = new SystemEventDetailsDtoSerializer();

        final SystemEventDetailsDto dto = new SystemEventDetailsDto();

        dto.setId(25L);
        dto.setHistoryType(ActionFactory.TYPE_HARDWARE_REFRESH_LIST.getLabel());
        dto.setHistoryTypeName(ActionFactory.TYPE_HARDWARE_REFRESH_LIST.getName());
        dto.setHistoryStatus(ActionFactory.STATUS_COMPLETED.getName());
        dto.setSummary("Hardware List Refresh scheduled by (none)");
        dto.setCreated(Date.from(LocalDateTime.of(2021, 10, 5, 16, 55)
                .atZone(ZoneOffset.systemDefault())
                .toInstant()));
        dto.setPickedUp(Date.from(LocalDateTime.of(2021, 10, 5, 16, 58)
                .atZone(ZoneOffset.systemDefault())
                .toInstant()));
        dto.setCompleted(Date.from(LocalDateTime.of(2021, 10, 5, 17, 0)
                .atZone(ZoneOffset.systemDefault())
                .toInstant()));
        dto.setEarliestAction(Date.from(LocalDateTime.of(2021, 10, 5, 16, 56)
                .atZone(ZoneOffset.systemDefault())
                .toInstant()));

        dto.setResultMsg("done");
        dto.setResultCode(0L);

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

        assertTrue(xml.contains("<name>created</name>"));
        assertTrue(xml.contains("<dateTime.iso8601>20211005T16:55:00</dateTime.iso8601>"));

        assertTrue(xml.contains("<name>picked_up</name>"));
        assertTrue(xml.contains("<dateTime.iso8601>20211005T16:58:00</dateTime.iso8601>"));

        assertTrue(xml.contains("<name>completed</name>"));
        assertTrue(xml.contains("<dateTime.iso8601>20211005T17:00:00</dateTime.iso8601>"));

        assertTrue(xml.contains("<name>earliest_action</name>"));
        assertTrue(xml.contains("<dateTime.iso8601>20211005T16:56:00</dateTime.iso8601>"));

        assertTrue(xml.contains("<name>result_msg</name>"));
        assertTrue(xml.contains("<string>done</string>"));

        assertTrue(xml.contains("<name>result_code</name>"));
        assertTrue(xml.contains("<i4>0</i4>"));

        // No additional info should be present
        assertFalse(xml.contains("<name>additional_info</name>"));
    }

}
