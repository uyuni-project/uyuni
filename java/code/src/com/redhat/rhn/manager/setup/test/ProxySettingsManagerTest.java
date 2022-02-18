/*
 * Copyright (c) 2014 SUSE LLC
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
package com.redhat.rhn.manager.setup.test;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.manager.setup.ProxySettingsDto;
import com.redhat.rhn.manager.setup.ProxySettingsManager;
import com.redhat.rhn.testing.RhnBaseTestCase;

/**
 * Tests for {@link ProxySettingsManager}.
 */
public class ProxySettingsManagerTest extends RhnBaseTestCase {

    /**
     * Tests getProxySettings().
     * @throws Exception if something goes wrong
     */
    public void testGetProxySettings() throws Exception {
        ProxySettingsDto proxy = new ProxySettingsDto();
        proxy.setHostname("proxy.foobar.com");
        proxy.setUsername("foobaruser");
        proxy.setPassword("foobarpassword");
        setProxySettings(proxy);
        assertTrue(proxy.equals(ProxySettingsManager.getProxySettings()));
    }

    /**
     * Sets the proxy settings.
     *
     * @param proxy the new proxy settings
     */
    public static void setProxySettings(ProxySettingsDto proxy) {
        Config.get().setString(ProxySettingsManager.KEY_PROXY_HOSTNAME, proxy.getHostname());
        Config.get().setString(ProxySettingsManager.KEY_PROXY_USERNAME, proxy.getUsername());
        Config.get().setString(ProxySettingsManager.KEY_PROXY_PASSWORD, proxy.getPassword());
    }
}
