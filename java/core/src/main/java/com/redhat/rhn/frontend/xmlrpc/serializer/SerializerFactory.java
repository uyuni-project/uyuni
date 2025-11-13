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

import com.suse.manager.api.ApiResponseSerializer;

import java.util.ArrayList;
import java.util.List;

public class SerializerFactory {
    private final List<ApiResponseSerializer<?>> serializers;

    /**
     * Constructs a {@link SerializerFactory} with the default {@link SerializerRegistry}
     */
    public SerializerFactory() {
        serializers = new ArrayList<>();
        initialize();
    }

    /**
     *
     * @return a list of serializers.
     */
    public List<ApiResponseSerializer<?>> getSerializers() {
        return serializers;
    }

    private void initialize() {
        for (Class<? extends ApiResponseSerializer<?>> clazz : SerializerRegistry.getSerializationClasses()) {
            try {
                serializers.add(clazz.getDeclaredConstructor().newInstance());
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
