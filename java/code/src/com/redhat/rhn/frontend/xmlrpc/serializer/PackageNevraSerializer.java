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

import com.redhat.rhn.domain.rhnpackage.PackageNevra;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;


/**
 * ServerSerializer: Converts a Server object for representation as an XMLRPC struct.
 * Includes full server details, which may be more data than some calls would like.
 * @version $Rev$
 *
 *
 * @xmlrpc.doc
 *  #struct("package nvera")
 *      #prop("string", "name")
 *      #prop("string", "epoch")
 *      #prop("string", "version")
 *      #prop("string", "release")
 *      #prop("string", "arch")
 *  #struct_end()
 */
public class PackageNevraSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    public Class getSupportedClass() {
        return PackageNevra.class;
    }

    /**
     * {@inheritDoc}
     */
    protected void doSerialize(Object value, Writer output, XmlRpcSerializer serializer)
        throws XmlRpcException, IOException {

        PackageNevra pack = (PackageNevra)value;
        SerializerHelper helper = new SerializerHelper(serializer);
        helper.add("name", pack.getName().getName());
        helper.add("epoch", pack.getEvr().getEpoch());
        helper.add("version", pack.getEvr().getVersion());
        helper.add("release", pack.getEvr().getRelease());
        helper.add("arch", pack.getArch().getLabel());
        helper.writeTo(output);
    }


}
