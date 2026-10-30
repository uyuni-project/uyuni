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

package com.suse.manager.webui.controllers.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.common.RhnConfiguration;
import com.redhat.rhn.domain.common.RhnConfigurationFactory;
import com.redhat.rhn.testing.BaseTestCase;

import com.suse.manager.webui.controllers.admin.beans.ServerSettingsEntry;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ServerSettingsServiceTest extends BaseTestCase {

    private final RhnConfigurationFactory factory = RhnConfigurationFactory.getSingleton();
    private final ServerSettingsService service = new ServerSettingsService();

    private ServerSettingsEntry getEntry(RhnConfiguration.KEYS key) {
        return service.list().stream().filter(e -> e.key().equals(key.name())).findFirst().orElseThrow();
    }

    @Test
    public void testListContainsOnlyVisibleKeys() {
        List<String> listed = service.list().stream().map(ServerSettingsEntry::key).toList();
        List<String> visible = Arrays.stream(RhnConfiguration.KEYS.values())
                .filter(key -> !key.isHidden())
                .map(Enum::name)
                .toList();

        assertEquals(visible.size(), listed.size());
        assertTrue(listed.containsAll(visible));
    }

    @Test
    public void testListIsSortedByKey() {
        List<String> listed = service.list().stream().map(ServerSettingsEntry::key).toList();

        assertEquals(listed.stream().sorted().toList(), listed);
    }

    @Test
    public void testListEntry() {
        factory.resetToDefault(RhnConfiguration.KEYS.REPOSYNC_TIMEOUT);

        ServerSettingsEntry entry = getEntry(RhnConfiguration.KEYS.REPOSYNC_TIMEOUT);
        assertEquals(RhnConfiguration.ValueType.INTEGER, entry.type());
        assertEquals("300", entry.value());
        assertEquals("300", entry.defaultValue());
        assertFalse(entry.modified());
        assertEquals(factory.getConfiguration(RhnConfiguration.KEYS.REPOSYNC_TIMEOUT).getDescription(),
                entry.description());

        factory.setValue(RhnConfiguration.KEYS.REPOSYNC_TIMEOUT, 600);
        entry = getEntry(RhnConfiguration.KEYS.REPOSYNC_TIMEOUT);
        assertEquals("600", entry.value());
        assertTrue(entry.modified());
    }

    @Test
    public void testListNormalizesBooleans() {
        // the default value is '0'
        factory.resetToDefault(RhnConfiguration.KEYS.REPOSYNC_NEVRA_FILTER);

        ServerSettingsEntry entry = getEntry(RhnConfiguration.KEYS.REPOSYNC_NEVRA_FILTER);
        assertEquals(RhnConfiguration.ValueType.BOOLEAN, entry.type());
        assertEquals("false", entry.value());
        assertEquals("false", entry.defaultValue());
    }

    @Test
    public void testUpdate() throws ValidatorException {
        service.update(Map.of(
                RhnConfiguration.KEYS.REPOSYNC_TIMEOUT.name(), " 600 ",
                RhnConfiguration.KEYS.SYNC_SOURCE_PACKAGES.name(), "true",
                RhnConfiguration.KEYS.NO_PROXY.name(), "example.com, example.org"));

        assertEquals(600, factory.getInteger(RhnConfiguration.KEYS.REPOSYNC_TIMEOUT));
        assertTrue(factory.getBoolean(RhnConfiguration.KEYS.SYNC_SOURCE_PACKAGES));
        assertEquals(List.of("example.com", "example.org"), factory.getList(RhnConfiguration.KEYS.NO_PROXY));
    }

    @Test
    public void testUpdateWithDefaultValueStoresNull() throws ValidatorException {
        factory.setValue(RhnConfiguration.KEYS.REPOSYNC_NEVRA_FILTER, true);
        factory.setValue(RhnConfiguration.KEYS.REPOSYNC_TIMEOUT, 600);

        // the default values are '0' and '300'
        service.update(Map.of(
                RhnConfiguration.KEYS.REPOSYNC_NEVRA_FILTER.name(), "false",
                RhnConfiguration.KEYS.REPOSYNC_TIMEOUT.name(), "300"));

        assertNull(factory.getConfiguration(RhnConfiguration.KEYS.REPOSYNC_NEVRA_FILTER).getValue());
        assertNull(factory.getConfiguration(RhnConfiguration.KEYS.REPOSYNC_TIMEOUT).getValue());
    }

    @Test
    public void testUpdateWithBlankValueResets() throws ValidatorException {
        factory.setValue(RhnConfiguration.KEYS.REPOSYNC_TIMEOUT, 600);

        service.update(Map.of(RhnConfiguration.KEYS.REPOSYNC_TIMEOUT.name(), " "));

        assertNull(factory.getConfiguration(RhnConfiguration.KEYS.REPOSYNC_TIMEOUT).getValue());
    }

    @Test
    public void testUpdateInvalidValuesChangesNothing() {
        factory.resetToDefault(RhnConfiguration.KEYS.REPOSYNC_MINRATE);
        factory.resetToDefault(RhnConfiguration.KEYS.REPOSYNC_TIMEOUT);
        factory.resetToDefault(RhnConfiguration.KEYS.SYNC_SOURCE_PACKAGES);

        ValidatorException e = assertThrows(ValidatorException.class, () -> service.update(Map.of(
                RhnConfiguration.KEYS.REPOSYNC_MINRATE.name(), "2000",
                RhnConfiguration.KEYS.REPOSYNC_TIMEOUT.name(), "ten",
                RhnConfiguration.KEYS.SYNC_SOURCE_PACKAGES.name(), "1")));

        assertEquals(2, e.getResult().getFieldErrors().size());
        assertTrue(e.getResult().getFieldErrors().containsKey(RhnConfiguration.KEYS.REPOSYNC_TIMEOUT.name()));
        assertTrue(e.getResult().getFieldErrors().containsKey(RhnConfiguration.KEYS.SYNC_SOURCE_PACKAGES.name()));
        assertNull(factory.getConfiguration(RhnConfiguration.KEYS.REPOSYNC_MINRATE).getValue());
        assertNull(factory.getConfiguration(RhnConfiguration.KEYS.REPOSYNC_TIMEOUT).getValue());
        assertNull(factory.getConfiguration(RhnConfiguration.KEYS.SYNC_SOURCE_PACKAGES).getValue());
    }

    @Test
    public void testUpdateHiddenKeyFails() {
        factory.resetToDefault(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN);

        ValidatorException e = assertThrows(ValidatorException.class, () ->
                service.update(Map.of(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN.name(), "1")));

        assertEquals("server_settings.error.hidden_key", e.getResult().getErrors().get(0).getKey());
        assertNull(factory.getConfiguration(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN).getValue());
    }

    @Test
    public void testUpdateUnknownKeyFails() {
        ValidatorException e = assertThrows(ValidatorException.class, () ->
                service.update(Map.of("NO_SUCH_KEY", "1")));

        assertEquals("server_settings.error.unknown_key", e.getResult().getErrors().get(0).getKey());
    }

    @Test
    public void testReset() throws ValidatorException {
        factory.setValue(RhnConfiguration.KEYS.REPOSYNC_TIMEOUT, 600);

        service.reset(RhnConfiguration.KEYS.REPOSYNC_TIMEOUT.name());

        assertNull(factory.getConfiguration(RhnConfiguration.KEYS.REPOSYNC_TIMEOUT).getValue());
    }

    @Test
    public void testResetHiddenKeyFails() {
        factory.setValue(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN, 6);

        assertThrows(ValidatorException.class, () ->
                service.reset(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN.name()));

        assertEquals(6, factory.getInteger(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN));
    }
}
