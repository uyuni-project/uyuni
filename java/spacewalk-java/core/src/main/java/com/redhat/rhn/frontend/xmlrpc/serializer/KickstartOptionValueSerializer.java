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

import com.redhat.rhn.frontend.dto.kickstart.KickstartOptionValue;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * Serializer for {@link KickstartOptionValue} objects.
 *
 *
 * @apidoc.doc
 *      #struct_begin("value")
 *          #prop("string", "name")
 *          #prop("string", "value")
 *          #prop("boolean", "enabled")
 *      #struct_end()

 */
public class KickstartOptionValueSerializer extends ApiResponseSerializer<KickstartOptionValue> {

    @Override
    public Class<KickstartOptionValue> getSupportedClass() {
        return KickstartOptionValue.class;
    }

    @Override
    public SerializedApiResponse serialize(KickstartOptionValue src) {
        SerializationBuilder builder = new SerializationBuilder()
                .add("name", src.getName())
                .add("value", src.getArg());

        // Null check so if enabled is effectively false, we send that and don't squash it
        Boolean enabled = src.getEnabled();
        if (enabled == null) {
            enabled = Boolean.FALSE;
        }
        builder.add("enabled", enabled);
        return builder.build();
    }
}
