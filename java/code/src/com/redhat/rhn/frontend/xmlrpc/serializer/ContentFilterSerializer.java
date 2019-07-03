/**
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
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

/**
 * Serializer for {@link ContentFilter}
 *
 * @xmlrpc.doc
 * #struct("Content Filter information")
 *   #prop("int", "id")
 *   #prop("string", "name")
 *   #prop("int", "orgId")
 *   #prop("entityType", "Entity type (e.g. 'package')")
 *   #prop("rule", "Rule (e.g. 'deny')")
 *   #struct("criteria")
 *       #prop_desc("string", "matcher", "The matcher type of the filter (e.g. 'contains')")
 *       #prop_desc("string", "field", "The entity field to match (e.g. 'name'")
 *       #prop_desc("string", "value", "The field value to match (e.g. 'kernel')")
 *   #struct_end()
 * #struct_end()
 */
public class ContentFilterSerializer extends RhnXmlRpcCustomSerializer {

    @Override
    public Class getSupportedClass() {
        return ContentFilter.class;
    }

    @Override
    protected void doSerialize(Object obj, Writer writer, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        ContentFilter filter = (ContentFilter) obj;
        SerializerHelper helper = new SerializerHelper(serializer);
        helper.add("id", filter.getId());
        helper.add("name", filter.getName());
        helper.add("orgId", filter.getOrg().getId());
        helper.add("entityType", filter.getEntityType().getLabel());
        helper.add("rule", filter.getRule().getLabel());
        helper.add("criteria", criteriaToMap(filter.getCriteria()));
        helper.writeTo(writer);
    }

    private HashMap<Object, Object> criteriaToMap(FilterCriteria criteria) {
        HashMap<Object, Object> map = new HashMap<>();
        map.put("matcher", criteria.getMatcher().getLabel());
        map.put("field", criteria.getField());
        map.put("value", criteria.getValue());
        return map;
    }
}
