/*
 * Copyright (c) 2016 SUSE LLC
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

import com.redhat.rhn.frontend.xmlrpc.activationkey.ChannelInfo;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 *
 * ChannelInfoSerializer
 *
 * @xmlrpc.doc
 *  #struct_begin("channelInfo")
 *       #prop_desc("string", "label", "Channel label")
 *       #prop_desc("string", "name", "Channel name")
 *       #prop_desc("string", "url", "Channel url")
 *       #prop_desc("string", "token", "Channel access token")
 *  #struct_end()
 *
 */
public class ChannelInfoSerializer extends ApiResponseSerializer<ChannelInfo> {

    @Override
    public Class<ChannelInfo> getSupportedClass() {
        return ChannelInfo.class;
    }

    @Override
    public SerializedApiResponse serialize(ChannelInfo src) {
        return new SerializationBuilder()
                .add("label", src.getLabel())
                .add("name", src.getName())
                .add("url", src.getUrl())
                .add("token", src.getToken())
                .build();
    }
}
