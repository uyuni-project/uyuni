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

import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

/**
 *
 * CpuSerializer
 * @version $Rev$
 * @xmlrpc.doc
 *   #struct("CPU")
 *      #prop("string", "cache")
 *      #prop("string", "family")
 *      #prop("string", "mhz")
 *      #prop("string", "flags")
 *      #prop("string", "model")
 *      #prop("string", "vendor")
 *      #prop("string", "arch")
 *      #prop("string", "stepping")
 *      #prop("string", "count")
 *      #prop("int", "socket_count (if available)")
 *  #struct_end()
 *
 */
public class CpuSerializer extends RhnXmlRpcCustomSerializer {

    /**
     *
     * {@inheritDoc}
     */
    public Class getSupportedClass() {
        return CPU.class;
    }
    /**
     *
     * {@inheritDoc}
     */
    protected void doSerialize(Object value, Writer output, XmlRpcSerializer serializer)
        throws XmlRpcException, IOException {
        SerializerHelper helper = new SerializerHelper(serializer);
        CPU cpu = (CPU) value;
        helper.add("cache", cpu.getCache());
        helper.add("family", cpu.getFamily());
        helper.add("mhz", cpu.getMHz());
        helper.add("flags", cpu.getFlags());
        helper.add("model", cpu.getModel());
        helper.add("vendor", cpu.getVendor());
        helper.add("arch", cpu.getArchName());
        helper.add("stepping", cpu.getStepping());
        helper.add("count", cpu.getNrCPU());
        helper.add("socket_count", cpu.getNrsocket());
        helper.writeTo(output);
    }

}
