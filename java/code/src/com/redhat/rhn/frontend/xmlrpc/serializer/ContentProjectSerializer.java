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
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectHistoryEntry;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import java.util.Comparator;

/**
 * Serializer for {@link com.redhat.rhn.domain.contentmgmt.ContentProject}
 *
 * @xmlrpc.doc
 * #struct_begin("Content Project information")
 *   #prop("int", "id")
 *   #prop("string", "label")
 *   #prop("string", "name")
 *   #prop("string", "description")
 *   #prop("dateTime.iso8601", "lastBuildDate")
 *   #prop("int", "orgId")
 *   #prop("string", "firstEnvironment label")
 * #struct_end()
 */
public class ContentProjectSerializer extends ApiResponseSerializer<ContentProject> {

    @Override
    public Class<ContentProject> getSupportedClass() {
        return ContentProject.class;
    }

    @Override
    public SerializedApiResponse serialize(ContentProject src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("label", src.getLabel())
                .add("name", src.getName())
                .add("description", src.getDescription())
                .add("lastBuildDate", src.getHistoryEntries().stream()
                        .map(ContentProjectHistoryEntry::getCreated)
                        .max(Comparator.naturalOrder())
                        .orElse(null))
                .add("orgId", src.getOrg().getId())
                .add("firstEnvironment", src.getFirstEnvironmentOpt()
                        .map(ContentEnvironment::getLabel)
                        .orElse(null))
                .build();
    }
}
