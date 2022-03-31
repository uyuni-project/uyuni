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

import com.redhat.rhn.domain.kickstart.crypto.CryptoKey;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * Serializes instances of
 * {@link com.redhat.rhn.domain.kickstart.crypto.CryptoKey}.
 *
 *
 * @xmlrpc.doc
 *      #struct_begin("key")
 *          #prop("string", "description")
 *          #prop("string", "type")
 *          #prop("string", "content")
 *      #struct_end()
 */
public class CryptoKeySerializer extends ApiResponseSerializer<CryptoKey> {

    @Override
    public Class<CryptoKey> getSupportedClass() {
        return CryptoKey.class;
    }

    @Override
    public SerializedApiResponse serialize(CryptoKey src) {
        return new SerializationBuilder()
                .add("description", src.getDescription())
                .add("type", src.getCryptoKeyType().getLabel())
                .add("content", src.getKeyString())
                .build();
    }
}
