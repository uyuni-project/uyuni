/*
 * Copyright (c) 2021 SUSE LLC
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


import com.redhat.rhn.domain.cloudpayg.PaygSshData;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 *
 * PaygSshDataSerializer a serializer for the PaygSshData class
 *
 * @apidoc.doc
 *      #struct_begin("SSH data")
 *              #prop("string", "description")
 *              #prop("string", "hostname")
 *              #prop("int", "port")
 *              #prop("string", "username")
 *              #prop("string", "bastion_hostname")
 *              #prop("int", "bastion_port")
 *              #prop("string", "bastion_username")
 *      #struct_end()
 */
public class PaygSshDataSerializer extends ApiResponseSerializer<PaygSshData> {

    @Override
    public Class<PaygSshData> getSupportedClass() {
        return PaygSshData.class;
    }

    @Override
    public SerializedApiResponse serialize(PaygSshData src) {
        return new SerializationBuilder()
                .add("description", src.getDescription())
                .add("hostname", src.getHost())
                .add("port", src.getPort())
                .add("username", src.getUsername())
                .add("bastion_hostname", src.getBastionHost())
                .add("bastion_port", src.getBastionPort())
                .add("bastion_username", src.getBastionUsername())
                .build();
    }
}
