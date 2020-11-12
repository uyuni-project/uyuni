/*
 * Copyright (c) 2016 SUSE LLC
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
package com.redhat.rhn.frontend.xmlrpc.serializer.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.frontend.xmlrpc.serializer.SUSEInstalledProductSerializer;
import com.redhat.rhn.frontend.xmlrpc.system.SUSEInstalledProduct;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcSerializer;

public class SUSEInstalledProductSerializerTest  {

    @Test
    public void testSerialize() throws Exception {
        SUSEInstalledProduct product = new SUSEInstalledProduct("sles", "12",
                "x86_64", null, true, "SUSE Linux Enterprise Server 12");
        Writer output = new StringWriter();

        SUSEInstalledProductSerializer serializer = new SUSEInstalledProductSerializer();
        serializer.serialize(product, output, new XmlRpcSerializer());
        String finalOutput = output.toString();

        assertTrue(finalOutput
                .contains("<name>name</name><value><string>sles</string></value>"));
        assertTrue(finalOutput
                .contains("<name>version</name><value><string>12</string></value>"));
        assertTrue(finalOutput
                .contains("<name>arch</name><value><string>x86_64</string></value>"));
        assertTrue(finalOutput
                .contains("<name>friendlyName</name><value><string>SUSE Linux Enterprise " +
                        "Server 12</string></value>"));
        assertTrue(finalOutput
                .contains("<name>isBaseProduct</name><value><boolean>1</boolean>" +
                        "</value>"));
        assertFalse(finalOutput.contains("release"));
    }

}

