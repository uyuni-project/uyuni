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
import com.redhat.rhn.domain.image.ImageOverview;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

import java.io.IOException;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 * ImageOverviewSerializer
 * @xmlrpc.doc
 * #struct("Image Overview information")
 *   #prop("int", "id")
 *   #prop("string", "name")
 *   #prop("string", "version")
 *   #prop("string", "arch")
 *   #prop("string", "checksum")
 *   #prop("string", "profileLabel")
 *   #prop("int", "buildServerId")
 *   #prop("int", "securityErrata")
 *   #prop("int", "bugErrata")
 *   #prop("int", "enhancementErrata")
 *   #prop("int", "outdatedPackages")
 * #struct_end()
 */
public class ImageOverviewSerializer extends RhnXmlRpcCustomSerializer {

    @Override
    protected void doSerialize(Object value, Writer writer, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        SerializerHelper helper = new SerializerHelper(serializer);
        ImageOverview image = (ImageOverview) value;
        Checksum chk = image.getChecksum();
        ImageProfile prof = image.getProfile();
        ImageStore store = image.getStore();
        MinionServer buildhost = image.getBuildServer();

        helper.add("id", image.getId());
        helper.add("name", image.getName());
        helper.add("version", image.getVersion());
        helper.add("arch", image.getArch());
        helper.add("checksum", chk != null ? chk.getChecksum() : "");
        helper.add("profileLabel", prof != null ? prof.getLabel() : "");
        helper.add("storeLabel", store != null ? store.getLabel() : "");
        helper.add("buildServerId", buildhost != null ? buildhost.getId() : 0);
        helper.add("securityErrata", image.getSecurityErrata());
        helper.add("bugErrata", image.getBugErrata());
        helper.add("enhancementErrata", image.getEnhancementErrata());
        helper.add("outdatedPackages", image.getOutdatedPackages());
        helper.writeTo(writer);
    }

    @Override
    public Class getSupportedClass() {
        return ImageOverview.class;
    }
}
