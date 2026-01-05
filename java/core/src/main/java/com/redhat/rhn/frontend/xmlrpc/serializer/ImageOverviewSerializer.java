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

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * ImageOverviewSerializer
 * @apidoc.doc
 * #struct_begin("image overview information")
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
 *   #prop_desc("struct", "files", "image files")
 *   #prop_desc("boolean", "obsolete", "true if the image has been replaced in the store")
 * #struct_end()
 */
public class ImageOverviewSerializer extends ApiResponseSerializer<ImageOverview> {

    @Override
    public Class<ImageOverview> getSupportedClass() {
        return ImageOverview.class;
    }

    @Override
    public SerializedApiResponse serialize(ImageOverview src) {
        Checksum chk = src.getChecksum();
        ImageProfile prof = src.getProfile();
        ImageStore store = src.getStore();
        MinionServer buildhost = src.getBuildServer();

        SerializationBuilder builder = new SerializationBuilder()
                .add("id", src.getId())
                .add("name", src.getName())
                .add("type", src.getImageType())
                .add("version", src.getVersion())
                .add("revision", src.getCurrRevisionNum())
                .add("arch", src.getArch())
                .add("external", src.isExternalImage())
                .add("checksum", chk != null ? chk.getChecksum() : "")
                .add("profileLabel", prof != null ? prof.getLabel() : "")
                .add("storeLabel", store != null ? store.getLabel() : "")
                .add("buildServerId", buildhost != null ? buildhost.getId() : 0)
                .add("securityErrata", src.getSecurityErrata())
                .add("bugErrata", src.getBugErrata())
                .add("enhancementErrata", src.getEnhancementErrata())
                .add("outdatedPackages", src.getOutdatedPackages())
                .add("installedPackages", src.getInstalledPackages());
        src.getBuildServerAction().ifPresentOrElse(
                ba -> builder.add("buildStatus", ba.getStatus().getName().toLowerCase()),
                () -> builder.add("buildStatus",
                        (src.isBuilt() ? ActionFactory.STATUS_COMPLETED : ActionFactory.STATUS_FAILED)
                                .getName().toLowerCase()));
        src.getInspectServerAction().ifPresent(
                ia -> builder.add("inspectStatus", ia.getStatus().getName().toLowerCase()));
        builder.add("files", src.getImageFiles());
        builder.add("obsolete", src.isObsolete());

        return builder.build();
    }
}
