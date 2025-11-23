/*
 * Copyright (c) 2024 SUSE LLC
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

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;
import com.suse.manager.webui.controllers.appstreams.response.AppStreamModuleResponse;
import com.suse.manager.webui.controllers.appstreams.response.ChannelAppStreamsResponse;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * ChannelAppStreamsResponseSerializer
 *
 * @apidoc.doc
 *  #struct_begin("ChannelAppStreams")
 *      #prop("string", "channel_name")
 *      #prop_array_begin("AppStreams")
 *          $AppStreamModuleResponseSerializer
 *      #array_end()
 *  #struct_end()
 *
 */
public class ChannelAppStreamsResponseSerializer  extends ApiResponseSerializer<ChannelAppStreamsResponse> {

    @Override
    public Class<ChannelAppStreamsResponse> getSupportedClass() {
        return ChannelAppStreamsResponse.class;
    }

    @Override
    public SerializedApiResponse serialize(ChannelAppStreamsResponse src) {
        SerializationBuilder builder = new SerializationBuilder();
        builder.add("channel_label", src.getChannel().getLabel());

        List<AppStreamModuleResponse> appstreams = new ArrayList<>(src.getAppStreams().keySet().size());
        if (!src.getAppStreams().isEmpty()) {
            src.getAppStreams().keySet().forEach(key -> {
                appstreams.addAll(src.getAppStreams().get(key));
            });
        }
        builder.add("app_streams", appstreams);

        return builder.build();
    }
}
