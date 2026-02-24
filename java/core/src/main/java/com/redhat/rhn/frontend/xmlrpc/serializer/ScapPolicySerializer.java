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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.xmlrpc.serializer;

import com.redhat.rhn.domain.audit.ScapPolicy;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * ScapPolicySerializer
 *
 * @apidoc.doc
 * #struct_begin("SCAP policy information")
 *   #prop_desc("int", "id", "Policy ID")
 *   #prop_desc("string", "policyName", "Policy name")
 *   #prop_desc("string", "description", "Policy description")
 *   #prop_desc("int", "scapContentId", "SCAP content ID")
 *   #prop_desc("string", "xccdfProfileId", "XCCDF profile ID")
 *   #prop_desc("int", "tailoringFileId", "Tailoring file ID (optional)")
 *   #prop_desc("string", "tailoringProfileId", "Tailoring profile ID (optional)")
 *   #prop_desc("string", "ovalFiles", "OVAL files")
 *   #prop_desc("string", "advancedArgs", "Advanced arguments")
 *   #prop_desc("boolean", "fetchRemoteResources", "Fetch remote resources flag")
 * #struct_end()
 */
public class ScapPolicySerializer extends ApiResponseSerializer<ScapPolicy> {

    @Override
    public Class<ScapPolicy> getSupportedClass() {
        return ScapPolicy.class;
    }

    @Override
    public SerializedApiResponse serialize(ScapPolicy src) {
        SerializationBuilder builder = new SerializationBuilder()
                .add("id", src.getId())
                .add("policyName", src.getPolicyName())
                .add("description", src.getDescription())
                .add("scapContentId", src.getScapContent() != null ? src.getScapContent().getId() : null)
                .add("xccdfProfileId", src.getXccdfProfileId())
                .add("tailoringProfileId", src.getTailoringProfileId())
                .add("ovalFiles", src.getOvalFiles())
                .add("advancedArgs", src.getAdvancedArgs())
                .add("fetchRemoteResources", src.isFetchRemoteResources());

        // Add tailoring file ID only if present
        if (src.getTailoringFile() != null) {
            builder.add("tailoringFileId", src.getTailoringFile().getId());
        }

        return builder.build();
    }
}
