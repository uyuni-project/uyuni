/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ContentSource;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * ChannelSerializer
 *
 * @apidoc.doc
 *  #struct_begin("channel")
 *      #prop("int", "id")
 *      #prop("string", "name")
 *      #prop("string", "label")
 *      #prop("string", "arch_name")
 *      #prop("string", "arch_label")
 *      #prop("string", "summary")
 *      #prop("string", "description")
 *      #prop("string", "checksum_label")
 *      #prop("$date", "last_modified")
 *      #prop("string", "maintainer_name")
 *      #prop("string", "maintainer_email")
 *      #prop("string", "maintainer_phone")
 *      #prop("string", "support_policy")
 *      #prop("string", "gpg_key_url")
 *      #prop("string", "gpg_key_id")
 *      #prop("string", "gpg_key_fp")
 *      #prop_desc("$date", "yumrepo_last_sync", "(optional)")
 *      #prop("string", "end_of_life")
 *      #prop("string", "parent_channel_label")
 *      #prop("string", "clone_original")
 *      #prop_array_begin("contentSources")
 *          #struct_begin("content source")
 *              #prop("int", "id")
 *              #prop("string", "label")
 *              #prop("string", "sourceUrl")
 *              #prop("string", "type")
 *          #struct_end()
 *      #array_end()
 *  #struct_end()
 *
 */
public class ChannelSerializer extends ApiResponseSerializer<Channel> {

    @Override
    public Class<Channel> getSupportedClass() {
        return Channel.class;
    }

    @Override
    public SerializedApiResponse serialize(Channel src) {
        SerializationBuilder builder = new SerializationBuilder();

        builder.add("id", src.getId());
        builder.add("label", src.getLabel());
        builder.add("name", src.getName());
        builder.add("arch_name",
                StringUtils.defaultString(src.getChannelArch().getName()));
        builder.add("arch_label",
                StringUtils.defaultString(src.getChannelArch().getLabel()));
        builder.add("summary", StringUtils.defaultString(src.getSummary()));
        builder.add("description",
                StringUtils.defaultString(src.getDescription()));
        builder.add("checksum_label", src.getChecksumTypeLabel());
        builder.add("last_modified", src.getLastModified());
        builder.add("maintainer_name",
                StringUtils.defaultString(src.getMaintainerName()));
        builder.add("maintainer_email",
                StringUtils.defaultString(src.getMaintainerEmail()));
        builder.add("maintainer_phone",
                StringUtils.defaultString(src.getMaintainerPhone()));
        builder.add("support_policy",
                StringUtils.defaultString(src.getSupportPolicy()));

        builder.add("gpg_key_url",
                StringUtils.defaultString(src.getGPGKeyUrl()));
        builder.add("gpg_key_id",
                StringUtils.defaultString(src.getGPGKeyId()));
        builder.add("gpg_key_fp",
                StringUtils.defaultString(src.getGPGKeyFp()));
        builder.add("gpg_check", src.isGPGCheck());

        List<ContentSource> csList = new ArrayList<>(src.getSources().size());
        if (!src.getSources().isEmpty()) {
            csList.addAll(src.getSources());
            builder.add("yumrepo_last_sync", src.getLastSynced());
        }
        builder.add("contentSources", csList);

        if (src.getEndOfLife() != null) {
            builder.add("end_of_life", src.getEndOfLife().toString());
        }
        else {
            builder.add("end_of_life", "");
        }

        Channel parent = src.getParentChannel();
        if (parent != null) {
            builder.add("parent_channel_label", parent.getLabel());
        }
        else {
            builder.add("parent_channel_label", "");
        }

        Channel orig = ChannelFactory.lookupOriginalChannel(src);
        if (orig != null) {
            builder.add("clone_original", orig.getLabel());
        }
        else {
            builder.add("clone_original", "");
        }

        return builder.build();
    }
}
