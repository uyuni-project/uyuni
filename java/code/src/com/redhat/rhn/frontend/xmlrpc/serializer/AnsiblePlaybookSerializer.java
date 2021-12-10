/*
 * Copyright (c) 2021 SUSE LLC
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

import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

import com.suse.manager.webui.utils.salt.custom.AnsiblePlaybookSlsResult;

import java.io.IOException;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 * XMLRPC Serializer for {@link AnsiblePlaybookSlsResult}
 *
 * @xmlrpc.doc
 *   #struct_begin("ansible playbook")
 *     #prop("string", "fullpath")
 *     #prop("string", "custom_inventory")
 *   #struct_end()
 */
public class AnsiblePlaybookSerializer extends RhnXmlRpcCustomSerializer {

    @Override
    public Class getSupportedClass() {
        return AnsiblePlaybookSlsResult.class;
    }

    @Override
    protected void doSerialize(Object obj, Writer output, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        AnsiblePlaybookSlsResult playbook = (AnsiblePlaybookSlsResult) obj;
        SerializerHelper helper = new SerializerHelper(serializer);

        helper.add("fullpath", playbook.getFullPath());
        helper.add("custom_inventory", playbook.getCustomInventory());

        helper.writeTo(output);
    }
}
