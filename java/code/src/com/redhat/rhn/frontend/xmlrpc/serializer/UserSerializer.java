/*
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


import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 *
 * UserSerializer a serializer for the User class
 *
 * @apidoc.doc
 *      #struct_begin("user")
 *              #prop("int", "id")
 *              #prop("string", "login")
 *              #prop_desc("string", "login_uc", "upper case version of the login")
 *              #prop_desc("boolean", "enabled", "true if user is enabled,
 *                         false if the user is disabled")
 *      #struct_end()
 */
public class UserSerializer extends ApiResponseSerializer<User> {

    @Override
    public Class<User> getSupportedClass() {
        return User.class;
    }

    @Override
    public SerializedApiResponse serialize(User src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("login", src.getLogin())
                .add("login_uc", src.getLoginUc())
                .add("enabled", src.isDisabled())
                .build();
    }
}
