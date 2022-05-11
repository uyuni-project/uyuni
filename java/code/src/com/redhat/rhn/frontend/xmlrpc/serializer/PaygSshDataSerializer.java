/**
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
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

import java.io.IOException;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 *
 * PaygSshDataSerializer a serializer for the PaygSshData class
 *
 * @xmlrpc.doc
 *      #struct_begin("SSH data")
 *              #prop("string", "description")
 *              #prop("string", "hostname")
 *              #prop("int", "port")
 *              #prop("string", "username")
 *              #prop("string", "password")
 *              #prop("string", "key")
 *              #prop("string", "key_password")
 *              #prop("string", "bastion_hostname")
 *              #prop("int", "bastion_port")
 *              #prop("string", "bastion_username")
 *              #prop("string", "bastion_password")
 *              #prop("string", "bastion_key")
 *              #prop("string", "bastion_key_password")
 *      #struct_end()
 */
public class PaygSshDataSerializer extends RhnXmlRpcCustomSerializer {
    /**
     * {@inheritDoc}
     */
    public Class getSupportedClass() {
        return PaygSshData.class;
    }

    /**
     * {@inheritDoc}
     */
    protected void doSerialize(Object value, Writer output, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        SerializerHelper helper = new SerializerHelper(serializer);

        PaygSshData sshData = (PaygSshData) value;
        helper.add("description", sshData.getDescription());

        helper.add("hostname", sshData.getHost());
        helper.add("port", sshData.getPort());
        helper.add("username", sshData.getUsername());
        helper.add("password", sshData.getPassword());
        helper.add("key", sshData.getKey());
        helper.add("key_password", sshData.getKeyPassword());

        helper.add("bastion_hostname", sshData.getBastionHost());
        helper.add("bastion_port", sshData.getBastionPort());
        helper.add("bastion_username", sshData.getBastionUsername());
        helper.add("bastion_password", sshData.getBastionPassword());
        helper.add("bastion_key", sshData.getBastionKey());
        helper.add("bastion_key_password", sshData.getBastionKeyPassword());
        helper.writeTo(output);
    }
}
