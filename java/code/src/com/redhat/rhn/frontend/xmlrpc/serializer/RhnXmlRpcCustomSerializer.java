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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import redstone.xmlrpc.XmlRpcCustomSerializer;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 * Parent class of all XmlRpcSerializers.  This exists because redstone.xmlrpc silently
 * eats exceptions thrown by the specific Serializer instances, and we'd like to log
 * that when it happens, so we can fix it.
 *
 * @author ggainey
 *
 */
public abstract class RhnXmlRpcCustomSerializer implements XmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    public void serialize(Object obj, Writer writer, XmlRpcSerializer serializer)
                    throws XmlRpcException, IOException {
        HibernateFactory.doWithoutAutoFlushing(() -> {
            try {
                doSerialize(obj, writer, serializer);
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new XmlRpcException(
                        "ERROR IN SERIALIZER FOR " + getSupportedClass().getName(), e);
            }
        });
    }

    /**
     * Turn {@link Map} into {@link XmlRpcSerializer}.
     * @param data Map with data.
     * @param serializer Current serializer.
     * @return {@link XmlRpcSerializer}
     */
    public SerializerHelper serializeMap(Map<String, Object> data,
                                         XmlRpcSerializer serializer) {
        SerializerHelper helper = new SerializerHelper(serializer);
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            helper.add(entry.getKey(), entry.getValue());
        }
        return helper;
    }

    /**
     * {@inheritDoc}
     */
    public abstract Class getSupportedClass();

    protected abstract void doSerialize(Object obj, Writer writer,
                    XmlRpcSerializer serializer) throws XmlRpcException, IOException;

}
