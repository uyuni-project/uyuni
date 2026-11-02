/*
 * Copyright (c) 2014--2026 SUSE LLC
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
package com.redhat.rhn.manager.setup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.domain.common.RhnConfiguration;
import com.redhat.rhn.domain.common.RhnConfigurationFactory;
import com.redhat.rhn.testing.BaseTestCase;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ProxySettingsManager}.
 */
public class ProxySettingsManagerTest extends BaseTestCase {

    private static final String TEST_HOSTNAME = "proxy.foobar.com";
    private static final String TEST_USERNAME = "foobaruser";
    private static final String TEST_PASSWD = "foobarpasswd";

    private final RhnConfigurationFactory factory = RhnConfigurationFactory.getSingleton();
    private ProxySettingsDto proxySettingsDto;

    @BeforeEach
    public void setupTest() {
        proxySettingsDto = new ProxySettingsDto();
        proxySettingsDto.setHostname(TEST_HOSTNAME);
        proxySettingsDto.setUsername(TEST_USERNAME);
        proxySettingsDto.setPassword(TEST_PASSWD);

        factory.resetToDefault(RhnConfiguration.KEYS.HTTP_PROXY);
        factory.resetToDefault(RhnConfiguration.KEYS.HTTP_PROXY_USERNAME);
        factory.resetToDefault(RhnConfiguration.KEYS.HTTP_PROXY_PASSWORD);
    }

    /**
     * Tests getProxySettings().
     */
    @Test
    public void testGetProxySettings() {
        factory.setValue(RhnConfiguration.KEYS.HTTP_PROXY, TEST_HOSTNAME);
        factory.setValue(RhnConfiguration.KEYS.HTTP_PROXY_USERNAME, TEST_USERNAME);
        factory.setValue(RhnConfiguration.KEYS.HTTP_PROXY_PASSWORD, TEST_PASSWD);

        ProxySettingsDto proxySettingsResult = ProxySettingsManager.getProxySettings();
        assertEquals(TEST_HOSTNAME, proxySettingsResult.getHostname());
        assertEquals(TEST_USERNAME, proxySettingsResult.getUsername());
        assertNull(proxySettingsResult.getPassword());
    }

    /**
     * Tests storeProxySettings().
     */
    @Test
    public void testStoreProxySettings() {
        ProxySettingsManager.storeProxySettings(proxySettingsDto, new RhnMockHttpServletRequest());

        assertEquals(TEST_HOSTNAME, factory.getString(RhnConfiguration.KEYS.HTTP_PROXY));
        assertEquals(TEST_USERNAME, factory.getString(RhnConfiguration.KEYS.HTTP_PROXY_USERNAME));
        assertEquals(TEST_PASSWD, factory.getString(RhnConfiguration.KEYS.HTTP_PROXY_PASSWORD));

        ProxySettingsDto emptySettings = new ProxySettingsDto();
        emptySettings.setHostname("");
        emptySettings.setUsername("");
        emptySettings.setPassword("");
        ProxySettingsManager.storeProxySettings(emptySettings, new RhnMockHttpServletRequest());

        assertNull(factory.getConfiguration(RhnConfiguration.KEYS.HTTP_PROXY).getValue());
        assertNull(factory.getConfiguration(RhnConfiguration.KEYS.HTTP_PROXY_USERNAME).getValue());
        assertNull(factory.getConfiguration(RhnConfiguration.KEYS.HTTP_PROXY_PASSWORD).getValue());
    }

    @Test
    public void testSanitizeInput() {
        // chars " and \ are accepted
        assertEquals("\"\\", ProxySettingsManager.sanitizeInput("\"\\"));

        // set of accepted printable chars
        assertEquals("!#$%&’()*+,-./:;<=>?@[]^_`{|}~",
                ProxySettingsManager.sanitizeInput("!#$%&’()*+,-./:;<=>?@[]^_`{|}~"));

        // standard ascii chars
        assertEquals("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ",
                ProxySettingsManager.sanitizeInput("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"));

        // remove tab and newline
        assertEquals("", ProxySettingsManager.sanitizeInput("\r\n\t"));

        //remove control chars
        StringBuilder builder = new StringBuilder();
        for (char ch = 0; ch < 32; ch++) {
            builder.append(ch);
        }
        builder.append((char) 127);
        assertEquals("", ProxySettingsManager.sanitizeInput(builder.toString()));

        assertEquals("Kind of string!",
                ProxySettingsManager.sanitizeInput("\n\nKind \n\n\n\tof string!\n\t"));
    }

    @Test
    public void testSanitizeSettingsInput() {

        ProxySettingsDto settings = new ProxySettingsDto();
        settings.setHostname("one\ntwo\nthree");
        settings.setUsername("@my+user^name%$\tone\ntwo");
        settings.setPassword("!#$%&’()pass\"word\\");

        ProxySettingsManager.sanitizeInput(settings);

        assertEquals("onetwothree", settings.getHostname());
        assertEquals("@my+user^name%$onetwo", settings.getUsername());
        assertEquals("!#$%&’()pass\"word\\", settings.getPassword());
    }
}
