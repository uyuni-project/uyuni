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

import com.redhat.rhn.domain.channel.AppStream;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 *
 * AppStreamSerializer
 *
 * @apidoc.doc
 *  #struct_begin("AppStream")
 *     #prop("string", "stream")
 *     #prop("string", "module")
 *     #prop("string", "arch")
 *  #struct_end()
 *
 */
public class AppStreamSerializer extends ApiResponseSerializer<AppStream> {

    @Override
    public Class<AppStream> getSupportedClass() {
        return AppStream.class;
    }

    @Override
    public SerializedApiResponse serialize(AppStream src) {
        SerializationBuilder builder = new SerializationBuilder();

        builder.add("module", src.getName());
        builder.add("stream", src.getStream());
        builder.add("arch", src.getArch());

        return builder.build();
    }
}
