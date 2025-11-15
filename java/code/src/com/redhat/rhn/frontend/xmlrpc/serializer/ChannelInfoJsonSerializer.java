/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.redhat.rhn.frontend.xmlrpc.serializer;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;
import com.suse.manager.model.hub.ChannelInfoJson;

/**
 * Converts a ChannelInfoJson to an XMLRPC &lt;struct&gt;.
 *
 * @apidoc.doc
 *   #struct_begin(" channel info ")
 *     #prop_desc("long", "id", "the id of the channel")
 *     #prop_desc("string", "name", "the name of the channel")
 *     #prop_desc("boolean", "label", "the label of the channel")
 *     #prop_desc("string", "summary", "the summary of the channel")
 *     #prop_desc("long", "org_id", "the organization id of the channel")
 *     #prop_desc("long", "parent_channel_id", "the parent channel ID of the channel")
 *   #struct_end()
 */
public class ChannelInfoJsonSerializer extends ApiResponseSerializer<ChannelInfoJson> {

    @Override
    public Class<ChannelInfoJson> getSupportedClass() {
        return ChannelInfoJson.class;
    }

    @Override
    public SerializedApiResponse serialize(ChannelInfoJson src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("name", src.getName())
                .add("label", src.getLabel())
                .add("summary", src.getSummary())
                .add("org_id", src.getOrgId())
                .add("parent_channel_id", src.getParentChannelId())
                .build();
    }
}
