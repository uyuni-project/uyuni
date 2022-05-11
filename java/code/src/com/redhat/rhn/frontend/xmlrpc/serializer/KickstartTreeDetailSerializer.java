/*
 * Copyright (c) 2010--2013 Red Hat, Inc.
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

import com.redhat.rhn.frontend.dto.kickstart.KickstartableTreeDetail;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;


/**
 * KickstartTreeDetailSerializer
 *
 * @apidoc.doc
 *
 * #struct_begin("kickstartable tree")
 *   #prop("int", "id")
 *   #prop("string", "label")
 *   #prop("string", "abs_path")
 *   #prop("int", "channel_id")
 *   $KickstartInstallTypeSerializer
 * #struct_end()
 */
public class KickstartTreeDetailSerializer extends ApiResponseSerializer<KickstartableTreeDetail> {

    @Override
    public Class<KickstartableTreeDetail> getSupportedClass() {
        return KickstartableTreeDetail.class;
    }

    @Override
    public SerializedApiResponse serialize(KickstartableTreeDetail src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("label", src.getLabel())
                .add("abs_path", src.getAbsolutePath())
                .add("channel_id", src.getChannel().getId())
                .add("install_type", src.getInstallType())
                .build();
    }
}
