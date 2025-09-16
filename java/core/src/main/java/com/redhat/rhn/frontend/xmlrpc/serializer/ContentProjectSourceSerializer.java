/*
 * Copyright (c) 2019 SUSE LLC
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

import com.redhat.rhn.domain.contentmgmt.ProjectSource;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * Serializer for {@link com.redhat.rhn.domain.contentmgmt.ProjectSource} class and subclasses
 *
 * @apidoc.doc
 * #struct_begin("content project source information")
 *   #prop("string", "contentProjectLabel")
 *   #prop("string", "type")
 *   #prop("string", "state")
     #prop_desc("string", "channelLabel", "(if type is SW_CHANNEL) the label of channel associated with the source")
 * #struct_end()
 */
public class ContentProjectSourceSerializer extends ApiResponseSerializer<ProjectSource> {

    @Override
    public Class<ProjectSource> getSupportedClass() {
        return ProjectSource.class;
    }

    @Override
    public SerializedApiResponse serialize(ProjectSource src) {
        SerializationBuilder builder = new SerializationBuilder()
                .add("contentProjectLabel", src.getContentProject().getLabel())
                .add("type", ProjectSource.Type.lookupBySourceClass(src.getClass()).getLabel())
                .add("state", src.getState().toString());

        src.asSoftwareSource().ifPresent(s -> builder.add("channelLabel", s.getChannel().getLabel()));
        return builder.build();
    }
}
