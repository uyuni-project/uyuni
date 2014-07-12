/**
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

import java.io.IOException;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

import com.redhat.rhn.domain.kickstart.crypto.CryptoKeyType;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

/**
 * CryptoKeyTypeSerializer: Converts a CryptoKeyType object for representation
 * as an XMLRPC struct.
 * @version $Rev$
 *
 */
public class CryptoKeyTypeSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    public Class getSupportedClass() {
        return CryptoKeyType.class;
    }

    /**
     * {@inheritDoc}
     */
    protected void doSerialize(Object value, Writer output, XmlRpcSerializer serializer)
        throws XmlRpcException, IOException {
        CryptoKeyType ckt = (CryptoKeyType)value;

        SerializerHelper helper = new SerializerHelper(serializer);

        helper.add("id", ckt.getId());
        helper.add("label", ckt.getLabel());
        helper.add("description", ckt.getDescription());
        helper.add("created", ckt.getCreated());
        helper.add("modified", ckt.getModified());

        helper.writeTo(output);
    }
}
