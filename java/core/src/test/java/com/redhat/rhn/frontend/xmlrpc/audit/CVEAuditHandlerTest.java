/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.redhat.rhn.frontend.xmlrpc.audit;

import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannel;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestInstalledPackage;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestPackage;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestServer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.frontend.xmlrpc.BaseHandlerTestCase;
import com.redhat.rhn.frontend.xmlrpc.MethodInvalidParamException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchSystemException;
import com.redhat.rhn.frontend.xmlrpc.serializer.SystemCVEResultSerializer;
import com.redhat.rhn.frontend.xmlrpc.system.XmlRpcSystemHelper;
import com.redhat.rhn.manager.audit.PatchStatus;
import com.redhat.rhn.manager.audit.SystemCVEAuditManagerTest;
import com.redhat.rhn.manager.audit.SystemCVEResult;
import com.redhat.rhn.testing.TestUtils;

import com.suse.oval.OvalSeverity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CVEAuditHandlerTest extends BaseHandlerTestCase {

    // The bootstrappers are not used by the system lookup
    private final CVEAuditHandler handler = new CVEAuditHandler(new XmlRpcSystemHelper(null, null));

    private Server server;
    private String cveName;

    @BeforeEach
    public void setUpCVEAuditHandlerTest() throws Exception {
        Config.get().setString(ConfigDefaults.CVE_AUDIT_ENABLE_OVAL_METADATA, "true");

        Channel channel = createTestChannel(admin);
        server = createTestServer(admin, Set.of(channel));
        String cpe = "cpe:/o:uyunitest:" + TestUtils.randomString(8).toLowerCase() + ":1";
        server.setCpe(cpe);
        server = TestUtils.saveAndFlush(server);

        String packageName = "uyuni-cve-test-" + TestUtils.randomString(8).toLowerCase();
        createTestInstalledPackage(createTestPackage(admin, channel, "x86_64", packageName, "0", "1.0", "1"), server);
        TestUtils.flushSession();

        cveName = "CVE-2099-" + TestUtils.randomNumeric(7);
        SystemCVEAuditManagerTest.addOvalVulnerablePackage(cpe, cveName, packageName, null, null,
                OvalSeverity.CRITICAL);
    }

    @AfterEach
    public void tearDownCVEAuditHandlerTest() {
        Config.get().setString(ConfigDefaults.CVE_AUDIT_ENABLE_OVAL_METADATA, "false");
    }

    @Test
    public void testListCvesBySystem() {
        List<SystemCVEResult> result = handler.listCvesBySystem(admin, server.getId().intValue());

        assertEquals(1, result.size());
        assertEquals(cveName, result.get(0).getCveName());
        assertEquals(PatchStatus.AFFECTED_PATCH_UNAVAILABLE, result.get(0).getStatus());
        assertEquals(OvalSeverity.CRITICAL, result.get(0).getSeverity());
    }

    @Test
    public void testFilterByPatchStatusAndSeverity() {
        int sid = server.getId().intValue();

        assertEquals(1, handler.listCvesBySystem(admin, sid, List.of("AFFECTED_PATCH_UNAVAILABLE"),
                List.of("CRITICAL")).size());
        assertTrue(handler.listCvesBySystem(admin, sid, List.of("PATCHED"), null).isEmpty());
        assertTrue(handler.listCvesBySystem(admin, sid, null, List.of("LOW", "UNKNOWN")).isEmpty());
        assertEquals(1, handler.listCvesBySystem(admin, sid, List.of(), List.of()).size());
    }

    @Test
    public void testInvalidLabelsAreRejected() {
        int sid = server.getId().intValue();

        assertThrows(MethodInvalidParamException.class,
                () -> handler.listCvesBySystem(admin, sid, List.of("BOGUS"), null));
        assertThrows(MethodInvalidParamException.class,
                () -> handler.listCvesBySystem(admin, sid, null, List.of("BOGUS")));
    }

    @Test
    public void testUnknownSystem() {
        assertThrows(NoSuchSystemException.class, () -> handler.listCvesBySystem(admin, Integer.MAX_VALUE));
    }

    @Test
    public void testEmptyWhenOvalIsDisabled() {
        Config.get().setString(ConfigDefaults.CVE_AUDIT_ENABLE_OVAL_METADATA, "false");

        assertTrue(handler.listCvesBySystem(admin, server.getId().intValue()).isEmpty());
    }

    @Test
    public void testSerialization() {
        SystemCVEResult result = handler.listCvesBySystem(admin, server.getId().intValue()).get(0);

        Map<String, Object> serialized = new HashMap<>();
        new SystemCVEResultSerializer().serialize(result)
                .forEach(serialized::put);

        assertEquals(result.getId(), serialized.get("cve_id"));
        assertEquals(cveName, serialized.get("cve_name"));
        assertEquals("AFFECTED_PATCH_UNAVAILABLE", serialized.get("patch_status"));
        assertEquals("CRITICAL", serialized.get("severity"));
        assertEquals(List.of(), serialized.get("errata"));
    }
}
