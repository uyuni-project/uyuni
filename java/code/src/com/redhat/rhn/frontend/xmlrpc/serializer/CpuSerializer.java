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

import com.redhat.rhn.domain.server.CPU;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 *
 * CpuSerializer
 * @xmlrpc.doc
 *   #struct_begin("CPU")
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
public class CpuSerializer extends ApiResponseSerializer<CPU> {

    @Override
    public Class<CPU> getSupportedClass() {
        return CPU.class;
    }

    @Override
    public SerializedApiResponse serialize(CPU src) {
        return new SerializationBuilder()
                .add("cache", src.getCache())
                .add("family", src.getFamily())
                .add("mhz", src.getMHz())
                .add("flags", src.getFlags())
                .add("model", src.getModel())
                .add("vendor", src.getVendor())
                .add("arch", src.getArchName())
                .add("stepping", src.getStepping())
                .add("count", src.getNrCPU())
                .add("socket_count", src.getNrsocket())
                .build();
    }
}
