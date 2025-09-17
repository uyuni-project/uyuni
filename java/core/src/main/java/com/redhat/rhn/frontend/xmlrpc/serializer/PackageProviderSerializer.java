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

import com.redhat.rhn.domain.rhnpackage.PackageProvider;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;


/**
 * PackageKeySerializer
 *
 * @apidoc.doc
 *   #struct_begin("package provider")
 *   #prop("string", "name")
 *   #prop_array_begin("keys")
 *      $PackageKeySerializer
 *   #prop_array_end()
 *   #struct_end()
 */
public class PackageProviderSerializer extends ApiResponseSerializer<PackageProvider> {

    @Override
    public Class<PackageProvider> getSupportedClass() {
        return PackageProvider.class;
    }

    @Override
    public SerializedApiResponse serialize(PackageProvider src) {
        return new SerializationBuilder()
                .add("name", src.getName())
                .add("keys", src.getKeys())
                .build();
    }
}
