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

import com.redhat.rhn.frontend.xmlrpc.audit.CVEAffectedServer;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

public class CVEAffectedServerSerializer extends ApiResponseSerializer<CVEAffectedServer> {
    @Override
    public SerializedApiResponse serialize(CVEAffectedServer src) {
        return new SerializationBuilder()
                .add("system_id", src.getId())
                .add("system_name", src.getName())
                .add("affected_packages", src.getAffectedPackages())
                .build();
    }

    @Override
    public Class<CVEAffectedServer> getSupportedClass() {
        return CVEAffectedServer.class;
    }
}
