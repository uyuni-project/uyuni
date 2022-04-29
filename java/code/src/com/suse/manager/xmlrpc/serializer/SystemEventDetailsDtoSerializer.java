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

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;
import com.suse.manager.xmlrpc.dto.SystemEventDetailsDto;

import java.util.Optional;

/**
 * Serializer for {@link SystemEventDetailsDto}
 *
 * @xmlrpc.doc
 *  #struct_begin("system event")
 *      #prop_desc("int", "id", "ID of the event")
 *      #prop_desc("string", "history_type", "type of history event")
 *      #prop_desc("string", "status", "status of the event")
 *      #prop_desc("string", "summary", "summary of the event")
 *
 *      #prop_desc("$date", "created", "date that the event was created")
 *      #prop_desc("$date", "picked_up", "date that the event was picked up")
 *      #prop_desc("$date", "completed", "date that the event occurred")

 *      #prop_desc("$date", "earliest_action", "earliest date this action could occur")
 *      #prop_desc("string", "result_msg", "the result string of the action executed on the client machine (optional)")
 *      #prop_desc("int", "result_code", "the result code of the action executed on the client machine (optional)")
 *          #prop_array_begin_desc("additional_info", "additional information for the event, if available")
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
 */
public class SystemEventDetailsDtoSerializer extends ApiResponseSerializer<SystemEventDetailsDto> {

    @Override
    public Class<SystemEventDetailsDto> getSupportedClass() {
        return SystemEventDetailsDto.class;
    }

    @Override
    public SerializedApiResponse serialize(SystemEventDetailsDto src) {
        SerializationBuilder builder = new SerializationBuilder()
                .add("id", src.getId())
                .add("history_type", Optional.ofNullable(src.getHistoryTypeName()).orElse("History Event"))
                .add("status", src.getHistoryStatus())
                .add("summary", src.getSummary())
                .add("created", src.getCreated())
                .add("picked_up", src.getPickedUp())
                .add("completed", src.getCompleted())
                .add("earliest_action", src.getEarliestAction())
                .add("result_msg", src.getResultMsg())
                .add("result_code", src.getResultCode());

        // Add the all the additional properties
        if (src.hasAdditionInfo()) {
            builder.add("additional_info", src.getAdditionalInfo());
        }

        return builder.build();
    }
}
