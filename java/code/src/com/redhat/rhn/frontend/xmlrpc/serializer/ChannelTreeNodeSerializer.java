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

import com.redhat.rhn.frontend.dto.ChannelTreeNode;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * ChannelTreeNodeSerializer: Converts a ChannelTreeNode object for
 * representation as an XMLRPC struct.
 *
 * @xmlrpc.doc
 *   #struct_begin("channel info")
 *     #prop("int", "id")
 *     #prop("string", "label")
 *     #prop("string", "name")
 *     #prop("string", "provider_name")
 *     #prop("int", "packages")
 *     #prop("int", "systems")
 *     #prop("string", "arch_name")
 *   #struct_end()
 */
public class ChannelTreeNodeSerializer extends ApiResponseSerializer<ChannelTreeNode> {

    @Override
    public Class<ChannelTreeNode> getSupportedClass() {
        return ChannelTreeNode.class;
    }

    @Override
    public SerializedApiResponse serialize(ChannelTreeNode src) {
        SerializationBuilder builder = new SerializationBuilder();

        builder.add("id", src.getId());
        builder.add("label", src.getChannelLabel());
        builder.add("name", src.getName());

        if (src.getOrgId() != null) {
            builder.add("provider_name", src.getOrgName());
        }
        else {
            builder.add("provider_name", "SUSE");
        }

        builder.add("packages", src.getPackageCount());

        if (src.getSystemCount() == null) {
            // it is possible for the current query to result in the count
            // being null; however, in this scenario, we still want to serialize the
            // result as 0.
            builder.add("systems", 0);
        }
        else {
            builder.add("systems", src.getSystemCount());
        }

        builder.add("arch_name", src.getArchName());

        return builder.build();
    }
}
