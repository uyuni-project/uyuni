/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.server.ServerAction;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;


/**
 * ActivationKeySerializer
 *
 * @apidoc.doc
 *      #struct_begin("action")
 *          #prop_desc("int", "failed_count", "number of times action failed")
 *          #prop_desc("string", "modified", "date modified (deprecated by 'modified_date')")
 *          #prop_desc($date, "modified_date", "date modified")
 *          #prop_desc("string", "created", "date created (deprecated by 'created_date')")
 *          #prop_desc($date, "created_date", "date created")
 *          #prop("string", "action_type")
 *          #prop_desc("int", "successful_count",
 *                      "number of times action was successful")
 *          #prop_desc("string", "earliest_action", "earliest date this action
 *                      will occur")
 *          #prop_desc("int", "archived", "if this action is archived (1 or 0)")
 *          #prop("string", "scheduler_user")
 *          #prop_desc("string", "prerequisite", "pre-requisite action (optional)")
 *          #prop_desc("string", "name", "name of this action")
 *          #prop_desc("int", "id", "ID of this action")
 *          #prop_desc("string", "version", "version of action")
 *          #prop_desc("string", "completion_time", "the date/time the event was completed.
 *                                  Format -&gt;YYYY-MM-dd hh:mm:ss.ms
 *                                  Eg -&gt;2007-06-04 13:58:13.0. (optional)
 *                                  (deprecated by completed_date)")
 *          #prop_desc($date, "completed_date", "the date/time the event was completed
 *                                  (optional)")
 *          #prop_desc("string", "pickup_time", "the date/time the action was picked up
 *                                   Format -&gt;YYYY-MM-dd hh:mm:ss.ms
 *                                   Eg -&gt;2007-06-04 13:58:13.0. (optional)
 *                                   (deprecated by pickup_date)")
 *          #prop_desc($date, "pickup_date", "the date/time the action was picked up
 *                                   (optional)")
 *          #prop_desc("string", "result_msg", "the result string after the action
 *                                       executes at the client machine (optional)")
 *      #struct_end()
 */
public class ServerActionSerializer extends ApiResponseSerializer<ServerAction> {

    @Override
    public Class<ServerAction> getSupportedClass() {
        return ServerAction.class;
    }

    @Override
    public SerializedApiResponse serialize(ServerAction src) {
        Action act = src.getParentAction();

        SerializationBuilder builder = ActionSerializer.getSerializationBuilder(act);

        if (src.getCompletionTime() != null) {
            builder.add("completion_time", src.getCompletionTime().toString());
        }
        if (src.getPickupTime() != null) {
            builder.add("pickup_time", src.getPickupTime().toString());
        }

        builder.add("completed_date", src.getCompletionTime())
                .add("pickup_date", src.getPickupTime())
                .add("result_msg", src.getResultMsg());

        return builder.build();
    }

}
