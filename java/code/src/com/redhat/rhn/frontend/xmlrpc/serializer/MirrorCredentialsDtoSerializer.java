/**
 * Copyright (c) 2014--2015 SUSE LLC
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
import com.redhat.rhn.manager.setup.MirrorCredentialsDto;

import java.io.IOException;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 * Serializer for {@link MirrorCredentialsDto} objects.
 *
 * @xmlrpc.doc
 *   #struct("credentials")
 *     #prop_desc("int", "id", "ID of the credentials")
 *     #prop_desc("string", "user", "username")
 *     #prop_desc("boolean", "isPrimary", "primary")
 *   #struct_end()
 */
public class MirrorCredentialsDtoSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<MirrorCredentialsDto> getSupportedClass() {
        return MirrorCredentialsDto.class;
    }

    @Override
    protected void doSerialize(Object obj, Writer writer, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        MirrorCredentialsDto credentials = (MirrorCredentialsDto) obj;
        SerializerHelper helper = new SerializerHelper(serializer);
        helper.add("id", credentials.getId());
        helper.add("user", credentials.getUser());
        helper.add("isPrimary", credentials.isPrimary());
        helper.writeTo(writer);
    }
}
