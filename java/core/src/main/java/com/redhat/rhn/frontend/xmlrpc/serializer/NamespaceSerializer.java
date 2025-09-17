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


import com.redhat.rhn.domain.access.Namespace;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 *
 * A serializer for RBAC namespaces
 *
 * @apidoc.doc
 *      #struct_begin("namespace")
 *              #prop("string", "namespace")
 *              #prop("string", "access_mode")
 *              #prop("string", "description")
 *      #struct_end()
 */
public class NamespaceSerializer extends ApiResponseSerializer<Namespace> {

    @Override
    public Class<Namespace> getSupportedClass() {
        return Namespace.class;
    }

    @Override
    public SerializedApiResponse serialize(Namespace src) {
        return new SerializationBuilder()
                .add("namespace", src.getNamespace())
                .add("access_mode", src.getAccessMode())
                .add("description", src.getDescription())
                .build();
    }
}
