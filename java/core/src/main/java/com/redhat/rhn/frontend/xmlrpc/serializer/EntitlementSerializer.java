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

import com.redhat.rhn.domain.entitlement.Entitlement;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;


/**
 * EntitlementSerializer
 *
 * @apidoc.doc
 * #struct_begin("entitlement info")
 *   #prop("string", "label")
 *   #prop("string", "name")
 *   #prop("string", "type")
 * #struct_end()
 */
public class EntitlementSerializer extends ApiResponseSerializer<Entitlement> {

    @Override
    public Class<Entitlement> getSupportedClass() {
        return Entitlement.class;
    }

    @Override
    public SerializedApiResponse serialize(Entitlement src) {
        return new SerializationBuilder()
                .add("label", src.getLabel())
                .add("name", src.getHumanReadableLabel())
                .add("type", src.getHumanReadableTypeLabel())
                .build();
    }
}
