/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.frontend.xmlrpc.serializer;

import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.frontend.xmlrpc.channel.software.dto.SyncErrataWithPackagesResponse;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * ErrataSerializer
 *
 * @apidoc.doc
 *      #struct_begin("errata package info")
 *          #prop_array_begin("erratas")
 *              #struct_begin("errata")
 *                  #prop_desc("int", "id", "errata ID")
 *                  #prop_desc("string", "date", "the date erratum was created")
 *                  #prop_desc("string", "advisory_type", "type of the advisory")
 *                  #prop_desc("string", "advisory_status", "status of the advisory")
 *                  #prop_desc("string", "advisory_name", "name of the advisory")
 *                  #prop_desc("string", "advisory_synopsis", "summary of the erratum")
 *              #struct_end()
 *          #array_end()
 *          #prop_array_begin("packages")
 *              #struct_begin("package")
 *                  #prop("string", "name")
 *                  #prop("string", "version")
 *                  #prop("string", "release")
 *                  #prop("string", "epoch")
 *                  #prop("int", "id")
 *                  #prop("string", "arch_label")
 *                  #prop("$date", "last_modified")
 *                  #prop_desc("string", "path", "the path on that file system that the package resides")
 *                  #prop_desc("boolean", "part_of_retracted_patch", "true if the package is a part of a retracted
 *                  patch")
 *                  #prop_desc("string", "provider", "the provider of the package, determined by
 *                          the gpg key it was signed with.")
 *              #struct_end()
 *          #array_end()
 *     #struct_end()
 */
public class SyncErrataWithPackagesResponseSerializer extends ApiResponseSerializer<SyncErrataWithPackagesResponse> {

    @Override
    public Class<SyncErrataWithPackagesResponseSerializer> getSupportedClass() {
        return SyncErrataWithPackagesResponseSerializer.class;
    }

    @Override
    public SerializedApiResponse serialize(SyncErrataWithPackagesResponse src) {
        SerializationBuilder builder = new SerializationBuilder();

        Set<Errata> erratas = new HashSet<>(src.erratas().size());
        if (!src.erratas().isEmpty()) {
            erratas.addAll(src.erratas());
        }
        builder.add("errata", erratas);

        Set<Package> packages = new HashSet<>(src.packages().size());
        if (!src.packages().isEmpty()) {
            packages.addAll(src.packages());
        }
        builder.add("package", packages);

        return builder.build();
    }

}
