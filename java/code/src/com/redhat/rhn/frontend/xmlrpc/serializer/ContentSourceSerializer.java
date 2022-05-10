/*
 * Copyright (c) 2009--2017 Red Hat, Inc.
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

import com.redhat.rhn.domain.channel.ContentSource;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 *
 * ContentSourceSerializer
 *
 * @apidoc.doc
 *  #struct_begin("channel")
 *      #prop("int", "id")
 *      #prop("string", "label")
 *      #prop("string", "sourceUrl")
 *      #prop("string", "type")
 *      #prop("boolean", "hasSignedMetadata")
 *      #prop_array_begin("sslContentSources")
 *         $SslContentSourceSerializer
 *      #prop_array_end()
 *  #struct_end()
 *
 */
public class ContentSourceSerializer extends ApiResponseSerializer<ContentSource> {

    @Override
    public Class<ContentSource> getSupportedClass() {
        return ContentSource.class;
    }

    @Override
    public SerializedApiResponse serialize(ContentSource src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("label", src.getLabel())
                .add("sourceUrl", src.getSourceUrl())
                .add("type", src.getType().getLabel())
                .add("hasSignedMetadata", src.getMetadataSigned())
                .add("sslContentSources", src.getSslSets())
                .build();
    }
}
