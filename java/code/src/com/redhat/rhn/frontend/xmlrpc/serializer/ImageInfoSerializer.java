/*
 * Copyright (c) 2017 SUSE LLC
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

import com.redhat.rhn.domain.common.Checksum;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

import java.io.IOException;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 * ImageInfoSerializer
 * @xmlrpc.doc
 * #struct_begin("Image information")
 *   #prop("int", "id")
 *   #prop_desc("string", "name", "image name")
 *   #prop_desc("string", "version", "image tag/version")
 *   #prop_desc("int", "revision", "image build revision number")
 *   #prop_desc("string", "arch", "image architecture")
 *   #prop_desc("boolean", "external", "true if the image is built externally,
 *          false otherwise")
 *   #prop("string", "storeLabel")
 *   #prop("string", "checksum")
 * #struct_end()
 */
public class ImageInfoSerializer extends RhnXmlRpcCustomSerializer {

    @Override
    protected void doSerialize(Object value, Writer writer, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        SerializerHelper helper = new SerializerHelper(serializer);
        ImageInfo image = (ImageInfo) value;
        Checksum chk = image.getChecksum();
        ImageStore store = image.getStore();
        helper.add("id", image.getId());
        helper.add("name", image.getName());
        helper.add("version", image.getVersion());
        helper.add("revision", image.getRevisionNumber());
        helper.add("arch", image.getImageArch().getLabel());
        helper.add("external", image.isExternalImage());
        helper.add("storeLabel", store != null ? store.getLabel() : "");
        helper.add("checksum", chk != null ? chk.getChecksum() : "");
        helper.writeTo(writer);
    }

    @Override
    public Class getSupportedClass() {
        return ImageInfo.class;
    }
}
