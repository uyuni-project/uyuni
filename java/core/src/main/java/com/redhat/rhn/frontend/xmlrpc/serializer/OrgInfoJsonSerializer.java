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

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;
import com.suse.manager.model.hub.OrgInfoJson;

/**
 * Converts a OrgInfoJson to an XMLRPC &lt;struct&gt;.
 *
 * @apidoc.doc
 *   #struct_begin(" org info ")
 *     #prop_desc("long", "org_id", "org identifier")
 *     #prop_desc("string", "org_name", "org name")
 *   #struct_end()
 */
public class OrgInfoJsonSerializer extends ApiResponseSerializer<OrgInfoJson> {

    @Override
    public Class<OrgInfoJson> getSupportedClass() {
        return OrgInfoJson.class;
    }

    @Override
    public SerializedApiResponse serialize(OrgInfoJson src) {
        return new SerializationBuilder()
                .add("org_id", src.getOrgId())
                .add("org_name", src.getOrgName())
                .build();
    }
}
