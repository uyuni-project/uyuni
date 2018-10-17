/**
 * Copyright (c) 2018 SUSE LLC
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
package com.redhat.rhn.frontend.xmlrpc.serializer.util;

import com.redhat.rhn.frontend.dto.SystemOverview;

import java.util.Date;

/**
 * Common code for serializing {@link com.redhat.rhn.frontend.dto.SystemOverview} and
 * its subclasses.
 */
public class SystemSerializerUtils {

    /**
     * Forbid instantiation.
     */
    private SystemSerializerUtils() { }

    /**
     * Serialize system overview using {@link com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper}
     *
     * @param system the system overview
     * @param helper the {@link com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper}
     */
    public static void serializeSystemOverview(SystemOverview system, SerializerHelper helper) {
        helper.add("id", system.getId());
        helper.add("name", system.getName());

        Date regDate = system.getCreated();
        if (regDate != null) {
            helper.add("created", regDate);
        }

        Date lastBoot = system.getLastBootAsDate();
        if (lastBoot != null) {
            helper.add("last_boot", lastBoot);
        }
    }
}
