/*
 * Copyright (c) 2010--2014 Red Hat, Inc.
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

import com.redhat.rhn.domain.notification.UserNotification;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 *ff
 * UserNotificationSerializer
 *
 * @apidoc.doc
 *  #struct_begin("notification")
 *      #prop("long", "id")
 *      #prop_array("boolean", "read")
 *      #prop_array("string", "message")
 *      #prop_array("notificationType", "type")
 *      #prop_array("date", "created")
 *  #struct_end()
 *
 */
public class UserNotificationSerializer extends ApiResponseSerializer<UserNotification> {

    @Override
    public Class<UserNotification> getSupportedClass() {
        return UserNotification.class;
    }

    @Override
    public SerializedApiResponse serialize(UserNotification src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("read", src.getRead())
                .add("message", src.getMessage().getData())
                .add("type", src.getMessage().getType())
                .add("created", src.getMessage().getCreated())
                .build();
    }
}
