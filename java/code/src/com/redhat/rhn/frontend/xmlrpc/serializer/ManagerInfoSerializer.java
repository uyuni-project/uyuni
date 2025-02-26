/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.frontend.xmlrpc.serializer;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;
import com.suse.manager.model.hub.ManagerInfoJson;

public class ManagerInfoSerializer extends ApiResponseSerializer<ManagerInfoJson> {

    @Override
    public Class<ManagerInfoJson> getSupportedClass() {
        return ManagerInfoJson.class;
    }

    @Override
    public SerializedApiResponse serialize(ManagerInfoJson src) {
        return new SerializationBuilder()
                .add("version", src.getVersion())
                .add("reportDb", src.hasReportDb())
                .add("reportDbName", src.getReportDbName())
                .add("reportDbHost", src.getReportDbHost())
                .add("reportDbPort", src.getReportDbPort())
                .build();
    }
}
