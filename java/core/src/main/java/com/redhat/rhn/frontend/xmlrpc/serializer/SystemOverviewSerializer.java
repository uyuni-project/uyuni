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
 * @apidoc.doc
 *
 * #struct_begin("system")
 *     #prop("int", "id")
 *     #prop("string", "name")
 *     #prop_desc("int", "group_count", "number of groups the system is in")
 *     #prop_desc("int", "security_errata", "number of applied security erratas")
 *     #prop_desc("int", "bug_errata", "number of applied bug erratas")
 *     #prop_desc("int", "enhancement_errata", "number of applied enhancement erratas")
 *     #prop_desc("int", "outdated_pkg_count", "number of out-of-date packages")
 *     #prop_desc("int", "config_files_with_difference", "number of configuration files changed")
 *     #prop_desc("string", "channel_labels", "channel set on the system")
 *     #prop_desc("$date", "last_checkin", "last time server successfully checked in")
 *     #prop_desc("boolean", "mgr_server", "true if the system is a peripheral server")
 *     #prop_desc("boolean", "proxy", "true if the system is a proxy")
 *     #prop_array("entitlement", "string", "List of entitlements of the system")
 *     #prop_desc("boolean", "virtual_host", "true if the system is a virtual host")
 *     #prop_desc("boolean", "virtual_guest", "true if the system is a virtual guest")
 *     #prop_desc("int", "extra_pkg_count", "number of packages not belonging to any assigned channel")
 *     #prop_desc("boolean", "requires_reboot", "true if the systems needs to be rebooted")
 *     #prop_desc("$date", "created", "server registration time")
 *     #prop_desc("$date", "last_boot", "last server boot time")
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
                .add("group_count", src.getGroupCount())
                .add("security_errata", src.getSecurityErrata())
                .add("bug_errata", src.getBugErrata())
                .add("enhancement_errata", src.getEnhancementErrata())
                .add("outdated_pkg_count", src.getOutdatedPackages())
                .add("config_files_with_difference", src.getConfigFilesWithDifferences())
                .add("channel_labels", src.getChannelLabels())
                .add("last_checkin", src.getLastCheckinDate())
                .add("mgr_server", src.isMgrServer())
                .add("proxy", src.isProxy())
                .add("entitlement", src.getEntitlement())
                .add("virtual_host", src.getVirtualHost())
                .add("virtual_guest", src.getVirtualGuest())
                .add("extra_pkg_count", src.getExtraPkgCount())
                .add("requires_reboot", src.getRequiresReboot())
                .add("status_type", src.getStatusType());

        Date regDate = src.getCreated();
        if (regDate != null) {
            builder.add("created", regDate);
        }

        Date lastBoot = src.getLastBootAsDate();
        if (lastBoot != null) {
            builder.add("last_boot", lastBoot);
        }
        return builder.build();
    }
}
