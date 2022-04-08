/*
 * Copyright (c) 2016 SUSE LLC
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

import com.redhat.rhn.frontend.xmlrpc.system.SUSEInstalledProduct;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * Converts an InstalledProduct object for representation as an XMLRPC struct.
 *
 * @xmlrpc.doc
 * #struct_begin("installed product")
 *     #prop("string", "name")
 *     #prop("boolean", "isBaseProduct")
 *     #prop_desc("string", "version", "returned only if applies")
 *     #prop_desc("string", "arch", "returned only if applies")
 *     #prop_desc("string", "release", "returned only if applies")
 *     #prop_desc("string", "friendlyName", "returned only if available")
 * #struct_end()
 *
 */
public class SUSEInstalledProductSerializer extends ApiResponseSerializer<SUSEInstalledProduct> {

    @Override
    public Class<SUSEInstalledProduct> getSupportedClass() {
        return SUSEInstalledProduct.class;
    }

    @Override
    public SerializedApiResponse serialize(SUSEInstalledProduct src) {
        SerializationBuilder builder = new SerializationBuilder()
                .add("name", src.getName())
                .add("isBaseProduct", src.isBaseproduct());

        if (src.getVersion() != null) {
            builder.add("version", src.getVersion());
        }
        if (src.getArch() != null) {
            builder.add("arch", src.getArch());
        }
        if (src.getRelease() != null) {
            builder.add("release", src.getRelease());
        }
        if (src.getFriendlyName() != null) {
            builder.add("friendlyName", src.getFriendlyName());
        }

        return builder.build();
    }
}
