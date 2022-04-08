/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.manager.entitlement.EntitlementManager;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;


/**
 * ServerSerializer: Converts a Server object for representation as an XMLRPC struct.
 * Includes full server details, which may be more data than some calls would like.
 *
 *
 * @xmlrpc.doc
 *  #struct_begin("server details")
 *         #prop_desc("int", "id", "System id")
 *         #prop("string", "profile_name")
 *         #prop("string", "machine_id")
 *         #prop("string", "minion_id")
 *         #prop_desc("string", "base_entitlement", "System's base entitlement label")
 *
 *         #prop_array("string", "addon_entitlements","System's addon entitlements labels,
 *                       currently only 'virtualization_host'")
 *          #prop_desc("boolean", "auto_update", "True if system has auto errata updates
 *                                          enabled.")
 *          #prop_desc("string", "release", "The Operating System release (i.e. 4AS,
 *                      5Server")
 *          #prop("string", "address1")
 *          #prop("string", "address2")
 *          #prop("string", "city")
 *          #prop("string", "state")
 *          #prop("string", "country")
 *          #prop("string", "building")
 *          #prop("string", "room")
 *          #prop("string", "rack")
 *          #prop("string", "description")
 *          #prop("string", "hostname")
 *          #prop($date, "last_boot")
 *          #prop_desc("string", "osa_status", "Either 'unknown', 'offline', or 'online'.")
 *          #prop_desc("boolean", "lock_status", "True indicates that the system is locked.
 *           False indicates that the system is unlocked.")
 *          #prop_desc("string", "virtualization", "Virtualization type -
 *           for virtual guests only (optional)")
 *          #prop_desc("string", "contact_method", "One of the following:")
 *            #options()
 *              #item("default")
 *              #item("ssh-push")
 *              #item("ssh-push-tunnel")
 *            #options_end()
 *  #struct_end()
 */
public class ServerSerializer extends ApiResponseSerializer<Server> {

    @Override
    public Class<Server> getSupportedClass() {
        return Server.class;
    }

    @Override
    public SerializedApiResponse serialize(Server src) {
        SerializationBuilder builder = new SerializationBuilder()
                .add("id", src.getId())
                .add("profile_name", src.getName())
                .add("machine_id", src.getMachineId())
                .add("hostname", src.getHostname())
                .add("minion_id", src.getMinionId());

        // Find this server's base entitlement:
        String baseEntitlement = EntitlementManager.UNENTITLED;
        List<String> addonEntitlements = new LinkedList<>();
        for (Entitlement ent : src.getEntitlements()) {
            if (ent.isBase()) {
                baseEntitlement = ent.getLabel();
            }
            else {
                addonEntitlements.add(ent.getLabel());
            }
        }
        builder.add("base_entitlement", baseEntitlement);
        builder.add("addon_entitlements", addonEntitlements);

        Boolean autoUpdate = Boolean.FALSE;
        if (src.getAutoUpdate().equals("Y")) {
            autoUpdate = Boolean.TRUE;
        }
        builder.add("auto_update", autoUpdate);

        builder.add("description", StringUtils.defaultString(src.getDescription()));

        String address1 = "";
        String address2 = "";
        String city = "";
        String state = "";
        String country = "";
        String building = "";
        String room = "";
        String rack = "";
        if (src.getLocation() != null) {
            address1 = StringUtils.defaultString(src.getLocation().getAddress1());
            address2 = StringUtils.defaultString(src.getLocation().getAddress2());
            city = StringUtils.defaultString(src.getLocation().getCity());
            state = StringUtils.defaultString(src.getLocation().getState());
            country = StringUtils.defaultString(src.getLocation().getCountry());
            building = StringUtils.defaultString(src.getLocation().getBuilding());
            room = StringUtils.defaultString(src.getLocation().getRoom());
            rack = StringUtils.defaultString(src.getLocation().getRack());
        }
        builder.add("address1", address1);
        builder.add("address2", address2);
        builder.add("city", city);
        builder.add("state", state);
        builder.add("country", country);
        builder.add("building", building);
        builder.add("room", room);
        builder.add("rack", rack);

        builder.add("release", src.getRelease());
        builder.add("last_boot", src.getLastBootAsDate());

        if (src.getPushClient() != null) {
            builder.add("osa_status", src.getPushClient().getState().getName());
        }
        else {
            builder.add("osa_status", LocalizationService.getInstance()
                    .getMessage("sdc.details.overview.unknown"));
        }

        Boolean locked = Boolean.FALSE;
        if (src.getLock() != null) {
            locked = Boolean.TRUE;
        }
        builder.add("lock_status", locked);

        if (src.isVirtualGuest()) {
            if (src.getVirtualInstance().getType() != null) {
                builder.add("virtualization", src.getVirtualInstance().getType().getName());
            }
            else {
                builder.add("virtualization", "");
            }
        }

        // Contact method
        builder.add("contact_method", src.getContactMethod().getLabel());

        return builder.build();
    }
}
