/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.xmlrpc.serializer;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;
import com.suse.manager.model.hub.migration.MigrationResult;

import java.util.List;
import java.util.Map;

/**
 * Convert a migration result into a XMLRPC &lt;struct&gt;.
 *
 * @apidoc.doc
 *   #struct_begin("result")
 *     #prop_array_begin("messages")
 *       #struct_begin("message")
 *         #prop_desc("string", "severity", "the severity of the message")
 *         #prop_desc("string", "message", "the message")
 *       #struct_end()
 *     #prop_array_end()
 *     #prop("result_code", "success")
 *   #struct_end()
 */
public class MigrationResultSerializer extends ApiResponseSerializer<MigrationResult> {
    @Override
    public Class<MigrationResult> getSupportedClass() {
        return MigrationResult.class;
    }

    @Override
    public SerializedApiResponse serialize(MigrationResult src) {
        List<Map<String, String>> messagesList = src.getMessages()
            .stream()
            .map(m -> Map.of("severity", m.severity().getLabel(), "message", m.message()))
            .toList();

        SerializationBuilder builder = new SerializationBuilder()
            .add("result_code", src.getResultCode())
            .add("messages", messagesList);

        return builder.build();
    }
}
