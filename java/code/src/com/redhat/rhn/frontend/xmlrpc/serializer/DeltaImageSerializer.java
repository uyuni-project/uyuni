/*
 * Copyright (c) 2022 SUSE LLC
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

import com.redhat.rhn.domain.image.DeltaImageInfo;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

import java.io.IOException;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 * DeltaImageSerializer
 * @xmlrpc.doc
 * #struct_begin("Delta Image information")
 *   #prop("int", "source_id")
 *   #prop("int", "target_id")
 *   #prop_desc("string", "file", "file path")
 *   #prop_desc("struct", "pillar", "pillar data")
 * #struct_end()
 */
public class DeltaImageSerializer extends RhnXmlRpcCustomSerializer {

    @Override
    protected void doSerialize(Object value, Writer writer, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        SerializerHelper helper = new SerializerHelper(serializer);
        DeltaImageInfo delta = (DeltaImageInfo) value;
        helper.add("source_id", delta.getSourceImageInfo().getId());
        helper.add("target_id", delta.getTargetImageInfo().getId());
        helper.add("file", delta.getFile());
        helper.add("pillar", delta.getPillar().getPillar());
        helper.writeTo(writer);
    }

    @Override
    public Class getSupportedClass() {
        return DeltaImageInfo.class;
    }
}
