/*
 * Copyright (c) 2019 SUSE LLC
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

import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.EnvironmentTarget;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * Serializer for {@link com.redhat.rhn.domain.contentmgmt.ContentEnvironment}
 *
 * @apidoc.doc
 * #struct_begin("content environment information")
 *   #prop("int", "id")
 *   #prop("string", "label")
 *   #prop("string", "name")
 *   #prop("string", "description")
 *   #prop("int", "version")
 *   #prop("string", "status")
 *   #prop("string", "contentProjectLabel")
 *   #prop("string", "previousEnvironmentLabel")
 *   #prop("string", "nextEnvironmentLabel")
 * #struct_end()
 */
public class ContentEnvironmentSerializer extends ApiResponseSerializer<ContentEnvironment> {

    @Override
    public Class<ContentEnvironment> getSupportedClass() {
        return ContentEnvironment.class;
    }

    @Override
    public SerializedApiResponse serialize(ContentEnvironment src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("label", src.getLabel())
                .add("name", src.getName())
                .add("description", src.getDescription())
                .add("version", src.getVersion())
                .add("status", src.computeStatus().map(EnvironmentTarget.Status::getLabel).orElse("unknown"))
                .add("contentProjectLabel", src.getContentProject().getLabel())
                .add("previousEnvironmentLabel", src.getPrevEnvironmentOpt()
                        .map(ContentEnvironment::getLabel).orElse(null))
                .add("nextEnvironmentLabel", src.getNextEnvironmentOpt()
                        .map(ContentEnvironment::getLabel).orElse(null))
                .build();
    }
}
