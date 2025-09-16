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

import com.redhat.rhn.frontend.dto.ProfileOverviewDto;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;


/**
 * ProfileOverviewDtoSerializer
 *
 * @apidoc.doc
 * #struct_begin("package profile")
 *   #prop("int", "id")
 *   #prop("string", "name")
 *   #prop("string", "channel")
 * #struct_end()
 */
public class ProfileOverviewDtoSerializer extends ApiResponseSerializer<ProfileOverviewDto> {

    @Override
    public Class<ProfileOverviewDto> getSupportedClass() {
        return ProfileOverviewDto.class;
    }

    @Override
    public SerializedApiResponse serialize(ProfileOverviewDto src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("name", src.getName())
                .add("channel", src.getChannelName())
                .build();
    }
}
