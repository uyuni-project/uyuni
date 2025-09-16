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

package com.redhat.rhn.frontend.xmlrpc.serializer;

import com.redhat.rhn.domain.contentmgmt.ContentEnvironmentDiff;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * Serializer for {@link ContentEnvironmentDiff}
 *
 * @apidoc.doc
 * #struct_begin("content environment difference information")
 *   #prop("int", "id")
 *   #prop("string", "type")
 *   #prop("string", "action")
 *   #prop("string", "name")
 *   #prop("string", "description")
 * #struct_end()
 */
public class ContentEnvironmentDifferenceSerializer extends ApiResponseSerializer<ContentEnvironmentDiff> {

    @Override
    public Class<ContentEnvironmentDiff> getSupportedClass() {
        return ContentEnvironmentDiff.class;
    }

    @Override
    public SerializedApiResponse serialize(ContentEnvironmentDiff src) {
        return new SerializationBuilder()
                .add("id", src.getEntryId())
                .add("type", src.getEntryType().getLabel())
                .add("action", src.getAction().getLabel())
                .add("name", src.getEntryName())
                .add("description", src.getEntryDescription())
                .build();
    }
}
