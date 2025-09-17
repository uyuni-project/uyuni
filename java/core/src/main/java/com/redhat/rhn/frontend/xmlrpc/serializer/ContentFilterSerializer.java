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
import com.redhat.rhn.domain.contentmgmt.FilterCriteria;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import java.util.HashMap;

/**
 * Serializer for {@link ContentFilter}
 *
 * @apidoc.doc
 * #struct_begin("content filter information")
 *   #prop("int", "id")
 *   #prop("string", "name")
 *   #prop("int", "orgId")
 *   #prop("entityType", "entity type (e.g. 'package')")
 *   #prop("rule", "rule (e.g. 'deny')")
 *   #struct_begin("criteria")
 *       #prop_desc("string", "matcher", "the matcher type of the filter (e.g. 'contains')")
 *       #prop_desc("string", "field", "the entity field to match (e.g. 'name'")
 *       #prop_desc("string", "value", "the field value to match (e.g. 'kernel')")
 *   #struct_end()
 * #struct_end()
 */
public class ContentFilterSerializer extends ApiResponseSerializer<ContentFilter> {

    @Override
    public Class<ContentFilter> getSupportedClass() {
        return ContentFilter.class;
    }

    @Override
    public SerializedApiResponse serialize(ContentFilter src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("name", src.getName())
                .add("orgId", src.getOrg().getId())
                .add("entityType", src.getEntityType().getLabel())
                .add("rule", src.getRule().getLabel())
                .add("criteria", criteriaToMap(src.getCriteria()))
                .build();
    }

    private HashMap<Object, Object> criteriaToMap(FilterCriteria criteria) {
        HashMap<Object, Object> map = new HashMap<>();
        map.put("matcher", criteria.getMatcher().getLabel());
        map.put("field", criteria.getField());
        map.put("value", criteria.getValue());
        return map;
    }
}
