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
package com.redhat.rhn.frontend.xmlrpc.serializer;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;
import com.suse.manager.model.attestation.ServerCoCoAttestationConfig;

/**
 * CoCoAttestationConfigSerializer will serialize an {@link ServerCoCoAttestationConfig} object to XMLRPC
 * syntax.
 *
 * @apidoc.doc
 *
 * #struct_begin("coco_attestation_config")
 *   #prop_desc("boolean", "enabled", "true if Confidential Compute Attestation is enabled for this system")
 *   #prop_desc("string", "environment_type", "the configured environment type")
 *   #prop_desc("int", "system_id", "the ID of the system")
 * #struct_end()
 */
public class CoCoAttestationConfigSerializer extends ApiResponseSerializer<ServerCoCoAttestationConfig> {

    @Override
    public Class<ServerCoCoAttestationConfig> getSupportedClass() {
        return ServerCoCoAttestationConfig.class;
    }

    @Override
    public SerializedApiResponse serialize(ServerCoCoAttestationConfig src) {
        return new SerializationBuilder()
                .add("enabled", src.isEnabled())
                .add("environment_type", src.getEnvironmentType().name())
                .add("system_id", src.getServer().getId())
                .add("attest_on_boot", src.isAttestOnBoot())
                .build();
    }
}
