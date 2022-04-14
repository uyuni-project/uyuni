/*
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

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.PackageKey;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * PackageSerializer
 * @xmlrpc.doc
 * #struct_begin("package")
 *      #prop("string", "name")
 *      #prop("string", "version")
 *      #prop("string", "release")
 *      #prop("string", "epoch")
 *      #prop("int", "id")
 *      #prop("string", "arch_label")
 *      #prop("dateTime.iso8601", "last_modified")
 *      #prop_desc("string", "path", "The path on that file system that the package
 *             resides")
 *      #prop_desc("boolean", "part_of_retracted_patch", "True if the package is a part of a retracted patch")
 *      #prop_desc("string", "provider", "The provider of the package, determined by
 *              the gpg key it was signed with.")
 *  #struct_end()
 *
 */
public class PackageSerializer extends ApiResponseSerializer<Package> {

    @Override
    public Class<Package> getSupportedClass() {
        return Package.class;
    }

    @Override
    public SerializedApiResponse serialize(Package src) {
        String provider = LocalizationService.getInstance().getMessage("channel.jsp.gpgunknown");
        if (src.getPackageKeys() != null) {
            for (PackageKey key : src.getPackageKeys()) {
                if (key.getType().equals(PackageFactory.PACKAGE_KEY_TYPE_GPG) && key.getProvider() != null) {
                    provider = key.getProvider().getName();
                }
            }
        }

        return new SerializationBuilder()
                .add("name", src.getPackageName().getName())
                .add("version", src.getPackageEvr().getVersion())
                .add("release", src.getPackageEvr().getRelease())
                .add("epoch", StringUtils.defaultString(src.getPackageEvr().getEpoch()))
                .add("id", src.getId())
                .add("arch_label", src.getPackageArch().getLabel())
                .add("last_modified", src.getLastModified())
                .add("path", src.getPath())
                .add("part_of_retracted_patch", src.isPartOfRetractedPatch())
                .add("provider", provider)
                .build();
    }
}
