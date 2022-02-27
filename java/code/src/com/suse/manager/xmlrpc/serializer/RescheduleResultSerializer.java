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
import com.redhat.rhn.frontend.xmlrpc.serializer.RhnXmlRpcCustomSerializer;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

import com.suse.manager.maintenance.rescheduling.RescheduleResult;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 * Serializer for {@link com.suse.manager.model.maintenance.RescheduleResult}
 *
 * @xmlrpc.doc
 * #struct_begin("Reschedule information")
 *   #prop_desc("string", "strategy", "selected strategy")
 *   #prop("string", "for_schedule_name", "for actions belong to this schedule")
 *   #prop("boolean", "status")
 *   #prop("string", "message")
 *   #prop_array_begin("actions")
 *     #struct_begin("Action information")
 *       #prop_desc("int", "id", "Action Id.")
 *       #prop_desc("string", "name", "Action name.")
 *       #prop_desc("string", "type", "Action type.")
 *       #prop_desc("string", "scheduler", "The user that scheduled the action. (optional)")
 *       #prop_desc($date, "earliest", "The earliest date and time the action
 *       will be performed")
 *       #prop_desc("int", "prerequisite", "ID of the prerequisite Action (optional)")
 *       #prop_array("affected_system_ids", "int", "affected System Ids")
 *       #prop_desc("string", "details", "Action details string")
 *     #struct_end()
 *   #prop_array_end()
 * #struct_end()
 */
public class RescheduleResultSerializer extends RhnXmlRpcCustomSerializer {

    @Override
    public Class getSupportedClass() {
        // TODO Auto-generated method stub
        return RescheduleResult.class;
    }

    @Override
    protected void doSerialize(Object obj, Writer writer, XmlRpcSerializer serializer)
        throws XmlRpcException, IOException {
        RescheduleResult result = (RescheduleResult) obj;
        SerializerHelper helper = new SerializerHelper(serializer);
        helper.add("strategy", result.getStrategy());
        helper.add("for_schedule_name", result.getScheduleName());
        helper.add("status", result.isSuccess());
        helper.add("message", result.getMessage());

        List<Map<String, Object>> actions = new ArrayList<>();
        for (Action action: result.getActionsServers().keySet()) {
            Map<String, Object> a = new HashMap<>();
            a.put("id", action.getId());
            a.put("name", action.getName());
            a.put("type", action.getActionType().getName());
            a.put("scheduler", action.getSchedulerUser().getLogin());
            a.put("earliest", action.getEarliestAction());
            if (action.getPrerequisite() != null) {
                a.put("prerequisite", action.getPrerequisite().getId());
            }
            a.put("affected_system_ids", result.getActionsServers().get(action).stream()
                    .map(Server::getId).collect(Collectors.toList()));
            a.put("details", StringUtil.toPlainText(action.getFormatter().getNotes()));
            actions.add(a);
        }

        helper.add("actions", actions);
        helper.writeTo(writer);

    }

}
