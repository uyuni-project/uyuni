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

import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFilter;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * Serializer for {@link ContentProjectFilter}
 * Also serializes the information about the associated {@link ContentFilter}
 *
 * @apidoc.doc
 * #struct_begin("assigned content filter information")
 *   #prop("string", "state")
 *   $ContentFilterSerializer
 * #struct_end()
 */
public class ContentProjectFilterSerializer extends ApiResponseSerializer<ContentProjectFilter> {

    @Override
    public Class<ContentProjectFilter> getSupportedClass() {
        return ContentProjectFilter.class;
    }

    @Override
    public SerializedApiResponse serialize(ContentProjectFilter src) {
        return new SerializationBuilder()
                .add("state", src.getState())
                .add("filter", src.getFilter())
                .build();
    }
}
