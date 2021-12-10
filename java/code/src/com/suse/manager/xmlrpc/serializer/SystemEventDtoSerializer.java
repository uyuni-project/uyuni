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
package com.suse.manager.xmlrpc.serializer;

import com.redhat.rhn.frontend.dto.SystemEventDto;
import com.redhat.rhn.frontend.xmlrpc.serializer.RhnXmlRpcCustomSerializer;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

import java.io.IOException;
import java.io.Writer;
import java.util.Optional;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 *
 * Serializer for {@link SystemEventDto}
 *
 * @xmlrpc.doc
 *  #struct_begin("System Event")
 *      #prop_desc("int", "id", "Identifier of the event")
 *      #prop_desc("string", "history_type", "Type of history event")
 *      #prop_desc("string", "status", "Status of the event")
 *      #prop_desc("string", "summary", "Summary of the event")
 *      #prop_desc("dateTime.iso8601", "completed", "Date that the event occurred")
 *  #struct_end()
 *
 */
public class SystemEventDtoSerializer extends RhnXmlRpcCustomSerializer {

    @Override
    public Class<SystemEventDto> getSupportedClass() {
        return SystemEventDto.class;

    }

    @Override
    protected void doSerialize(Object value, Writer output, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {

        SerializerHelper helper = new SerializerHelper(serializer);
        SystemEventDto event = (SystemEventDto) value;

        helper.add("id", event.getId());
        helper.add("history_type", Optional.ofNullable(event.getHistoryTypeName()).orElse("History Event"));
        helper.add("status", event.getHistoryStatus());
        helper.add("summary", event.getSummary());
        helper.add("completed", event.getCompleted());

        helper.writeTo(output);
    }
}
