/**
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

import com.redhat.rhn.frontend.xmlrpc.serializer.RhnXmlRpcCustomSerializer;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

import com.suse.manager.model.maintenance.MaintenanceSchedule;

import java.io.IOException;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 * Serializer for {@link com.suse.manager.model.maintenance.MaintenanceSchedule}
 *
 * @xmlrpc.doc
 * #struct_begin("maintenance schedule information")
 *   #prop("int", "id")
 *   #prop("int", "orgId")
 *   #prop("string", "name")
 *   #prop("string", "type")
 *   $MaintenanceCalendarSerializer
 * #struct_end()
 */
public class MaintenanceScheduleSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class getSupportedClass() {
        return MaintenanceSchedule.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSerialize(Object obj, Writer writer, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        MaintenanceSchedule schedule = (MaintenanceSchedule) obj;
        SerializerHelper helper = new SerializerHelper(serializer);
        helper.add("id", schedule.getId());
        helper.add("orgId", schedule.getOrg().getId());
        helper.add("name", schedule.getName());
        helper.add("type", schedule.getScheduleType().getLabel());
        schedule.getCalendarOpt().ifPresent(cal -> helper.add("calendar", cal));
        helper.writeTo(writer);
    }
}
