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

package com.suse.manager.webui.controllers.admin.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.common.RhnConfiguration;
import com.redhat.rhn.domain.common.RhnConfigurationFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.TestStatics;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.webui.controllers.BaseControllerTestCase;
import com.suse.manager.webui.controllers.admin.service.ServerSettingsService;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class ServerSettingsControllerTest extends BaseControllerTestCase {

    private static final String URL = "/manager/api/admin/config/server-settings";

    private final RhnConfigurationFactory factory = RhnConfigurationFactory.getSingleton();
    private ServerSettingsController controller;
    private User satAdmin;

    @BeforeEach
    public void setUp() {
        satAdmin = UserTestUtils.createUser(TestStatics.TEST_SAT_USER, user.getOrg().getId());
        satAdmin.addPermanentRole(RoleFactory.SAT_ADMIN);
        controller = new ServerSettingsController(new ServerSettingsService());
    }

    // Gson can't deserialize records, so the response is checked as JSON
    private static JsonObject parse(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    private static boolean isSuccess(JsonObject result) {
        return result.get("success").getAsBoolean();
    }

    private static Optional<JsonObject> getEntry(JsonObject result, RhnConfiguration.KEYS key) {
        return StreamSupport.stream(result.getAsJsonArray("data").spliterator(), false)
                .map(JsonElement::getAsJsonObject)
                .filter(entry -> entry.get("key").getAsString().equals(key.name()))
                .findFirst();
    }

    @Test
    public void testPermissionCheck() {
        assertThrows(PermissionException.class, () ->
                controller.listSettings(getRequestWithCsrf(URL), response, user));
        assertThrows(PermissionException.class, () ->
                controller.updateSettings(getPostRequestWithCsrfAndBody(URL, "{}"), response, user));
        assertThrows(PermissionException.class, () ->
                controller.resetSetting(getPostRequestWithCsrfAndBody(URL + "/reset", "{}"), response, user));
    }

    @Test
    public void testList() {
        JsonObject result = parse(controller.listSettings(getRequestWithCsrf(URL), response, satAdmin));

        assertTrue(isSuccess(result));
        assertTrue(getEntry(result, RhnConfiguration.KEYS.REPOSYNC_TIMEOUT).isPresent());
        assertTrue(getEntry(result, RhnConfiguration.KEYS.HTTP_PROXY_PASSWORD).isEmpty());
    }

    @Test
    public void testUpdate() throws UnsupportedEncodingException {
        String body = "{\"REPOSYNC_TIMEOUT\": 600, \"SYNC_SOURCE_PACKAGES\": true}";
        JsonObject result =
                parse(controller.updateSettings(getPostRequestWithCsrfAndBody(URL, body), response, satAdmin));

        assertTrue(isSuccess(result));
        assertEquals(600, factory.getInteger(RhnConfiguration.KEYS.REPOSYNC_TIMEOUT));
        assertTrue(factory.getBoolean(RhnConfiguration.KEYS.SYNC_SOURCE_PACKAGES));
        JsonObject entry = getEntry(result, RhnConfiguration.KEYS.REPOSYNC_TIMEOUT).orElseThrow();
        assertEquals("600", entry.get("value").getAsString());
        assertTrue(entry.get("modified").getAsBoolean());
    }

    @Test
    public void testUpdateInvalidValue() throws UnsupportedEncodingException {
        String body = "{\"REPOSYNC_TIMEOUT\": \"ten\"}";
        JsonObject result =
                parse(controller.updateSettings(getPostRequestWithCsrfAndBody(URL, body), response, satAdmin));

        assertFalse(isSuccess(result));
        assertEquals(400, response.status());
        assertTrue(result.getAsJsonObject("errors").has(RhnConfiguration.KEYS.REPOSYNC_TIMEOUT.name()));
    }

    @Test
    public void testUpdateInvalidBody() throws UnsupportedEncodingException {
        JsonObject result =
                parse(controller.updateSettings(getPostRequestWithCsrfAndBody(URL, "[1, 2]"), response, satAdmin));

        assertFalse(isSuccess(result));
        assertEquals(400, response.status());
    }

    @Test
    public void testReset() throws UnsupportedEncodingException {
        factory.setValue(RhnConfiguration.KEYS.REPOSYNC_TIMEOUT, 600);

        String body = "{\"key\": \"REPOSYNC_TIMEOUT\"}";
        JsonObject result = parse(
                controller.resetSetting(getPostRequestWithCsrfAndBody(URL + "/reset", body), response, satAdmin));

        assertTrue(isSuccess(result));
        assertNull(factory.getConfiguration(RhnConfiguration.KEYS.REPOSYNC_TIMEOUT).getValue());
    }

    @Test
    public void testResetHiddenKey() throws UnsupportedEncodingException {
        String body = "{\"key\": \"HTTP_PROXY_PASSWORD\"}";
        JsonObject result = parse(
                controller.resetSetting(getPostRequestWithCsrfAndBody(URL + "/reset", body), response, satAdmin));

        assertFalse(isSuccess(result));
        assertEquals(400, response.status());
    }
}
