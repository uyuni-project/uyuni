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

import com.redhat.rhn.domain.action.ActionFactory;
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
 * #struct_begin("Image Overview information")
 *   #prop("int", "id")
 *   #prop_desc("string", "name", "image name")
 *   #prop_desc("string", "type", "image type")
 *   #prop_desc("string", "version", "image tag/version")
 *   #prop_desc("int", "revision", "image build revision number")
 *   #prop_desc("string", "arch", "image architecture")
 *   #prop_desc("boolean", "external", "true if the image is built externally,
 *          false otherwise")
 *   #prop("string", "checksum")
 *   #prop("string", "profileLabel")
 *   #prop("string", "storeLabel")
 *   #prop_desc("string", "buildStatus", "One of:")
 *            #options()
 *              #item("queued")
 *              #item("picked up")
 *              #item("completed")
 *              #item("failed")
 *            #options_end()
 *   #prop_desc("string", "inspectStatus", "Available if the build is successful. One of:")
 *            #options()
 *              #item("queued")
 *              #item("picked up")
 *              #item("completed")
 *              #item("failed")
 *            #options_end()
 *   #prop("int", "buildServerId")
 *   #prop("int", "securityErrata")
 *   #prop("int", "bugErrata")
 *   #prop("int", "enhancementErrata")
 *   #prop("int", "outdatedPackages")
 *   #prop("int", "installedPackages")
 *   #prop_desc("boolean", "obsolete", "true if the image has been replaced in the store")
 *   #prop_desc("struct", "files", "image files")
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
        helper.add("type", image.getImageType());
        helper.add("version", image.getVersion());
        helper.add("revision", image.getCurrRevisionNum());
        helper.add("arch", image.getArch());
        helper.add("external", image.isExternalImage());
        helper.add("checksum", chk != null ? chk.getChecksum() : "");
        helper.add("profileLabel", prof != null ? prof.getLabel() : "");
        helper.add("storeLabel", store != null ? store.getLabel() : "");
        helper.add("buildServerId", buildhost != null ? buildhost.getId() : 0);
        helper.add("securityErrata", image.getSecurityErrata());
        helper.add("bugErrata", image.getBugErrata());
        helper.add("enhancementErrata", image.getEnhancementErrata());
        helper.add("outdatedPackages", image.getOutdatedPackages());
        helper.add("installedPackages", image.getInstalledPackages());
        image.getBuildServerAction().ifPresentOrElse(
                ba -> helper.add("buildStatus", ba.getStatus().getName().toLowerCase()),
                () -> helper.add("buildStatus",
                        (image.isBuilt() ? ActionFactory.STATUS_COMPLETED : ActionFactory.STATUS_FAILED)
                        .getName().toLowerCase()));

        image.getInspectServerAction().ifPresent(
                ia -> helper.add("inspectStatus", ia.getStatus().getName().toLowerCase()));
        helper.add("files", image.getImageFiles());
        helper.add("obsolete", image.isObsolete());

        helper.writeTo(writer);
    }

    @Override
    public Class getSupportedClass() {
        return ImageOverview.class;
    }
}
