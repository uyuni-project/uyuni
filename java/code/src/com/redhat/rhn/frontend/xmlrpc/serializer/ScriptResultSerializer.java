/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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

import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.action.script.ScriptResult;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;


/**
 * Converts a ScriptResult to an XMLRPC &lt;struct&gt;.
 *
 * @xmlrpc.doc
 *  #struct_begin("script result")
 *      #prop_desc("int", "serverId", "ID of the server the script runs on")
 *      #prop_desc("$date", "startDate", "time script began execution")
 *      #prop_desc("$date", "stopDate", "time script stopped execution")
 *      #prop_desc("int", "returnCode", "script execution return code")
 *      #prop_desc("string", "output", "output of the script (base64 encoded according
                to the output_enc64 attribute)")
 *      #prop_desc("boolean", "output_enc64", "identifies base64 encoded output")
 *  #struct_end()
 *
 */
public class ScriptResultSerializer extends ApiResponseSerializer<ScriptResult> {

    @Override
    public Class<ScriptResult> getSupportedClass() {
        return ScriptResult.class;
    }

    @Override
    public SerializedApiResponse serialize(ScriptResult src) {
        SerializationBuilder builder = new SerializationBuilder()
                .add("serverId", src.getServerId())
                .add("startDate", src.getStartDate())
                .add("stopDate", src.getStopDate())
                .add("returnCode", src.getReturnCode());

        String outputContents = src.getOutputContents();
        if (StringUtil.containsInvalidXmlChars2(outputContents)) {
            builder.add("output_enc64", Boolean.TRUE);
            builder.add("output", new String(Base64.encodeBase64(outputContents
                    .getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
        }
        else {
            builder.add("output_enc64", Boolean.FALSE);
            builder.add("output", src.getOutputContents());
        }
        return builder.build();
    }
}
