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

import com.redhat.rhn.frontend.xmlrpc.serializer.RhnXmlRpcCustomSerializer;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

import com.suse.manager.xmlrpc.dto.SystemEventDetailsDto;

import java.io.IOException;
import java.io.Writer;
import java.util.Optional;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 *
 * Serializer for {@link SystemEventDetailsDto}
 *
 * @xmlrpc.doc
 *  #struct_begin("System Event")
 *      #prop_desc("int", "id", "Identifier of the event")
 *      #prop_desc("string", "history_type", "Type of history event")
 *      #prop_desc("string", "status", "Status of the event")
 *      #prop_desc("string", "summary", "Summary of the event")
 *
 *      #prop_desc("dateTime.iso8601", "created", "Date that the event was created")
 *      #prop_desc("dateTime.iso8601", "picked_up", "Date that the event was picked up")
 *      #prop_desc("dateTime.iso8601", "completed", "Date that the event occurred")

 *      #prop_desc("dateTime.iso8601", "earliest_action", "Earliest date this action could occur.")
 *      #prop_desc("string", "result_msg", "The result string of the action executed on the client machine. (optional)")
 *      #prop_desc("int", "result_code", "The result code of the action executed on the client machine. (optional).")
 *          #prop_array_begin_desc("additional_info", "This array contains additional
 *              information for the event, if available.")
 *              #struct_begin("info")
 *                  #prop_desc("string", "detail", "The detail provided depends on the
 *                  specific event.  For example, for a package event, this will be the
 *                  package name, for an errata event, this will be the advisory name
 *                  and synopsis, for a config file event, this will be path and
 *                  optional revision information...etc.")
 *                  #prop_desc("string", "result", "The result (if included) depends
 *                  on the specific event.  For example, for a package or errata event,
 *                  no result is included, for a config file event, the result might
 *                  include an error (if one occurred, such as the file was missing)
 *                  or in the case of a config file comparison it might include the
 *                  differences found.")
 *              #struct_end()
 *          #prop_array_end()
 *  #struct_end()
 *
 */
public class SystemEventDetailsDtoSerializer extends RhnXmlRpcCustomSerializer {

    @Override
    public Class<SystemEventDetailsDto> getSupportedClass() {
        return SystemEventDetailsDto.class;

    }

    @Override
    protected void doSerialize(Object value, Writer output, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {

        SerializerHelper helper = new SerializerHelper(serializer);
        SystemEventDetailsDto detail = (SystemEventDetailsDto) value;

        helper.add("id", detail.getId());

        helper.add("history_type", Optional.ofNullable(detail.getHistoryTypeName()).orElse("History Event"));
        helper.add("status", detail.getHistoryStatus());
        helper.add("summary", detail.getSummary());

        helper.add("created", detail.getCreated());
        helper.add("picked_up", detail.getPickedUp());
        helper.add("completed", detail.getCompleted());

        helper.add("earliest_action", detail.getEarliestAction());
        helper.add("result_msg", detail.getResultMsg());
        helper.add("result_code", detail.getResultCode());

        // Add the all the additional properties
        if (detail.hasAdditionInfo()) {
            helper.add("additional_info", detail.getAdditionalInfo());
        }

        helper.writeTo(output);
    }
}
