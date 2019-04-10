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

import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

import java.io.IOException;
import java.io.Writer;

/**
 * Serializer for {@link com.redhat.rhn.domain.contentmgmt.ContentEnvironment}
 *
 * @xmlrpc.doc
 * #struct("Content Environment information")
 *   #prop("int", "id")
 *   #prop("string", "label")
 *   #prop("string", "name")
 *   #prop("string", "description")
 *   #prop("int", "version")
 *   #prop("string", "status")
 *   #prop("string", "contentProjectLabel")
 *   #prop("string", "previousEnvironmentLabel")
 *   #prop("string", "nextEnvironmentLabel")
 * #struct_end()
 */
public class ContentEnvironmentSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class getSupportedClass() {
        return ContentEnvironment.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSerialize(Object obj, Writer writer, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        ContentEnvironment environment = (ContentEnvironment) obj;
        SerializerHelper helper = new SerializerHelper(serializer);
        helper.add("id", environment.getId());
        helper.add("label", environment.getLabel());
        helper.add("name", environment.getName());
        helper.add("description", environment.getDescription());
        helper.add("version", environment.getVersion());
        helper.add("status", environment.computeStatus().map(s -> s.getLabel()).orElse("unknown"));
        helper.add("contentProjectLabel", environment.getContentProject().getLabel());
        helper.add("previousEnvironmentLabel", environment.getPrevEnvironmentOpt()
                .map(e -> e.getLabel()).orElse(null));
        helper.add("nextEnvironmentLabel", environment.getNextEnvironmentOpt()
                .map(e -> e.getLabel()).orElse(null));
        helper.writeTo(writer);
    }
}
