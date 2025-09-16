/*
 * Copyright (c) 2024 SUSE LLC
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
package com.suse.manager.xmlrpc.serializer;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;
import com.suse.manager.model.attestation.CoCoAttestationResult;

/**
 * Convert an attestation result into a XMLRPC &lt;struct&gt;.
 *
 * @apidoc.doc
 *   #struct_begin("result")
 *     #prop("int", "result_id")
 *     #prop("string", "result_type")
 *     #prop("string", "result_status")
 *     #prop("string", "result_description")
 *     #prop("$date", "result_attested")
 *     #prop("string", "result_details")
 *   #struct_end()
 */
public class CoCoAttestationResultSerializer extends ApiResponseSerializer<CoCoAttestationResult> {
    @Override
    public Class<CoCoAttestationResult> getSupportedClass() {
        return CoCoAttestationResult.class;
    }

    @Override
    public SerializedApiResponse serialize(CoCoAttestationResult src) {
        SerializationBuilder builder = new SerializationBuilder()
                .add("result_id", src.getId())
                .add("result_type", src.getResultType().name())
                .add("result_status", src.getStatus().name())
                .add("result_description", src.getDescription())
                .add("result_attested", src.getAttested());
        src.getDetailsOpt().ifPresent(d -> builder.add("result_details", d));
        return builder.build();
    }
}
