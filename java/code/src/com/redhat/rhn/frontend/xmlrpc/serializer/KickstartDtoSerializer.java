/**
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

import java.io.IOException;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

import com.redhat.rhn.frontend.dto.kickstart.KickstartDto;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;


/**
 * ActivationKeySerializer
 * @version $Rev$
 *
 * @xmlrpc.doc
 *   #struct("kickstart")
 *          #prop("string", "label")
 *          #prop("string", "tree_label")
 *          #prop("string", "name")
 *          #prop("boolean", "advanced_mode")
 *          #prop("boolean", "org_default")
 *          #prop("boolean", "active")
 *          #prop("string", "update_type")
 *   #struct_end()
 */
public class KickstartDtoSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    public Class getSupportedClass() {
        return KickstartDto.class;
    }

    /** {@inheritDoc} */
    protected void doSerialize(Object value, Writer output, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        KickstartDto ks = (KickstartDto)value;
        SerializerHelper helper = new SerializerHelper(serializer);

        helper.add("label", ks.getLabel());
        helper.add("active", ks.isActive());
        helper.add("tree_label", ks.getTreeLabel());
        helper.add("name", ks.getLabel());
        helper.add("advanced_mode", ks.isAdvancedMode());
        if (ks.isOrgDefault()) {
            helper.add("org_default", true);
        }
        else {
            helper.add("org_default", false);
        }
        helper.add("update_type", ks.getUpdateType());


        helper.writeTo(output);
    }

}
