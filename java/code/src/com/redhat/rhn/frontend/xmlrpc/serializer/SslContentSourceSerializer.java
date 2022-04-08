/*
 * Copyright (c) 2017 Red Hat, Inc.
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

import com.redhat.rhn.domain.channel.SslContentSource;
import com.redhat.rhn.domain.kickstart.crypto.SslCryptoKey;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 *
 * SslContentSourceSerializer
 *
 * @xmlrpc.doc
 *  #struct_begin("contentsourcessl")
 *      #prop("string", "sslCaDesc")
 *      #prop("string", "sslCertDesc")
 *      #prop("string", "sslKeyDesc")
 *  #struct_end()
 *
 */
public class SslContentSourceSerializer extends ApiResponseSerializer<SslContentSource> {

    @Override
    public Class<SslContentSource> getSupportedClass() {
        return SslContentSource.class;
    }

    @Override
    public SerializedApiResponse serialize(SslContentSource src) {
        SslCryptoKey ca = src.getCaCert();
        SslCryptoKey cert = src.getClientCert();
        SslCryptoKey key = src.getClientKey();

        return new SerializationBuilder()
                .add("sslCaDesc", (ca != null) ? ca.getDescription() : "")
                .add("sslCertDesc", (cert != null) ? cert.getDescription() : "")
                .add("sslKeyDesc", (key != null) ? key.getDescription() : "")
                .build();
    }
}
