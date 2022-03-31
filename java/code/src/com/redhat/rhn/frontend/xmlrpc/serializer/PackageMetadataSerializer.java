/*
 * Copyright (c) 2009--2016 Red Hat, Inc.
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

import com.redhat.rhn.frontend.dto.PackageMetadata;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * Converts PackageMetadata to an XMLRPC &lt;struct&gt;.
 *
 * @xmlrpc.doc
 *  #struct_begin("Package Metadata")
 *      #prop("int", "package_name_id")
 *      #prop("string", "package_name")
 *      #prop("string", "package_epoch")
 *      #prop("string", "package_version")
 *      #prop("string", "package_release")
 *      #prop("string", "package_arch")
 *      #prop_desc("string", "this_system", "Version of package on this system.")
 *      #prop_desc("string", "other_system", "Version of package on the other system.")
 *      #prop("int", "comparison")
 *          #options()
 *              #item("0 - No difference.")
 *              #item("1 - Package on this system only.")
 *              #item("2 - Newer package version on this system.")
 *              #item("3 - Package on other system only.")
 *              #item("4 - Newer package version on other system.")
 *           #options_end()
 *   #struct_end()
 *
 *
 */
public class PackageMetadataSerializer extends ApiResponseSerializer<PackageMetadata> {

    @Override
    public Class<PackageMetadata> getSupportedClass() {
        return PackageMetadata.class;
    }

    @Override
    public SerializedApiResponse serialize(PackageMetadata src) {
        return new SerializationBuilder()
                .add("package_name_id", src.getId())
                .add("package_name", src.getName())
                .add("package_epoch", src.getEpoch())
                .add("package_version", src.getVersion())
                .add("package_release", src.getRelease())
                .add("package_arch", src.getArch())
                .add("this_system", src.getSystemEvr())
                .add("other_system", src.getOtherEvr())
                .add("comparison", src.getComparisonAsInt())
                .build();
    }
}
