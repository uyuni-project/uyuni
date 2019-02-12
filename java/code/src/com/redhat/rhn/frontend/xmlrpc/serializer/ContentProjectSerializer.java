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

import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

import java.io.IOException;
import java.io.Writer;

/**
 * Serializer for {@link com.redhat.rhn.domain.contentmgmt.ContentProject}
 *
 * @xmlrpc.doc
 * #struct("Content Project information")
 *   #prop("int", "id")
 *   #prop("string", "label")
 *   #prop("string", "name")
 *   #prop("string", "description")
 *   #prop("int", "orgId")
 *   #prop("string", "firstEnvironment label")
 * #struct_end()
 */
public class ContentProjectSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class getSupportedClass() {
        return ContentProject.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSerialize(Object obj, Writer writer, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        ContentProject contentProject = (ContentProject) obj;
        SerializerHelper helper = new SerializerHelper(serializer);
        helper.add("id", contentProject.getId());
        helper.add("label", contentProject.getLabel());
        helper.add("name", contentProject.getName());
        helper.add("description", contentProject.getDescription());
        helper.add("orgId", contentProject.getOrg().getId());
        helper.add("firstEnvironment", contentProject.getFirstEnvironmentOpt().map(e -> e.getLabel()).orElse(null));
        helper.writeTo(writer);
    }
}
