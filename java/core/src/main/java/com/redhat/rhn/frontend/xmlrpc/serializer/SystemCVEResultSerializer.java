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

import com.redhat.rhn.manager.audit.SystemCVEResult;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import java.util.List;

/**
 * SystemCVEResultSerializer
 *
 * @apidoc.doc
 *
 * #struct_begin("CVE affecting a system")
 *     #prop("int", "cve_id")
 *     #prop("string", "cve_name")
 *     #prop("string", "patch_status")
 *         #options()
 *             #item_desc ("AFFECTED_PATCH_UNAVAILABLE", "affected, patch is unavailable anywhere")
 *             #item_desc ("AFFECTED_PATCH_UNAVAILABLE_IN_UYUNI",
 *                "affected, patch is unavailable in any of the synced channels")
 *             #item_desc ("AFFECTED_PATCH_INAPPLICABLE", "affected, patch available in unassigned channel")
 *             #item_desc ("AFFECTED_PARTIAL_PATCH_APPLICABLE",
 *                "affected, partial patch available in assigned channel")
 *             #item_desc ("AFFECTED_FULL_PATCH_APPLICABLE", "affected, full patch available in assigned channel")
 *             #item_desc ("AFFECTED_PATCH_INAPPLICABLE_SUCCESSOR_PRODUCT",
 *                "affected, patch available in a Product Migration target")
 *             #item_desc ("PATCHED", "patched")
 *         #options_end()
 *     #prop("string", "severity")
 *         #options()
 *             #item ("CRITICAL")
 *             #item ("IMPORTANT")
 *             #item ("MODERATE")
 *             #item ("LOW")
 *             #item_desc ("UNKNOWN", "no severity is available in the OVAL data")
 *         #options_end()
 *     #prop_array_begin("errata")
 *         #struct_begin("patch")
 *             #prop("int", "id")
 *             #prop("string", "advisory")
 *             #prop("string", "channel_label")
 *             #prop_desc("boolean", "assigned", "true if the channel is assigned to the system")
 *         #struct_end()
 *     #prop_array_end()
 * #struct_end()
 *
 */
public class SystemCVEResultSerializer extends ApiResponseSerializer<SystemCVEResult> {

    @Override
    public Class<SystemCVEResult> getSupportedClass() {
        return SystemCVEResult.class;
    }

    @Override
    public SerializedApiResponse serialize(SystemCVEResult src) {
        List<SerializedApiResponse> errata = src.getErrata().stream()
                .map(e -> new SerializationBuilder()
                        .add("id", e.id())
                        .add("advisory", e.advisory())
                        .add("channel_label", e.channelLabel())
                        .add("assigned", e.assigned())
                        .build())
                .toList();

        return new SerializationBuilder()
                .add("cve_id", src.getId())
                .add("cve_name", src.getCveName())
                .add("patch_status", src.getStatus().toString())
                .add("severity", src.getSeverity().toString())
                .add("errata", errata)
                .build();
    }
}
