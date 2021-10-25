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

import com.redhat.rhn.frontend.xmlrpc.serializer.RhnXmlRpcCustomSerializer;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

import com.suse.manager.model.maintenance.MaintenanceCalendar;

import java.io.IOException;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 * Serializer for {@link com.suse.manager.model.maintenance.MaintenanceCalendar}
 *
 * @xmlrpc.doc
 * #struct_begin("Maintenance Calendar information")
 *   #prop("int", "id")
 *   #prop("int", "orgId")
 *   #prop("string", "label")
 *   #prop_desc("string", "url", "calendar url if present")
 *   #prop("string", "ical")
 * #struct_end()
 */
public class MaintenanceCalendarSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class getSupportedClass() {
        return MaintenanceCalendar.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSerialize(Object obj, Writer writer, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        MaintenanceCalendar calendar = (MaintenanceCalendar) obj;
        SerializerHelper helper = new SerializerHelper(serializer);
        helper.add("id", calendar.getId());
        helper.add("orgId", calendar.getOrg().getId());
        helper.add("label", calendar.getLabel());
        calendar.getUrlOpt().ifPresent(u -> helper.add("url", u));
        helper.add("ical", calendar.getIcal());
        helper.writeTo(writer);
    }
}
