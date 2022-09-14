/*
 * Copyright (c) 2022 SUSE LLC
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

import com.redhat.rhn.domain.action.Action;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * Serializer for Action class
 */
public class ActionSerializer extends ApiResponseSerializer<Action> {

    @Override
    public SerializedApiResponse serialize(Action action) {
        return getSerializationBuilder(action).build();
    }

    /**
     * @return a serialization builder using the Action provided as parameter
     * @param act - Action instance being serialized
     */
    public static SerializationBuilder getSerializationBuilder(Action act) {
        SerializationBuilder builder = new SerializationBuilder()
                .add("failed_count", act.getFailedCount())
                .add("modified", act.getModified().toString())
                .add("created", act.getCreated().toString())
                .add("action_type", act.getActionType().getName())
                .add("successful_count", act.getSuccessfulCount())
                .add("earliest_action", act.getEarliestAction().toString())
                .add("archived", act.getArchived())
                .add("prerequisite", act.getPrerequisite())
                .add("name", act.getName())
                .add("id", act.getId())
                .add("version", act.getVersion().toString())
                .add("modified_date", act.getModified())
                .add("created_date", act.getCreated());
        if (act.getSchedulerUser() != null) {
            builder.add("scheduler_user", act.getSchedulerUser().getLogin());
        }
        return builder;
    }

    @Override
    public Class<Action> getSupportedClass() {
        return Action.class;
    }
}
