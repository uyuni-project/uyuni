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
import com.redhat.rhn.taskomatic.TaskoRun;

import java.io.IOException;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;


/**
 * TaskoRunSerializer
 * @version $Rev$
 */
public class TaskoRunSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    public Class getSupportedClass() {
        return TaskoRun.class;
    }

    /**
     * {@inheritDoc}
     */
    public void doSerialize(Object value, Writer output,
            XmlRpcSerializer serializer)
        throws XmlRpcException, IOException {

        TaskoRun run = (TaskoRun) value;
        SerializerHelper helper = new SerializerHelper(serializer);

        helper.add("id", run.getId());
        helper.add("schedule_id", run.getScheduleId());
        helper.add("task", run.getTemplate().getTask().getName());
        helper.add("start_time", run.getStartTime());
        helper.add("end_time", run.getEndTime());
        helper.add("status", run.getStatus());
        helper.add("stdOutputPath", run.getStdOutputPath() != null);
        helper.add("stdErrorPath", run.getStdErrorPath() != null);

        helper.writeTo(output);
    }
}
