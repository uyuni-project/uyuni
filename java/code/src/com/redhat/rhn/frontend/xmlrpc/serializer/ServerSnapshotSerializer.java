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

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.server.EntitlementServerGroup;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerSnapshot;
import com.redhat.rhn.domain.server.SnapshotTag;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import java.util.HashSet;
import java.util.Set;


/**
 * ServerSerializer: Converts a Server object for representation as an XMLRPC struct.
 * Includes full server details, which may be more data than some calls would like.
 *
 *
 * @xmlrpc.doc
 *  #struct_begin("server snapshot")
 *      #prop("int", "id")
 *      #prop_desc("string", "reason", "the reason for the snapshot's existence")
 *      #prop($date, "created")
 *      #prop_array("channels", "string", "labels of channels associated with the
 *              snapshot")
 *      #prop_array("groups", "string", "names of server groups associated with
 *              the snapshot")
 *      #prop_array("entitlements", "string", "names of system entitlements associated
 *              with the snapshot")
 *       #prop_array("config_channels", "string", "labels of config channels the snapshot
 *                  is associated with")
 *      #prop_array("tags", "string", "tag names associated with this snapshot")
 *      #prop_desc("string", "Invalid_reason", "if the snapshot is invalid, this is the
 *                  reason (optional)")
 *  #struct_end()
 */
public class ServerSnapshotSerializer extends ApiResponseSerializer<ServerSnapshot> {

    @Override
    public Class<ServerSnapshot> getSupportedClass() {
        return ServerSnapshot.class;
    }

    @Override
    public SerializedApiResponse serialize(ServerSnapshot src) {
        SerializationBuilder builder = new SerializationBuilder()
                .add("id", src.getId())
                .add("reason", src.getReason())
                .add("created", src.getCreated());

        Set<String> channels = new HashSet<>();
        for (Channel chan : src.getChannels()) {
            channels.add(chan.getLabel());
        }
        builder.add("channels", channels);

        Set<String> entGroups = new HashSet<>();
        Set<String> mgmtGroups = new HashSet<>();
        for (ServerGroup grp : src.getGroups()) {
            if (grp instanceof EntitlementServerGroup) {
                entGroups.add(grp.getName());
            }
            else {
                mgmtGroups.add(grp.getName());
            }
        }
        builder.add("groups", mgmtGroups);
        builder.add("entitlements", entGroups);

        Set<String> cfgChans = new HashSet<>();
        for (ConfigChannel grp : src.getConfigChannels()) {
            cfgChans.add(grp.getLabel());
        }
        builder.add("config_channels", cfgChans);

        if (src.getInvalidReason() != null) {
            builder.add("Invalid_reason", src.getInvalidReason().getName());
        }

        Set<String> tags = new HashSet();
        for (SnapshotTag tag : src.getTags()) {
            tags.add(tag.getName().getName());
        }
        builder.add("tags", tags);

        return builder.build();
    }


}
