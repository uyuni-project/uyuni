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

import com.redhat.rhn.frontend.dto.ProfileOverviewDto;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;


/**
 * ProfileOverviewDtoSerializer
 * @version $Rev$
 *
 * @xmlrpc.doc
 * #struct("package profile")
 *   #prop("int", "id")
 *   #prop("string", "name")
 *   #prop("string", "channel")
 * #struct_end()
 */
public class ProfileOverviewDtoSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    public Class getSupportedClass() {
        return ProfileOverviewDto.class;
    }

    /**
     * {@inheritDoc}
     */
    protected void doSerialize(Object value, Writer output, XmlRpcSerializer serializer)
        throws XmlRpcException, IOException {
        ProfileOverviewDto dto = (ProfileOverviewDto) value;
        SerializerHelper helper = new SerializerHelper(serializer);
        helper.add("id", dto.getId().longValue());
        helper.add("name", dto.getName());
        helper.add("channel", dto.getChannelName());
        helper.writeTo(output);
    }
}
