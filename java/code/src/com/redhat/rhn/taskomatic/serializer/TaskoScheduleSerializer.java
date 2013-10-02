/**
 * Copyright (c) 2010 Red Hat, Inc.
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
package com.redhat.rhn.taskomatic.serializer;

import com.redhat.rhn.frontend.xmlrpc.serializer.RhnXmlRpcCustomSerializer;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;
import com.redhat.rhn.taskomatic.TaskoSchedule;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;


/**
 * TaskoScheduleSerializer
 * @version $Rev$
 */
public class TaskoScheduleSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    public Class getSupportedClass() {
        return TaskoSchedule.class;
    }

    /**
     * {@inheritDoc}
     */
    public void doSerialize(Object value, Writer output,
            XmlRpcSerializer serializer)
        throws XmlRpcException, IOException {

        TaskoSchedule schedule = (TaskoSchedule) value;
        SerializerHelper helper = new SerializerHelper(serializer);

        helper.add("id", schedule.getId());
        helper.add("job_label", schedule.getJobLabel());
        helper.add("bunch", schedule.getBunch().getName());
        helper.add("active_from", schedule.getActiveFrom());
        helper.add("active_till", schedule.getActiveTill());
        if (schedule.getCronExpr() != null) {
            helper.add("cron_expr", schedule.getCronExpr());
        }
        Map dataMap = schedule.getDataMap();
        if (dataMap == null) {
            dataMap = new HashMap();
        }
        helper.add("data_map", dataMap);

        helper.writeTo(output);
    }
}
