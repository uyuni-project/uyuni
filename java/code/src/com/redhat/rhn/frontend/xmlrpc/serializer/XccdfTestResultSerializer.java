/*
 * Copyright (c) 2012 Red Hat, Inc.
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
import com.redhat.rhn.domain.action.scap.ScapActionDetails;
import com.redhat.rhn.domain.audit.XccdfBenchmark;
import com.redhat.rhn.domain.audit.XccdfProfile;
import com.redhat.rhn.domain.audit.XccdfTestResult;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;


/**
 * XccdfTestResultSerializer
 * @apidoc.doc
 * #struct_begin("OpenSCAP XCCDF Scan")
 *   #prop_desc("int", "xid", "XCCDF TestResult ID")
 *   #prop_desc("int", "sid", "serverId")
 *   #prop_desc("int", "action_id", "ID of the parent action")
 *   #prop_desc("string", "path", "path to XCCDF document")
 *   #prop_desc("string", "ovalfiles", "optional OVAL files")
 *   #prop_desc("string", "oscap_parameters", "oscap command-line arguments")
 *   #prop_desc("string", "test_result", "identifier of XCCDF TestResult")
 *   #prop_desc("string", "benchmark", "identifier of XCCDF Benchmark")
 *   #prop_desc("string", "benchmark_version" , "version of the Benchmark")
 *   #prop_desc("string", "profile", "identifier of XCCDF Profile")
 *   #prop_desc("string", "profile_title", "title of XCCDF Profile")
 *   #prop_desc($date, "start_time", "client machine time of scan start")
 *   #prop_desc($date, "end_time", "client machine time of scan completion")
 *   #prop_desc("string", "errors", "stderr output of scan")
 *   #prop_desc("boolean", "deletable", "indicates whether the scan can be deleted")
 * #struct_end()
 */
public class XccdfTestResultSerializer extends ApiResponseSerializer<XccdfTestResult> {

    @Override
    public Class<XccdfTestResult> getSupportedClass() {
        return XccdfTestResult.class;
    }

    @Override
    public SerializedApiResponse serialize(XccdfTestResult src) {
        ScapActionDetails actionDetails = src.getScapActionDetails();
        XccdfBenchmark benchmark = src.getBenchmark();
        XccdfProfile profile = src.getProfile();
        Action parentAction = actionDetails.getParentAction();

        SerializationBuilder builder = new SerializationBuilder();
        add(builder, "xid", src.getId());
        add(builder, "sid", src.getServer().getId());
        add(builder, "path", actionDetails.getPath());
        add(builder, "ovalfiles", actionDetails.getOvalfiles());
        add(builder, "oscap_parameters", actionDetails.getParametersContents());
        add(builder, "test_result", src.getIdentifier());
        add(builder, "benchmark", benchmark.getIdentifier());
        add(builder, "benchmark_version", benchmark.getVersion());
        add(builder, "profile", profile.getIdentifier());
        add(builder, "profile_title", profile.getTitle());
        add(builder, "start_time", src.getStartTime());
        add(builder, "end_time", src.getEndTime());
        add(builder, "errors", src.getErrrosContents());
        add(builder, "action_id", parentAction.getId());
        add(builder, "deletable", src.getDeletable());
        return builder.build();
    }

    private static void add(SerializationBuilder builder, String label, Object value) {
        if (value != null) {
            builder.add(label, value);
        }
    }
}
