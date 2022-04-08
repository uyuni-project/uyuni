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


import com.redhat.rhn.domain.server.Dmi;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * DmiSerializer
 * @xmlrpc.doc
 *      #struct_begin("DMI")
 *          #prop("string", "vendor")
 *          #prop("string", "system")
 *          #prop("string", "product")
 *          #prop("string", "asset")
 *          #prop("string", "board")
 *          #prop_desc("string", "bios_release", "(optional)")
 *          #prop_desc("string", "bios_vendor", "(optional)")
 *          #prop_desc("string", "bios_version", "(optional)")
 *      #struct_end()
 */
public class DmiSerializer extends ApiResponseSerializer<Dmi> {

    @Override
    public Class<Dmi> getSupportedClass() {
        return Dmi.class;
    }

    @Override
    public SerializedApiResponse serialize(Dmi src) {
        SerializationBuilder builder = new SerializationBuilder()
                .add("vendor", StringUtils.defaultString(src.getVendor()))
                .add("system", StringUtils.defaultString(src.getSystem()))
                .add("product", StringUtils.defaultString(src.getProduct()))
                .add("asset", StringUtils.defaultString(src.getAsset()))
                .add("board", StringUtils.defaultString(src.getBoard()));
        if (src.getBios() != null) {
            builder.add("bios_release", StringUtils.defaultString(src.getBios().getRelease()))
                    .add("bios_vendor", StringUtils.defaultString(src.getBios().getVendor()))
                    .add("bios_version", StringUtils.defaultString(src.getBios().getVersion()));
        }
        return builder.build();
    }
}
