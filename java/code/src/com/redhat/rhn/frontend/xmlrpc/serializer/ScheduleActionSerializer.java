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

import com.redhat.rhn.frontend.dto.ScheduledAction;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * ScheduleActionSerializer
 *
 * @apidoc.doc
 *
 * #struct_begin("action")
 *   #prop_desc("int", "id", "action ID")
 *   #prop_desc("string", "name", "action name")
 *   #prop_desc("string", "type", "action type")
 *   #prop_desc("string", "scheduler", "the user that scheduled the action (optional)")
 *   #prop_desc($date, "earliest", "the earliest date and time the action
 *   will be performed")
 *   #prop_desc("int", "prerequisite", "ID of the prerequisite action (optional)")
 *   #prop_desc("int", "completedSystems", "number of systems that completed the action")
 *   #prop_desc("int", "failedSystems", "number of systems that failed the action")
 *   #prop_desc("int", "inProgressSystems", "number of systems that are in progress")
 * #struct_end()
 */
public class ScheduleActionSerializer extends ApiResponseSerializer<ScheduledAction> {

    @Override
    public Class<ScheduledAction> getSupportedClass() {
        return ScheduledAction.class;
    }

    @Override
    public SerializedApiResponse serialize(ScheduledAction src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("name", src.getActionName())
                .add("type", src.getTypeName())
                .add("scheduler", src.getSchedulerName())
                .add("earliest", src.getEarliestDate())
                .add("prerequisite", src.getPrerequisite())
                .add("completedSystems", src.getCompletedSystems())
                .add("failedSystems", src.getFailedSystems())
                .add("inProgressSystems", src.getInProgressSystems())
                .build();
    }
}
