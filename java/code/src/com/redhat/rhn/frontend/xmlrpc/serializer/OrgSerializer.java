/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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

import com.redhat.rhn.domain.org.Org;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;


/**
 * OrgSerializer is a custom serializer for the XMLRPC library.
 * It converts an Org to an XMLRPC &lt;struct&gt;.
 *
 * @apidoc.doc
 *  #struct_begin("script result")
 *      #prop_desc("long", "id", "ID of the Org")
 *      #prop_desc("string", "name", "name of the Org")
 *  #struct_end()
 */
public class OrgSerializer extends ApiResponseSerializer<Org> {

    @Override
    public Class<Org> getSupportedClass() {
        return Org.class;
    }

    @Override
    public SerializedApiResponse serialize(Org src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("name", src.getName())
                .build();
    }
}
