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

package com.suse.manager.webui.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.manager.audit.SystemCVEAuditManagerTest;
import com.redhat.rhn.testing.ErrataTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import spark.ModelAndView;
import spark.Request;

public class CVEAuditControllerSystemCveTest extends BaseControllerTestCase {

    private static final String API_URL = "/manager/api/systems/:sid/details/audit/cve/:statuses/:severities";

    @AfterEach
    public void tearDownCVEAuditControllerSystemCveTest() {
        Config.get().setString(ConfigDefaults.CVE_AUDIT_ENABLE_OVAL_METADATA, "false");
    }

    private Server createServerWithAffectedCve(String cveName) throws Exception {
        Channel channel = ErrataTestUtils.createTestChannel(user);
        Server server = ErrataTestUtils.createTestServer(user, Set.of(channel));
        String cpe = "cpe:/o:uyunitest:" + TestUtils.randomString(8).toLowerCase() + ":1";
        server.setCpe(cpe);
        server = TestUtils.saveAndFlush(server);

        String pkgName = "uyuni-cve-test-" + TestUtils.randomString(8).toLowerCase();
        Package pkg = ErrataTestUtils.createTestPackage(user, channel, "x86_64", pkgName, "0", "1.0", "1");
        ErrataTestUtils.createTestInstalledPackage(pkg, server);
        TestUtils.flushSession();

        SystemCVEAuditManagerTest.addOvalVulnerablePackage(cpe, cveName, pkgName, "1.0", "2");
        return server;
    }

    @Test
    public void testSystemCveViewUsesTemplate() throws Exception {
        Config.get().setString(ConfigDefaults.CVE_AUDIT_ENABLE_OVAL_METADATA, "false");
        Server server = ErrataTestUtils.createTestServer(user);
        Request request = getRequestWithCsrf("/manager/systems/details/audit/cve");

        ModelAndView mv = CVEAuditController.systemCveView(request, response, user, server);

        assertEquals("templates/minion/system-cve.jade", mv.getViewName());
        assertEquals("DISABLED", ((Map<?, ?>) mv.getModel()).get("ovalState"));
    }

    @Test
    public void testSystemCvesReturnsPagedJson() throws Exception {
        Config.get().setString(ConfigDefaults.CVE_AUDIT_ENABLE_OVAL_METADATA, "true");
        String cveName = "CVE-2099-" + TestUtils.randomNumeric(7);
        Server server = createServerWithAffectedCve(cveName);

        Request request = getRequestWithCsrfAndParams(API_URL,
                Map.of("p", "1", "ps", "10", "s", "1", "sc", "status"),
                server.getId(), "AFFECTED_PATCH_UNAVAILABLE_IN_UYUNI,PATCHED", "all");
        String json = CVEAuditController.systemCves(request, response, user, server);

        JsonObject result = JsonParser.parseString(json).getAsJsonObject();
        assertEquals(1, result.get("total").getAsLong());
        JsonObject item = result.getAsJsonArray("items").get(0).getAsJsonObject();
        assertEquals(cveName, item.get("cveName").getAsString());
        assertEquals("AFFECTED_PATCH_UNAVAILABLE_IN_UYUNI", item.get("status").getAsString());
    }

    @Test
    public void testSystemCvesSearchAndPaging() throws Exception {
        Config.get().setString(ConfigDefaults.CVE_AUDIT_ENABLE_OVAL_METADATA, "true");
        String prefix = "CVE-2098-" + TestUtils.randomNumeric(5);
        Server server = createServerWithAffectedCve(prefix + "0");
        String pkgName = "uyuni-cve-test-" + TestUtils.randomString(8).toLowerCase();
        Package pkg = ErrataTestUtils.createTestPackage(user, ErrataTestUtils.createTestChannel(user), "x86_64",
                pkgName, "0", "1.0", "1");
        ErrataTestUtils.createTestInstalledPackage(pkg, server);
        TestUtils.flushSession();
        for (int i = 1; i < 3; i++) {
            SystemCVEAuditManagerTest.addOvalVulnerablePackage(server.getCpe(), prefix + i, pkgName, "1.0", "2");
        }
        SystemCVEAuditManagerTest.addOvalVulnerablePackage(server.getCpe(), "CVE-2099-" + TestUtils.randomNumeric(7),
                pkgName, "1.0", "2");

        Request request = getRequestWithCsrfAndParams(API_URL,
                Map.of("p", "1", "ps", "2", "q", prefix, "qc", "cveName", "s", "1", "sc", "cveName"),
                server.getId(), "AFFECTED_PATCH_UNAVAILABLE_IN_UYUNI", "all");
        JsonObject result = JsonParser.parseString(CVEAuditController.systemCves(request, response, user, server))
                .getAsJsonObject();

        assertEquals(3, result.get("total").getAsLong());
        assertEquals(2, result.getAsJsonArray("items").size());
        assertEquals(prefix + "0",
                result.getAsJsonArray("items").get(0).getAsJsonObject().get("cveName").getAsString());
    }

    @Test
    public void testSystemCvesStatusFilterExcludesRows() throws Exception {
        Config.get().setString(ConfigDefaults.CVE_AUDIT_ENABLE_OVAL_METADATA, "true");
        Server server = createServerWithAffectedCve("CVE-2099-" + TestUtils.randomNumeric(7));

        Request request = getRequestWithCsrfAndParams(API_URL,
                Map.of("p", "1", "ps", "10"), server.getId(), "PATCHED", "all");
        String json = CVEAuditController.systemCves(request, response, user, server);

        assertEquals(0, JsonParser.parseString(json).getAsJsonObject().get("total").getAsLong());
    }

    @Test
    public void testSystemCvesSeverityFilter() throws Exception {
        Config.get().setString(ConfigDefaults.CVE_AUDIT_ENABLE_OVAL_METADATA, "true");
        Server server = createServerWithAffectedCve("CVE-2099-" + TestUtils.randomNumeric(7));

        Request unknown = getRequestWithCsrfAndParams(API_URL, Map.of("p", "1", "ps", "10"), server.getId(),
                "AFFECTED_PATCH_UNAVAILABLE_IN_UYUNI", "UNKNOWN");
        assertEquals(1, JsonParser.parseString(CVEAuditController.systemCves(unknown, response, user, server))
                .getAsJsonObject().get("total").getAsLong());

        Request critical = getRequestWithCsrfAndParams(API_URL, Map.of("p", "1", "ps", "10"), server.getId(),
                "AFFECTED_PATCH_UNAVAILABLE_IN_UYUNI", "CRITICAL");
        assertEquals(0, JsonParser.parseString(CVEAuditController.systemCves(critical, response, user, server))
                .getAsJsonObject().get("total").getAsLong());
    }
}
