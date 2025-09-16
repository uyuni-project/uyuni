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

/**
 * Converts a ManagerInfoJson to an XMLRPC &lt;struct&gt;.
 *
 * @apidoc.doc
 *  #struct_begin("manager info")
 *      #prop_desc("string", "version", "version")
 *      #prop_desc("boolean", "report_db", "true if there is a report database")
 *      #prop_desc("string", "report_db_name", "name of the report database")
 *      #prop_desc("string", "report_db_host", "hostname of the report database")
 *      #prop_desc("int", "report_db_port", "port of the report database")
 *  #struct_end()
 */
public class ManagerInfoJsonSerializer extends ApiResponseSerializer<ManagerInfoJson> {

    @Override
    public Class<ManagerInfoJson> getSupportedClass() {
        return ManagerInfoJson.class;
    }

    @Override
    public SerializedApiResponse serialize(ManagerInfoJson src) {
        return new SerializationBuilder()
                .add("version", src.getVersion())
                .add("report_db", src.hasReportDb())
                .add("report_db_name", src.getReportDbName())
                .add("report_db_host", src.getReportDbHost())
                .add("report_db_port", src.getReportDbPort())
                .build();
    }
}
