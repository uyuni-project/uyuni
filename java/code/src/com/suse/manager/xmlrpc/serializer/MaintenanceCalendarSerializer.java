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

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;
import com.suse.manager.model.maintenance.MaintenanceCalendar;

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
public class MaintenanceCalendarSerializer extends ApiResponseSerializer<MaintenanceCalendar> {

    @Override
    public Class<MaintenanceCalendar> getSupportedClass() {
        return MaintenanceCalendar.class;
    }

    @Override
    public SerializedApiResponse serialize(MaintenanceCalendar src) {
        SerializationBuilder builder = new SerializationBuilder()
                .add("id", src.getId())
                .add("orgId", src.getOrg().getId())
                .add("label", src.getLabel());
        src.getUrlOpt().ifPresent(u -> builder.add("url", u));
        builder.add("ical", src.getIcal());
        return builder.build();
    }
}
