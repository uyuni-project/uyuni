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

import com.redhat.rhn.domain.kickstart.KickstartCommand;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * KickstartCommandSerializer: Converts a KickstartCommand object for representation
 * as an XMLRPC struct.
 *
 *
 * @xmlrpc.doc
 *      #struct_begin("option")
 *          #prop("int", "id")
 *          #prop("string", "arguments")
 *      #struct_end()
 */
public class KickstartCommandSerializer extends ApiResponseSerializer<KickstartCommand> {

    @Override
    public Class<KickstartCommand> getSupportedClass() {
        return KickstartCommand.class;
    }

    @Override
    public SerializedApiResponse serialize(KickstartCommand src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("arguments", src.getArguments())
                .build();
    }
}
