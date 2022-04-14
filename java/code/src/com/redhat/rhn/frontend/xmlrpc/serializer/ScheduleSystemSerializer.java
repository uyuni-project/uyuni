/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.serializer;

import com.redhat.rhn.frontend.dto.ActionedSystem;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * ScheduleSystemSerializer
 * @xmlrpc.doc
 *
 * #struct_begin("system")
 *   #prop("int", "server_id")
 *   #prop_desc("string", "server_name", "Server name.")
 *   #prop_desc("string", "base_channel", "Base channel used by the server.")
 *   #prop_desc($date, "timestamp", "The time the action was completed")
 *   #prop_desc("string", "message", "Optional message containing details
 *   on the execution of the action.  For example, if the action failed,
 *   this will contain the failure text.")
 * #struct_end()
 */
public class ScheduleSystemSerializer extends ApiResponseSerializer<ActionedSystem> {

    @Override
    public Class<ActionedSystem> getSupportedClass() {
        return ActionedSystem.class;
    }

    @Override
    public SerializedApiResponse serialize(ActionedSystem src) {
        return new SerializationBuilder()
                .add("server_id", src.getId())
                .add("server_name", src.getServerName())
                .add("base_channel", src.getChannelLabels())
                .add("timestamp", src.getDate())
                .add("message", src.getMessage())
                .build();
    }
}
