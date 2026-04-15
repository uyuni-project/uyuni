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
package com.redhat.rhn.common.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

public class ClientCertificateDigesterTest extends RhnBaseTestCase {

    @Test
    public void testBuildSystemIdStream() throws Exception {
        ClientCertificate cert = ClientCertificateDigester.buildCertificate(
                    TestUtils.findTestData("systemid.xml").openStream());

        assertNotNull(cert, "SystemId is null");

        // hardcoded key from test system
        cert.validate("h13MzDNjItVNsXd2YOU7etBUh8EefWdKouUM7DETP5ISYWxXAa9vnWYZV7LD7EuM");
    }

    @Test
    public void testBuildSystemIdReader() throws Exception {
        String data = TestUtils.readAll(TestUtils.findTestData("systemid.xml"));
        StringReader rdr = new StringReader(data);
        ClientCertificate cert =
               ClientCertificateDigester.buildCertificate(rdr);

        assertNotNull(cert, "SystemId is null");

        // hardcoded key from test system
        cert.validate("h13MzDNjItVNsXd2YOU7etBUh8EefWdKouUM7DETP5ISYWxXAa9vnWYZV7LD7EuM");
    }

    @Test
    public void testGetValueByName() throws Exception {
        ClientCertificate cert = ClientCertificateDigester.buildCertificate(
                TestUtils.findTestData("systemid.xml").openStream());

        assertEquals("4AS", cert.getValueByName("os_release"));
        assertEquals("682269dc79d694980f9c2aa3c8aa3a74d6c2f634f7a7e2bcbb3c6059b9800ee2",
                cert.getValueByName("checksum"));
        assertEquals("REAL", cert.getValueByName("type"));
        assertEquals("x86_64", cert.getValueByName("architecture"));
        assertEquals("ID-1005691306", cert.getValueByName("system_id"));
        assertEquals("Rat's Hat Linux", cert.getValueByName("operating_system"));
        assertEquals("firefox104", cert.getValueByName("profile_name"));
        assertEquals("jesusr_redhat", cert.getValueByName("username"));

        // test a field which has multiple values
        assertEquals("system_id", cert.getValueByName("fields"));

        // test null
        assertNull(cert.getValueByName(null));

        // test invalid name
        assertNull(cert.getValueByName("invalid name"));
    }

    @Test
    public void testGetValuesByName() throws Exception {
        ClientCertificate cert = ClientCertificateDigester.buildCertificate(
                TestUtils.findTestData("systemid.xml").openStream());

        assertEquals("4AS", cert.getValuesByName("os_release")[0]);
        assertEquals("682269dc79d694980f9c2aa3c8aa3a74d6c2f634f7a7e2bcbb3c6059b9800ee2",
                cert.getValuesByName("checksum")[0]);
        assertEquals("REAL", cert.getValuesByName("type")[0]);
        assertEquals("x86_64", cert.getValuesByName("architecture")[0]);
        assertEquals("ID-1005691306", cert.getValuesByName("system_id")[0]);
        assertEquals("Rat's Hat Linux",
                cert.getValuesByName("operating_system")[0]);
        assertEquals("firefox104", cert.getValuesByName("profile_name")[0]);
        assertEquals("jesusr_redhat", cert.getValuesByName("username")[0]);

        // test fields
        String[] values = cert.getValuesByName("fields");
        assertNotNull(values);
        assertEquals(6, values.length);
        assertEquals("system_id", values[0]);
        assertEquals("os_release", values[1]);
        assertEquals("operating_system", values[2]);
        assertEquals("architecture", values[3]);
        assertEquals("username", values[4]);
        assertEquals("type", values[5]);

        // test null
        assertNull(cert.getValuesByName(null));

        // test invalid name
        assertNull(cert.getValuesByName("invalid name"));
    }
}
