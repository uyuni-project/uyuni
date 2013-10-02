/**
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

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.codec.binary.Base64;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.action.script.ScriptResult;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;


/**
 * Converts a ScriptResult to an XMLRPC &lt;struct&gt;.
 * @version $Rev$
 *
 * @xmlrpc.doc
 *  #struct("script result")
 *      #prop_desc("int", "serverId", "ID of the server the script runs on.")
 *      #prop_desc("dateTime.iso8601", "startDate", "Time script began execution.")
 *      #prop_desc("dateTime.iso8601", "stopDate", "Time script stopped execution.")
 *      #prop_desc("int", "returnCode", "Script execution return code.")
 *      #prop_desc("string", "output", "Output of the script (base64 encoded according
                to the output_enc64 attribute)")
 *      #prop_desc("boolean", "output_enc64", "Identifies base64 encoded output")
 *  #struct_end()
 *
 */
public class ScriptResultSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    public Class getSupportedClass() {
        return ScriptResult.class;
    }

    /** {@inheritDoc} */
    protected void doSerialize(Object value, Writer output, XmlRpcSerializer serializer)
        throws XmlRpcException, IOException {
        ScriptResult scriptResult = (ScriptResult)value;
        SerializerHelper helper = new SerializerHelper(serializer);
        helper.add("serverId", scriptResult.getServerId());
        helper.add("startDate", scriptResult.getStartDate());
        helper.add("stopDate", scriptResult.getStopDate());
        helper.add("returnCode", scriptResult.getReturnCode());
        String outputContents = scriptResult.getOutputContents();
        if (StringUtil.containsInvalidXmlChars2(outputContents)) {
            helper.add("output_enc64", Boolean.TRUE);
            helper.add("output", new String(Base64.encodeBase64(outputContents
                    .getBytes("UTF-8")), "UTF-8"));
        }
        else {
            helper.add("output_enc64", Boolean.FALSE);
            helper.add("output", scriptResult.getOutputContents());
        }
        helper.writeTo(output);
    }
}
