/*
 * Copyright (c) 2022 SUSE LLC
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
package com.suse.manager.ssl.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.testing.RhnJmockBaseTestCase;

import com.suse.manager.ssl.SSLCertData;
import com.suse.manager.ssl.SSLCertManager;
import com.suse.manager.ssl.SSLCertPair;
import com.suse.manager.utils.ExecHelper;

import org.hamcrest.collection.IsArrayContainingInAnyOrder;
import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

public class SSLCertManagerTest extends RhnJmockBaseTestCase {

    private Runtime runtime;
    private Process process;
    private File tempDir;
    private SSLCertManager certManager;

    @BeforeEach
    public void setUp() throws Exception {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        runtime = mock(Runtime.class);
        process = mock(Process.class);
        tempDir = Files.createTempDirectory("testssl").toFile();
        certManager = new SSLCertManager(new ExecHelper(() -> runtime), tempDir);
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        org.apache.commons.io.FileUtils.deleteDirectory(tempDir);
    }

    @Test
    public void testGenerateSSLCert() throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        context().checking(new Expectations() {{
            allowing(runtime).exec(with(IsArrayContainingInAnyOrder.arrayContainingInAnyOrder(
                    "rhn-ssl-tool", "--gen-server", "-q", "--no-rpm", "-d", tempDir.getAbsolutePath(),
                    "--ca-cert", "ca.crt", "--ca-key", "ca.key", "--set-hostname", "server.acme.lab",
                    "--set-cname", "srv1.acme.lab", "--set-cname", "srv2.acme.lab", "--set-country", "DE",
                    "--set-state", "Bayern", "--set-city", "Nurnberg", "--set-org", "SUSE",
                    "--set-org-unit", "SUSE Tests", "--set-email", "fakemail")));
            will(returnValue(process));
            allowing(process).getOutputStream();
            will(returnValue(outStream));
            allowing(process).getInputStream();
            will(returnValue(new ByteArrayInputStream("".getBytes())));
            allowing(process).waitFor();
            will(returnValue(0));
        }});

        File srvDir = new File(tempDir, "server");
        srvDir.mkdir();
        FileUtils.writeStringToFile("FakeCert", new File(srvDir, "server.crt").getAbsolutePath());
        FileUtils.writeStringToFile("FakeKey", new File(srvDir, "server.key").getAbsolutePath());

        SSLCertData sslData = new SSLCertData("server.acme.lab", List.of("srv1.acme.lab", "srv2.acme.lab"),
                "DE", "Bayern", "Nurnberg", "SUSE", "SUSE Tests", "fakemail");

        SSLCertPair actual = certManager.generateCertificate(new SSLCertPair("caCert", "caKey"), "capassword", sslData);
        assertEquals(new SSLCertPair("FakeCert", "FakeKey"), actual);

        assertEquals("capassword", outStream.toString(StandardCharsets.US_ASCII));

        assertFalse(tempDir.exists());
    }

    @Test
    public void testGetNamesFromCert() throws Exception {
        String mockedOutput = "subject=C = US, ST = North Carolina, O = Example Corp. Inc., OU = unit, " +
                    "CN = dev-pxy-test.mgr.lab, emailAddress = admin@example.com\n" +
                "X509v3 Subject Alternative Name: \n" +
                "    DNS:dev-foo.mgr.lab, DNS:dev-bar.mgr.lab\n" +
                "    DNS:*.example.com\n";
        String mockedCert = "FAKE CERT";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        context().checking(new Expectations() {{
            allowing(runtime).exec(with(IsArrayContainingInAnyOrder.arrayContainingInAnyOrder(
                            "openssl", "x509", "-noout", "-subject", "-ext", "subjectAltName")));
            will(returnValue(process));
            allowing(process).getInputStream();
            will(returnValue(new ByteArrayInputStream(mockedOutput.getBytes())));
            allowing(process).getOutputStream();
            will(returnValue(outputStream));
            allowing(process).waitFor();
            will(returnValue(0));
        }});
        Set<String> names = certManager.getNamesFromSslCert("FAKE CERT");
        Set<String> expected = Set.of("dev-pxy-test.mgr.lab", "dev-foo.mgr.lab", "dev-bar.mgr.lab", "*.example.com");
        assertEquals(expected, names);
        assertEquals(mockedCert, outputStream.toString(StandardCharsets.US_ASCII));
    }
}
