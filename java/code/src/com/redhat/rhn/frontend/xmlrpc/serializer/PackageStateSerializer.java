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


import com.redhat.rhn.domain.state.PackageState;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 *
 * PackageStateSerializer
 *
 * @xmlrpc.doc
 *
 * #struct_begin("packagestate")
 *     #prop("int", "id")
 *     #prop("string", "name")
 *     #prop_desc("int",  "state_revision_id", "State revision Id")
 *     #prop_desc("string",  "package_state_type_id", "INSTALLED or REMOVED")
 *     #prop_desc("string",  "version_constraint_id", "LATEST or ANY")
 * #struct_end()
 */
public class PackageStateSerializer extends ApiResponseSerializer<PackageState> {

    @Override
    public Class<PackageState> getSupportedClass() {
        return PackageState.class;
    }

    @Override
    public SerializedApiResponse serialize(PackageState src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("name", src.getName().getName())
                .add("state_revision_id", src.getStateRevision().getId())
                .add("package_state_type_id", src.getPackageState().name())
                .add("version_constraint_id", src.getVersionConstraint().name())
                .build();
    }
}
