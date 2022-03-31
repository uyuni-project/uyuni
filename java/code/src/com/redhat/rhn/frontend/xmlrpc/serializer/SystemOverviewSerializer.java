/*
 * Copyright (c) 2009--2015 Red Hat, Inc.
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


import com.redhat.rhn.frontend.dto.SystemOverview;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import java.util.Date;

/**
 *
 * SystemOverviewSerializer
 *
 * @xmlrpc.doc
 *
 * #struct_begin("system")
 *     #prop("int", "id")
 *     #prop("string", "name")
 *     #prop_desc("dateTime.iso8601",  "last_checkin", "Last time server
 *             successfully checked in")
 *     #prop_desc("dateTime.iso8601",  "created", "Server registration time")
 *     #prop_desc("dateTime.iso8601",  "last_boot", "Last server boot time")
 *     #prop_desc("int",  "extra_pkg_count", "Number of packages not belonging
 *             to any assigned channel")
 *     #prop_desc("int",  "outdated_pkg_count", "Number of out-of-date packages")
 * #struct_end()
 */
public class SystemOverviewSerializer extends ApiResponseSerializer<SystemOverview> {

    @Override
    public Class<SystemOverview> getSupportedClass() {
        return SystemOverview.class;
    }

    @Override
    public SerializedApiResponse serialize(SystemOverview src) {
        SerializationBuilder builder = new SerializationBuilder()
                .add("id", src.getId())
                .add("name", src.getName())
                .add("last_checkin", src.getLastCheckinDate());

        Date regDate = src.getCreated();
        if (regDate != null) {
            builder.add("created", regDate);
        }

        Date lastBoot = src.getLastBootAsDate();
        if (lastBoot != null) {
            builder.add("last_boot", lastBoot);
        }
        builder.add("extra_pkg_count", src.getExtraPkgCount());
        builder.add("outdated_pkg_count", src.getOutdatedPackages());
        return builder.build();
    }
}
