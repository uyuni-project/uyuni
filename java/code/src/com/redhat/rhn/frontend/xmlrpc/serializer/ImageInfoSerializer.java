/**
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
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

import java.io.IOException;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 * ImageInfoSerializer
 */
public class ImageInfoSerializer extends RhnXmlRpcCustomSerializer {

    @Override
    protected void doSerialize(Object value, Writer writer, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        SerializerHelper helper = new SerializerHelper(serializer);
        ImageInfo image = (ImageInfo) value;
        Checksum chk = image.getChecksum();
        helper.add("id", image.getId());
        helper.add("name", image.getName());
        helper.add("version", image.getVersion());
        helper.add("arch", image.getImageArch().getLabel());
        helper.add("checksum", chk != null ? chk.getChecksum() : "");
        helper.writeTo(writer);
    }

    @Override
    public Class getSupportedClass() {
        return ImageInfo.class;
    }
}
