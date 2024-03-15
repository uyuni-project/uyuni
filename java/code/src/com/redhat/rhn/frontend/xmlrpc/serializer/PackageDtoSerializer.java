/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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

import com.redhat.rhn.frontend.dto.PackageDto;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import java.util.Optional;

/**
 *
 * PackageSerializer
 * @apidoc.doc
 * #struct_begin("package")
 *      #prop("string", "name")
 *      #prop("string", "version")
 *      #prop("string", "release")
 *      #prop("string", "epoch")
 *      #prop("string", "checksum")
 *      #prop("string", "checksum_type")
 *      #prop("int", "id")
 *      #prop("string", "arch_label")
 *      #prop("string", "last_modified_date")
 *      #prop_desc("string", "last_modified", "(deprecated)")
 *  #struct_end()
 *
 */
public class PackageDtoSerializer extends ApiResponseSerializer<PackageDto> {

    @Override
    public Class<PackageDto> getSupportedClass() {
        return PackageDto.class;
    }

    @Override
    public SerializedApiResponse serialize(PackageDto src) {
        SerializationBuilder builder = new SerializationBuilder()
                .add("name", src.getName())
                .add("epoch", Optional.ofNullable(src.getEpoch()).orElse(""))
                .add("version", src.getVersion())
                .add("release", src.getRelease())
                .add("checksum", src.getChecksum())
                .add("checksum_type", src.getChecksumType())
                .add("id", src.getId())
                .add("arch_label", src.getArchLabel())
                .add("last_modified_date", src.getLastModified());

        if (src.getRetracted() != null) {
            builder.add("retracted", src.getRetracted());
        }

        // Deprecated and should eventually be removed, were returning this
        // for some time although it was undocumented. All other occurrences of
        // last_modified are actual date objects, whereas last_modified_date is
        // used whenever we return a string.
        builder.add("last_modified", src.getLastModified());

        return builder.build();
    }
}
