/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.redhat.rhn.frontend.xmlrpc.serializer;

import com.redhat.rhn.manager.errata.syncwithpackages.dto.SyncedErratumWithPackages;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import java.util.Map;

/**
 * SyncedErratumSerializer
 *
 * @apidoc.doc
 *  #struct_begin("errata")
 *      #prop_desc("int", "id", "the id of the representative erratum in the target channel")
 *      #prop("string", "advisory_name")
 *      #prop_array_begin_desc("packages", "the packages the target channel serves for the advisory")
 *          #struct_begin("package")
 *              #prop("int", "id")
 *              #prop("string", "name_evra")
 *          #struct_end()
 *      #prop_array_end()
 *  #struct_end()
 */
public class SyncedErratumSerializer extends ApiResponseSerializer<SyncedErratumWithPackages> {

    @Override
    public Class<SyncedErratumWithPackages> getSupportedClass() {
        return SyncedErratumWithPackages.class;
    }

    @Override
    public SerializedApiResponse serialize(SyncedErratumWithPackages src) {
        return new SerializationBuilder()
                .add("id", src.errata().getId())
                .add("advisory_name", src.errata().getAdvisoryName())
                .add("packages", src.packages().stream()
                        .map(p -> Map.of("id", p.getId(), "name_evra", p.getNameEvra()))
                        .toList())
                .build();
    }
}
