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

import com.redhat.rhn.domain.kickstart.KickstartIpRange;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;


/**
 * KickstartIpRangeSerializer
 *
 * @apidoc.doc
 *   #struct_begin("kickstart IP range")
 *     #prop_desc("string", "ksLabel", "the kickstart label associated with the IP range")
 *     #prop_desc("string", "max", "the max IP of the range")
 *     #prop_desc("string", "min", "the min IP of the range")
 *   #struct_end()
 */
public class KickstartIpRangeSerializer extends ApiResponseSerializer<KickstartIpRange> {

    @Override
    public Class<KickstartIpRange> getSupportedClass() {
        return KickstartIpRange.class;
    }

    @Override
    public SerializedApiResponse serialize(KickstartIpRange src) {
        return new SerializationBuilder()
                .add("ksLabel", src.getKsdata().getLabel())
                .add("min", src.getMinString())
                .add("max", src.getMaxString())
                .build();
    }
}
