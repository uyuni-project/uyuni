/*
 * Copyright (c) 2014--2015 SUSE LLC
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

import com.redhat.rhn.manager.setup.MirrorCredentialsDto;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * Serializer for {@link MirrorCredentialsDto} objects.
 *
 * @xmlrpc.doc
 *   #struct_begin("credentials")
 *     #prop_desc("int", "id", "ID of the credentials")
 *     #prop_desc("string", "user", "username")
 *     #prop_desc("boolean", "isPrimary", "primary")
 *   #struct_end()
 */
public class MirrorCredentialsDtoSerializer extends ApiResponseSerializer<MirrorCredentialsDto> {

    @Override
    public Class<MirrorCredentialsDto> getSupportedClass() {
        return MirrorCredentialsDto.class;
    }

    @Override
    public SerializedApiResponse serialize(MirrorCredentialsDto src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("user", src.getUser())
                .add("isPrimary", src.isPrimary())
                .build();
    }
}
