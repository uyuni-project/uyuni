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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.utils.salt.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.webui.utils.salt.custom.HwProfileUpdateSlsResult;
import com.suse.utils.Json;

import com.google.gson.reflect.TypeToken;

import org.junit.jupiter.api.Test;

import java.util.List;

public class HwProfileUpdateSlsResultTest {

    @Test
    public void testNoDnsFqdns() throws Exception {
        String jsonResult = TestUtils.readAll(TestUtils.findTestData("hw_profile_update_res.json"));
        jsonResult = jsonResult.replace("\"dns_fqdns_changes\": {}", "\"changes\": {}");
        TypeToken<HwProfileUpdateSlsResult> typeToken = new TypeToken<>() {
        };
        HwProfileUpdateSlsResult results = Json.GSON.fromJson(jsonResult, typeToken.getType());
        assertTrue(results.getDnsFqdns().isEmpty());
    }

    @Test
    public void testDnsFqdns() throws Exception {
        String jsonResult = TestUtils.readAll(TestUtils.findTestData("hw_profile_update_res.json"));
        String changes = "\"ret\": { \"dns_fqdns\": [\"min-centos7-dns.test.lab\"]}";
        jsonResult = jsonResult.replace("\"dns_fqdns_changes\": {}", "\"changes\": {" + changes + "}");
        TypeToken<HwProfileUpdateSlsResult> typeToken = new TypeToken<>() {
        };
        HwProfileUpdateSlsResult results = Json.GSON.fromJson(jsonResult, typeToken.getType());
        assertEquals(List.of("min-centos7-dns.test.lab"), results.getDnsFqdns());
    }
}
