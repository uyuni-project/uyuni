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

import com.redhat.rhn.domain.rhnpackage.PackageNevra;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;


/**
 * ServerSerializer: Converts a Server object for representation as an XMLRPC struct.
 * Includes full server details, which may be more data than some calls would like.
 *
 *
 * @xmlrpc.doc
 *  #struct_begin("package nevra")
 *      #prop("string", "name")
 *      #prop("string", "epoch")
 *      #prop("string", "version")
 *      #prop("string", "release")
 *      #prop("string", "arch")
 *  #struct_end()
 */
public class PackageNevraSerializer extends ApiResponseSerializer<PackageNevra> {

    @Override
    public Class<PackageNevra> getSupportedClass() {
        return PackageNevra.class;
    }

    @Override
    public SerializedApiResponse serialize(PackageNevra src) {
        return new SerializationBuilder()
                .add("name", src.getName().getName())
                .add("epoch", src.getEvr().getEpoch())
                .add("version", src.getEvr().getVersion())
                .add("release", src.getEvr().getRelease())
                .add("arch", src.getArch().getLabel())
                .build();
    }
}
