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

import com.redhat.rhn.domain.audit.TailoringFile;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * TailoringFileSerializer
 *
 * @apidoc.doc
 * #struct_begin("Tailoring file information")
 *   #prop_desc("int", "id", "Tailoring file ID")
 *   #prop_desc("string", "name", "Tailoring file name")
 *   #prop_desc("string", "fileName", "File name on disk")
 *   #prop_desc("string", "displayFileName", "Display file name")
 *   #prop_desc("int", "orgId", "Organization ID")
 * #struct_end()
 */
public class TailoringFileSerializer extends ApiResponseSerializer<TailoringFile> {

    @Override
    public Class<TailoringFile> getSupportedClass() {
        return TailoringFile.class;
    }

    @Override
    public SerializedApiResponse serialize(TailoringFile src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("name", src.getName())
                .add("fileName", src.getFileName())
                .add("displayFileName", src.getDisplayFileName())
                .add("orgId", src.getOrg().getId())
                .build();
    }
}
