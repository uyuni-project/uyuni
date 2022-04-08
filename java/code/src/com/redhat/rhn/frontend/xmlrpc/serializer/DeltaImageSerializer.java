/*
 * Copyright (c) 2022 SUSE LLC
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

import com.redhat.rhn.domain.image.DeltaImageInfo;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * DeltaImageSerializer
 * @xmlrpc.doc
 * #struct_begin("Delta Image information")
 *   #prop("int", "source_id")
 *   #prop("int", "target_id")
 *   #prop_desc("string", "file", "file path")
 *   #prop_desc("struct", "pillar", "pillar data")
 * #struct_end()
 */
public class DeltaImageSerializer extends ApiResponseSerializer<DeltaImageInfo> {

    @Override
    public Class<DeltaImageInfo> getSupportedClass() {
        return DeltaImageInfo.class;
    }

    @Override
    public SerializedApiResponse serialize(DeltaImageInfo src) {
        return new SerializationBuilder()
                .add("source_id", src.getSourceImageInfo().getId())
                .add("target_id", src.getTargetImageInfo().getId())
                .add("file", src.getFile())
                .add("pillar", src.getPillar().getPillar())
                .build();
    }
}
