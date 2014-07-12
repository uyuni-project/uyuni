/**
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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

/**
 *
 * ChannelSerializer
 * @version $Rev$
 *
 * @xmlrpc.doc
 *  #struct("channel")
 *      #prop("int", "id")
 *      #prop("string", "name")
 *      #prop("string", "label")
 *      #prop("string", "arch_name")
 *      #prop("string", "arch_label")
 *      #prop("string", "summary")
 *      #prop("string", "description")
 *      #prop("string", "checksum_label")
 *      #prop("dateTime.iso8601", "last_modified")
 *      #prop("string", "maintainer_name")
 *      #prop("string", "maintainer_email")
 *      #prop("string", "maintainer_phone")
 *      #prop("string", "support_policy")
 *      #prop("string", "gpg_key_url")
 *      #prop("string", "gpg_key_id")
 *      #prop("string", "gpg_key_fp")
 *      #prop_desc("dateTime.iso8601", "yumrepo_last_sync", "(optional)")
 *      #prop("string", "end_of_life")
 *      #prop("string", "parent_channel_label")
 *      #prop("string", "clone_original")
 *      #array()
 *          #struct("contentSources")
 *              #prop("int", "id")
 *              #prop("string", "label")
 *              #prop("string", "sourceUrl")
 *              #prop("string", "type")
 *          #struct_end()
 *      #array_end()
 *  #struct_end()
 *
 */
public class ChannelSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    public Class getSupportedClass() {
        return Channel.class;
    }

    /**
     * {@inheritDoc}
     */
    protected void doSerialize(Object value, Writer output, XmlRpcSerializer serializer)
        throws XmlRpcException, IOException {
        SerializerHelper helper = new SerializerHelper(serializer);
        Channel c = (Channel) value;


        helper.add("id", c.getId());
        helper.add("label", c.getLabel());
        helper.add("name", c.getName());
        helper.add("arch_name",
                StringUtils.defaultString(c.getChannelArch().getName()));
        helper.add("arch_label",
                StringUtils.defaultString(c.getChannelArch().getLabel()));
        helper.add("summary", StringUtils.defaultString(c.getSummary()));
        helper.add("description",
                StringUtils.defaultString(c.getDescription()));
        helper.add("checksum_label", c.getChecksumTypeLabel());
        helper.add("last_modified", c.getLastModified());
        helper.add("maintainer_name",
                StringUtils.defaultString(c.getMaintainerName()));
        helper.add("maintainer_email",
                StringUtils.defaultString(c.getMaintainerEmail()));
        helper.add("maintainer_phone",
                StringUtils.defaultString(c.getMaintainerPhone()));
        helper.add("support_policy",
                StringUtils.defaultString(c.getSupportPolicy()));

        helper.add("gpg_key_url",
                StringUtils.defaultString(c.getGPGKeyUrl()));
        helper.add("gpg_key_id",
                StringUtils.defaultString(c.getGPGKeyId()));
        helper.add("gpg_key_fp",
                StringUtils.defaultString(c.getGPGKeyFp()));

        List<ContentSource> csList = new ArrayList<ContentSource>(c.getSources().size());
        if (!c.getSources().isEmpty()) {
            for (Iterator itr = c.getSources().iterator(); itr.hasNext();) {
                ContentSource cs = (ContentSource) itr.next();
                csList.add(cs);
            }
            helper.add("yumrepo_last_sync", c.getLastSynced());
        }
        helper.add("contentSources", csList);

        if (c.getEndOfLife() != null) {
            helper.add("end_of_life", c.getEndOfLife().toString());
        }
        else {
            helper.add("end_of_life", "");
        }

        Channel parent = c.getParentChannel();
        if (parent != null) {
            helper.add("parent_channel_label", parent.getLabel());
        }
        else {
            helper.add("parent_channel_label", "");
        }

        Channel orig = ChannelFactory.lookupOriginalChannel(c);
        if (orig != null) {
            helper.add("clone_original", orig.getLabel());
        }
        else {
            helper.add("clone_original", "");
        }

        helper.writeTo(output);
    }
}
