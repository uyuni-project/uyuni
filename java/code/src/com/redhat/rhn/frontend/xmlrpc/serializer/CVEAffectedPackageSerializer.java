/*
 * Copyright (c) 2023 SUSE LLC
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

import com.redhat.rhn.frontend.xmlrpc.audit.CVEAffectedPackage;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

public class CVEAffectedPackageSerializer extends ApiResponseSerializer<CVEAffectedPackage> {
    @Override
    public SerializedApiResponse serialize(CVEAffectedPackage src) {
        return new SerializationBuilder()
                .add("name", src.getPackageName())
                .add("installed_version", src.getInstalledVersion())
                .add("patched_version", src.getPatchedVersion())
                .add("patch_status", src.getStatus().toString())
                .build();
    }

    @Override
    public Class<CVEAffectedPackage> getSupportedClass() {
        return CVEAffectedPackage.class;
    }
}
