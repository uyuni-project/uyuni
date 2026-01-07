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
package com.suse.manager.webui.controllers.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.SparkTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.ssl.SSLCertData;
import com.suse.manager.ssl.SSLCertManager;
import com.suse.manager.ssl.SSLCertPair;
import com.suse.manager.webui.controllers.ProxyController;

import org.jmock.Expectations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;

import spark.Request;

/**
 * Test ProxyController class
 */
public class ProxyControllerTest extends BaseControllerTestCase {

    private static final String TEST_DIR = "/com/suse/manager/webui/controllers/test/";

    private ProxyController proxyController;
    private SystemManager systemManager;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        systemManager = context().mock(SystemManager.class);
        proxyController = new ProxyController(systemManager);
    }

    @Test
    public void testGenerateContainerConfigExternal() throws Exception {
        byte[] data = "config data".getBytes();

        context().checking(new Expectations() {{
            oneOf(systemManager).createProxyContainerConfig(
                    with(equal(user)), with(equal("pxy.acme.lab")), with(equal(8022)), with(equal("srv.acme.lab")),
                    with(equal(2048L)), with(equal("coyote@acme.lab")), with(equal("Root CA")),
                    with(equal(List.of("CA1", "CA2"))), with(equal(new SSLCertPair("CERT", "KEY"))),
                    with(aNull(SSLCertPair.class)), with(aNull(String.class)), with(aNull(SSLCertData.class)),
                    with(any(SSLCertManager.class)));
            will(returnValue(data));
        }});

        String path = new File(TestUtils.findTestData(TEST_DIR + "proxy-container-data-external.json")
                .getPath()).getAbsolutePath();
        String content = FileUtils.readStringFromFile(path);
        Request request = SparkTestUtils.createMockRequestWithBody("/manager/api/proxy/container-config",
                Collections.emptyMap(), content);
        assertEquals("\"pxy-config.tar.gz\"", proxyController.generateContainerConfig(request, response, user));
        assertEquals(data, request.session().attribute("pxy-config.tar.gz"));
    }

    @Test
    public void testGenerateContainerConfigGenerate() throws Exception {
        byte[] data = "config data".getBytes();

        context().checking(new Expectations() {{
            oneOf(systemManager).createProxyContainerConfig(
                    with(equal(user)), with(equal("pxy.acme.lab")), with(equal(22)),
                    with(equal("srv.acme.lab")), with(equal(2048L)), with(equal("coyote@acme.lab")),
                    with(aNull(String.class)), with(aNull(List.class)), with(aNull(SSLCertPair.class)),
                    with(equal(new SSLCertPair("CA CERT", "CA KEY"))), with(equal("secret")),
                    with(equal(new SSLCertData("pxy.acme.lab", List.of("cname1", "cname2"), "DE", "Bavaria", "Nurnberg",
                            "SUSE", "SUSE Unit", "roadrunner@acme.lab"))),
                    with(any(SSLCertManager.class)));
            will(returnValue(data));
        }});

        String path = new File(TestUtils.findTestData(TEST_DIR + "proxy-container-data-generate.json")
                .getPath()).getAbsolutePath();
        String content = FileUtils.readStringFromFile(path);
        Request request = SparkTestUtils.createMockRequestWithBody("/manager/api/proxy/container-config",
                Collections.emptyMap(), content);
        assertEquals("\"pxy-config.tar.gz\"", proxyController.generateContainerConfig(request, response, user));
        assertEquals(data, request.session().attribute("pxy-config.tar.gz"));
    }
}
