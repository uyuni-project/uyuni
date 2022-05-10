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

import com.redhat.rhn.frontend.dto.MultiOrgUserOverview;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 *
 * MultiOrgAllUserOverviewSerializer
 *
 * @apidoc.doc
 * #struct_begin("user")
 *   #prop("string", "login")
 *   #prop("string", "login_uc")
 *   #prop("string", "name")
 *   #prop("string", "email")
 *   #prop("boolean", "is_org_admin")
 * #struct_end()
 */
public class MultiOrgUserOverviewSerializer extends ApiResponseSerializer<MultiOrgUserOverview> {

    @Override
    public Class<MultiOrgUserOverview> getSupportedClass() {
        return MultiOrgUserOverview.class;
    }

    @Override
    public SerializedApiResponse serialize(MultiOrgUserOverview src) {
        return new SerializationBuilder()
                .add("login", src.getLogin())
                .add("login_uc", src.getLoginUc())
                .add("name", src.getUserDisplayName())
                .add("email", src.getAddress())
                .add("is_org_admin", src.getOrgAdmin() == 1)
                .build();
    }
}
