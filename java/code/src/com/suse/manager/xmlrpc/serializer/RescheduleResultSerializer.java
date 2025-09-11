/*
 * Copyright (c) 2020 SUSE LLC
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

import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.server.Server;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;
import com.suse.manager.maintenance.rescheduling.RescheduleResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serializer for {@link com.suse.manager.maintenance.rescheduling.RescheduleResult}
 *
 * @apidoc.doc
 * #struct_begin("reschedule information")
 *   #prop_desc("string", "strategy", "selected strategy")
 *   #prop("string", "for_schedule_name", "for actions belong to this schedule")
 *   #prop("boolean", "status")
 *   #prop("string", "message")
 *   #prop_array_begin("actions")
 *     #struct_begin("action information")
 *       #prop_desc("int", "id", "action ID")
 *       #prop_desc("string", "name", "action name")
 *       #prop_desc("string", "type", "action type")
 *       #prop_desc("string", "scheduler", "the user that scheduled the action (optional)")
 *       #prop_desc($date, "earliest", "the earliest date and time the action will be performed")
 *       #prop_desc("int", "prerequisite", "ID of the prerequisite action (optional)")
 *       #prop_array("affected_system_ids", "int", "affected system IDs")
 *       #prop_desc("string", "details", "action details string")
 *     #struct_end()
 *   #prop_array_end()
 * #struct_end()
 */
public class RescheduleResultSerializer extends ApiResponseSerializer<RescheduleResult> {

    @Override
    public Class<RescheduleResult> getSupportedClass() {
        return RescheduleResult.class;
    }

    @Override
    public SerializedApiResponse serialize(RescheduleResult src) {
        SerializationBuilder builder = new SerializationBuilder()
                .add("strategy", src.getStrategy())
                .add("for_schedule_name", src.getScheduleName())
                .add("status", src.isSuccess())
                .add("message", src.getMessage());

        List<Map<String, Object>> actions = new ArrayList<>();
        for (Action action: src.getActionsServers().keySet()) {
            Map<String, Object> a = new HashMap<>();
            a.put("id", action.getId());
            a.put("name", action.getName());
            a.put("type", action.getActionType().getName());
            a.put("scheduler", action.getSchedulerUser().getLogin());
            a.put("earliest", action.getEarliestAction());
            if (action.getPrerequisite() != null) {
                a.put("prerequisite", action.getPrerequisite().getId());
            }
            a.put("affected_system_ids", src.getActionsServers().get(action).stream()
                    .map(Server::getId).collect(Collectors.toList()));
            a.put("details", StringUtil.toPlainText(action.getFormatter().getNotes()));
            actions.add(a);
        }

        builder.add("actions", actions);
        return builder.build();
    }
}
