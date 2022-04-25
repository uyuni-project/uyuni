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

import com.redhat.rhn.domain.recurringactions.RecurringAction;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

import java.io.IOException;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 * Serializer for {@link com.redhat.rhn.domain.recurringactions.RecurringAction} class and subclasses
 *
 * @xmlrpc.doc
 * #struct_begin("recurring action information")
 *   #prop("int", "id")
 *   #prop("string", "name")
 *   #prop("int", "entity_id")
 *   #prop("string", "entity_type")
 *   #prop("string", "cron_expr")
 *   #prop($date, "created")
 *   #prop("string", "creator")
 *   #prop("boolean", "test")
 *   #prop("boolean", "active")
 * #struct_end()
 */
public class RecurringActionSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class getSupportedClass() {
        return RecurringAction.class;
    }

    @Override
    protected void doSerialize(Object obj, Writer writer, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        RecurringAction action = (RecurringAction) obj;
        SerializerHelper helper = new SerializerHelper(serializer);

        helper.add("id", action.getId());
        helper.add("name", action.getName());
        helper.add("entity_id", action.getEntityId());
        helper.add("entity_type", action.getType().toString());
        helper.add("cron_expr", action.getCronExpr());
        helper.add("created", action.getCreated());
        helper.add("creator", action.getCreator().getLogin());
        helper.add("test", action.isTestMode());
        helper.add("active", action.isActive());

        helper.writeTo(writer);
    }
}
