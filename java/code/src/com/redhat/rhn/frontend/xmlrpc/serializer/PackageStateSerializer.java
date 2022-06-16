/**
 * Copyright (c) 2020 SUSE LLC
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


import com.redhat.rhn.domain.state.PackageState;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

import java.io.IOException;
import java.io.Writer;

/**
 *
 * PackageStateSerializer
 *
 * @xmlrpc.doc
 *
 * #struct_begin("package state")
 *     #prop("int", "id")
 *     #prop("string", "name")
 *     #prop_desc("int",  "state_revision_id", "state revision ID")
 *     #prop_desc("string",  "package_state_type_id", "'INSTALLED' or 'REMOVED'")
 *     #prop_desc("string",  "version_constraint_id", "'LATEST' or 'ANY'")
 * #struct_end()
 */
public class PackageStateSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    public Class getSupportedClass() {
        return PackageState.class;
    }

    /**
     * {@inheritDoc}
     */
    protected void doSerialize(Object value, Writer output, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        PackageState packageState = (PackageState) value;
        SerializerHelper helper = new SerializerHelper(serializer);
        helper.add("id", packageState.getId());
        helper.add("name", packageState.getName().getName());
        helper.add("state_revision_id", packageState.getStateRevision().getId());
        helper.add("package_state_type_id", packageState.getPackageState().name());
        helper.add("version_constraint_id", packageState.getVersionConstraint().name());
        helper.writeTo(output);
    }
}
