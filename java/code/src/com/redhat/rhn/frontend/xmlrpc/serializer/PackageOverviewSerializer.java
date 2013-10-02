/**
 * Copyright (c) 2009--2013 Red Hat, Inc.
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

import org.apache.commons.lang.StringUtils;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

import com.redhat.rhn.frontend.dto.PackageOverview;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

/**
 * PackageOverviewSerializer
 * @version $Rev$
 *
 * @xmlrpc.doc
 *   #struct("package overview")
 *   #prop("int", "id")
 *   #prop("string", "name")
 *   #prop("string", "summary")
 *   #prop("string", "description")
 *   #prop("string", "version")
 *   #prop("string", "release")
 *   #prop("string", "arch")
 *   #prop("string", "epoch")
 *   #prop("string", "provider")
 *   #struct_end()
 */
public class PackageOverviewSerializer extends RhnXmlRpcCustomSerializer {
    /**
     * {@inheritDoc}
     */
    public Class getSupportedClass() {
        return PackageOverview.class;
    }

    /** {@inheritDoc} */
    protected void doSerialize(Object value, Writer output, XmlRpcSerializer serializer)
        throws XmlRpcException, IOException {
        PackageOverview pO = (PackageOverview)value;
        SerializerHelper helper = new SerializerHelper(serializer);
        helper.add("id", pO.getId());
        helper.add("name", pO.getPackageName());
        helper.add("summary", pO.getSummary());
        helper.add("description", StringUtils.defaultString(pO.getDescription()));
        helper.add("version", pO.getVersion());
        helper.add("release", pO.getRelease());
        String epoch = pO.getEpoch();
        if (epoch == null) {
            epoch = "";
        }
        helper.add("epoch", epoch);
        helper.add("arch", pO.getPackageArch());


        helper.add("nvre", pO.getPackageNvre());
        helper.add("nvrea", pO.getNvrea());
        helper.add("packageChannels", pO.getPackageChannels());
        helper.add("provider", pO.getProvider());
        helper.writeTo(output);
    }
}
