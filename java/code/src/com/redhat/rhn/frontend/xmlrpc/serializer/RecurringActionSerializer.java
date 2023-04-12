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
import com.redhat.rhn.domain.recurringactions.state.RecurringStateConfig;
import com.redhat.rhn.domain.recurringactions.type.RecurringActionType;
import com.redhat.rhn.domain.recurringactions.type.RecurringHighstate;
import com.redhat.rhn.domain.recurringactions.type.RecurringState;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Serializer for {@link com.redhat.rhn.domain.recurringactions.RecurringAction} class and subclasses
 *
 * @apidoc.doc
 * #struct_begin("recurring action information (some fields may be absent for some action types)")
 *   #prop("int", "id")
 *   #prop("string", "name")
 *   #prop("int", "entity_id")
 *   #prop("string", "entity_type")
 *   #prop("string", "cron_expr")
 *   #prop($date, "created")
 *   #prop("string", "creator")
 *   #prop("boolean", "test")
 *   #prop_array("states", "string", "the ordered list of states to be executed by a custom state action")
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
        SerializationBuilder builder = new SerializationBuilder();
        builder.add("id", src.getId())
                .add("type", src.getActionType().toString())
                .add("name", src.getName())
                .add("entity_id", src.getEntityId())
                .add("entity_type", src.getTargetType().toString())
                .add("cron_expr", src.getCronExpr())
                .add("created", src.getCreated())
                .add("creator", src.getCreator().getLogin())
                .add("active", src.isActive());

        if (src.getActionType().equals(RecurringActionType.ActionType.CUSTOMSTATE)) {
            RecurringState stateAction = (RecurringState) src.getRecurringActionType();
            builder.add("test", ((RecurringState) src.getRecurringActionType()).isTestMode());
            builder.add("states", stateAction.getStateConfig().stream()
                    .sorted(Comparator.comparing(RecurringStateConfig::getPosition))
                    .map(RecurringStateConfig::getStateName)
                    .collect(Collectors.toList()));
        }
        else if (src.getActionType().equals(RecurringActionType.ActionType.HIGHSTATE)) {
            builder.add("test", ((RecurringHighstate) src.getRecurringActionType()).isTestMode());
        }

        return builder.build();
    }
}
