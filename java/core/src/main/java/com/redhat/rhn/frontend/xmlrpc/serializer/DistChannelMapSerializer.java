/*
 * Copyright (c) 2010--2012 Red Hat, Inc.
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

import com.redhat.rhn.domain.channel.DistChannelMap;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 *
 * DistChannelMapSerializer
 *
 * @apidoc.doc
 *      #struct_begin("distribution channel map")
 *          #prop_desc("string", "os", "operating system")
 *          #prop_desc("string", "release", "OS Relase")
 *          #prop_desc("string", "arch_name", "channel architecture")
 *          #prop_desc("string", "channel_label", "channel label")
 *          #prop_desc("string", "org_specific", "'Y' organization specific, 'N' default")
 *     #struct_end()
 */
public class DistChannelMapSerializer extends ApiResponseSerializer<DistChannelMap> {

    @Override
    public Class<DistChannelMap> getSupportedClass() {
        return DistChannelMap.class;
    }

    @Override
    public SerializedApiResponse serialize(DistChannelMap src) {
        return new SerializationBuilder()
                .add("os", src.getOs())
                .add("release", src.getRelease())
                .add("arch_name", src.getChannelArch().getName())
                .add("channel_label", src.getChannel().getLabel())
                .add("org_specific", src.getOrg() == null ? "N" : "Y")
                .build();
    }
}
