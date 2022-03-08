/*
 * Copyright (c) 2022 SUSE LLC
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
package com.suse.manager.api;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;

import redstone.xmlrpc.XmlRpcCustomSerializer;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 * Base class for defining a serialization strategy for a custom object returned from an API handler method
 * TODO: Implement and test in XMLRPC
 * @param <T> the type of the serialized object
 */
public abstract class ApiResponseSerializer<T> implements XmlRpcCustomSerializer, JsonSerializer<T> {
    /**
     * Populates a {@link SerializedApiResponse} to be used for serializing an instance of {@link T}
     *
     * A {@link SerializedApiResponse} should contain the properties of an object that are intended to be serialized.
     * It can be created using a {@link SerializationBuilder}.
     *
     * @param src the object to be serialized
     * @return the serialization object
     */
    public abstract SerializedApiResponse serialize(T src);

    @Override
    public final void serialize(Object obj, Writer writer, XmlRpcSerializer serializer) throws XmlRpcException {
        HibernateFactory.doWithoutAutoFlushing(() -> {
            try {
                if (getSupportedClass().isInstance(obj)) {
                    doSerialize(obj, writer, serializer);
                }
            }
            catch (Exception e) {
                throw new XmlRpcException(
                        "ERROR IN SERIALIZER FOR " + getSupportedClass().getName(), e);
            }
        }, false);
    }

    @SuppressWarnings("unchecked")
    private void doSerialize(Object obj, Writer writer, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        SerializedApiResponse serialized = serialize((T) obj);
        SerializerHelper helper = new SerializerHelper(serializer);
        serialized.forEach(helper::add);
        helper.writeTo(writer);
    }

    @Override
    public final JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        SerializedApiResponse serialized = serialize(src);
        JsonObject json = new JsonObject();

        serialized.forEach((key, val) -> {
            if (val instanceof Number) {
                json.addProperty(key, (Number) val);
            }
            else if (val instanceof String) {
                json.addProperty(key, (String) val);
            }
            else if (val instanceof Boolean) {
                json.addProperty(key, (Boolean) val);
            }
            else if (val instanceof Character) {
                json.addProperty(key, (Character) val);
            }
            else {
                json.add(key, context.serialize(val));
            }
        });

        return json;
    }

    @Override
    public abstract Class<T> getSupportedClass();
}
