/*
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

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * Serializer for {@link com.redhat.rhn.domain.recurringactions.RecurringAction} class and subclasses
 *
 * @xmlrpc.doc
 * #struct_begin("Recurring Action information")
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
public class RecurringActionSerializer extends ApiResponseSerializer<RecurringAction> {

    @Override
    public Class<RecurringAction> getSupportedClass() {
        return RecurringAction.class;
    }

    @Override
    public SerializedApiResponse serialize(RecurringAction src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("name", src.getName())
                .add("entity_id", src.getEntityId())
                .add("entity_type", src.getType().toString())
                .add("cron_expr", src.getCronExpr())
                .add("created", src.getCreated())
                .add("creator", src.getCreator().getLogin())
                .add("test", src.isTestMode())
                .add("active", src.isActive())
                .build();
    }
}
