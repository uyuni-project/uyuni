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
import com.suse.manager.model.attestation.ServerCoCoAttestationReport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convert an attestation report into a XMLRPC &lt;struct&gt;.
 *
 * @apidoc.doc
 *   #struct_begin("reportResults")
 *     #prop("int", "report_id")
 *     #prop("string", "report_status")
 *     #prop("$date", "report_created")
 *     #prop("$date", "report_modified")
 *     #prop_array_begin("results")
 *       #struct_begin("result")
 *         #prop("int", "result_id")
 *         #prop("string", "result_type")
 *         #prop("string", "result_status")
 *         #prop("string", "result_description")
 *         #prop("$date", "result_attested")
 *       #struct_end()
 *     #array_end()
 *   #struct_end()
 */
public class ServerCoCoAttestationReportSerializer  extends ApiResponseSerializer<ServerCoCoAttestationReport> {
    @Override
    public Class<ServerCoCoAttestationReport> getSupportedClass() {
        return ServerCoCoAttestationReport.class;
    }

    @Override
    public SerializedApiResponse serialize(ServerCoCoAttestationReport src) {
        SerializationBuilder builder = new SerializationBuilder()
                .add("report_id", src.getId())
                .add("report_status", src.getStatus().name())
                .add("report_created", src.getCreated())
                .add("report_modified", src.getModified());
        List<Map<String, Object>> results = new ArrayList<>();
        src.getResults().forEach(res -> {
            Map<String, Object> r = new HashMap<>();
            r.put("result_id", res.getId());
            r.put("result_type", res.getResultType().name());
            r.put("result_status", res.getStatus().name());
            r.put("result_description", res.getDescription());
            if (res.getAttested() != null) {
                r.put("result_attested", res.getAttested());
            }
            results.add(r);
        });
        builder.add("results", results);
        return builder.build();
    }
}
