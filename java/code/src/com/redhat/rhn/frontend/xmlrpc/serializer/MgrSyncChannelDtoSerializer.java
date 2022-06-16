/**
 * Copyright (c) 2018 SUSE LLC
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

import com.redhat.rhn.domain.product.MgrSyncChannelDto;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

import java.io.IOException;
import java.io.Writer;
import java.util.Optional;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 * Serializer for {@link MgrSyncChannelDto} objects.
 *
 * @xmlrpc.doc
 *   #struct_begin("channel")
 *     #prop_desc("string", "arch", "architecture of the channel")
 *     #prop_desc("string", "description", "description of the channel")
 *     #prop_desc("string", "family", "channel family label")
 *     #prop_desc("boolean", "is_signed", "channel has signed metadata")
 *     #prop_desc("string", "label", "label of the channel")
 *     #prop_desc("string", "name", "name of the channel")
 *     #prop_desc("boolean", "optional", "channel is optional")
 *     #prop_desc("string", "parent", "the label of the parent channel")
 *     #prop_desc("string", "product_name", "product name")
 *     #prop_desc("string", "product_version", "product version")
 *     #prop_desc("string", "source_url", "repository source URL")
 *     #prop_desc("string", "status", "'available', 'unavailable' or 'installed'")
 *     #prop_desc("string", "summary", "channel summary")
 *     #prop_desc("string", "update_tag", "update tag")
 *     #prop_desc("boolean", "installer_updates", "is an installer update channel")
 *   #struct_end()
 */
public class MgrSyncChannelDtoSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<MgrSyncChannelDto> getSupportedClass() {
        return MgrSyncChannelDto.class;
    }

    @Override
    protected void doSerialize(Object obj, Writer writer, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        MgrSyncChannelDto channel = (MgrSyncChannelDto) obj;
        SerializerHelper helper = new SerializerHelper(serializer);

        helper.add("arch", channel.getArch().orElse(PackageFactory.lookupPackageArchByLabel("noarch")).getLabel());
        helper.add("description", channel.getDescription());
        helper.add("family", channel.getFamily());
        helper.add("is_signed", channel.isSigned());
        helper.add("label", channel.getLabel());
        helper.add("name", channel.getName());
        helper.add("optional", !channel.isMandatory());
        helper.add("parent", Optional.ofNullable(channel.getParentLabel()).orElse("BASE"));
        helper.add("product_name", channel.getProductName());
        helper.add("product_version", channel.getProductVersion());
        helper.add("source_url", channel.getSourceUrl());
        helper.add("status", channel.getStatus().name());
        helper.add("summary", channel.getSummary());
        helper.add("update_tag", channel.getUpdateTag());
        helper.add("installer_updates", channel.isInstallerUpdates());
        helper.writeTo(writer);
    }
}
