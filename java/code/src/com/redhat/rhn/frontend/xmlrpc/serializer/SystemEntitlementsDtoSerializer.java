/**
 * Copyright (c) 2013 Red Hat, Inc.
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

import com.redhat.rhn.frontend.dto.SystemEntitlementsDto;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

import java.io.IOException;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcCustomSerializer;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 * SystemEntitlementsDtoSerializer
 * @version $Rev$
 *
 * @xmlrpc.doc
 * #struct("entitlement usage")
 *   #prop("string", "label")
 *   #prop("string", "name")
 *   #prop("int", "allocated")
 *   #prop("int", "unallocated")
 *   #prop("int", "used")
 *   #prop("int", "free")
 * #struct_end()
 */
public class SystemEntitlementsDtoSerializer implements XmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    public Class getSupportedClass() {
        return SystemEntitlementsDto.class;
    }

    /**
     * {@inheritDoc}
     */
    public void serialize(Object value, Writer output, XmlRpcSerializer builtInSerializer)
        throws XmlRpcException, IOException {
        SerializerHelper helper = new SerializerHelper(builtInSerializer);
        SystemEntitlementsDto dto = (SystemEntitlementsDto) value;

        helper.add("label", dto.getLabel());
        helper.add("name", dto.getName());
        helper.add("allocated", dto.getAllocated());
        helper.add("unallocated", dto.getAvailable());
        helper.add("used", dto.getUsed());
        helper.add("free", dto.getFree());
        helper.writeTo(output);
    }
}
