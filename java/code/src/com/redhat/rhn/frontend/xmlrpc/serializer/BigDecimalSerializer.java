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

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;


/**
 * BigDecimalSerializer
 *

 * @apidoc.doc
 *      #param ("int", "decimal")
 */
public class BigDecimalSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class getSupportedClass() {
        return BigDecimal.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSerialize(Object value, Writer output,
                               XmlRpcSerializer serializer)
        throws XmlRpcException, IOException {
        BigDecimal bd = (BigDecimal) value;
        output.write("<i4>");
        output.write(Integer.toString(bd.intValue()));
        output.write("</i4>");
    }
}
