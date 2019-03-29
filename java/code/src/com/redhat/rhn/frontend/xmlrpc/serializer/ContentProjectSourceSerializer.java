/**
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
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

import java.io.IOException;
import java.io.Writer;

/**
 * Serializer for {@link com.redhat.rhn.domain.contentmgmt.ProjectSource} class and subclasses
 *
 * @xmlrpc.doc
 * #struct("Content Project Source information")
 *   #prop("string", "contentProjectLabel")
 *   #prop("string", "type")
 *   #prop("string", "state")
     #prop_desc("string", "channelLabel", "(If type is SW_CHANNEL) The label of channel associated with the source")
 * #struct_end()
 */
public class ContentProjectSourceSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class getSupportedClass() {
        return ProjectSource.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSerialize(Object obj, Writer writer, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        ProjectSource source = (ProjectSource) obj;

        SerializerHelper helper = new SerializerHelper(serializer);
        helper.add("contentProjectLabel", source.getContentProject().getLabel());
        helper.add("type", ProjectSource.Type.lookupBySourceClass(source.getClass()).getLabel());
        helper.add("state", source.getState());
        source.asSoftwareSource().ifPresent(s -> {
            helper.add("channelLabel", s.getChannel().getLabel());
        });
        helper.writeTo(writer);
    }
}
