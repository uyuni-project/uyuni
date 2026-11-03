/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.manager.kickstart;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.testing.BaseTestCase;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;

/**
 * Tests the kickstart host FQDN resolution (step 1.5): a provided FQDN is
 * canonicalized, null/empty falls back to the configured Java hostname
 * unchanged, unresolvable input is returned unchanged.
 */
public class KickstartHostHelperTest extends BaseTestCase {

    /**
     * A provided FQDN is canonicalized.
     */
    @Test
    public void testProvidedFqdnIsCanonicalized() throws Exception {
        String expected = InetAddress.getByName("127.0.0.1").getCanonicalHostName();
        assertEquals(expected, KickstartHostHelper.getServerFqdn("127.0.0.1"));
    }

    /**
     * Null falls back to the configured Java hostname unchanged (not
     * canonicalized - it must stay the external FQDN, step-1.5 decision).
     */
    @Test
    public void testNullFallsBackToConfiguredHostname() {
        assertEquals("localhost", KickstartHostHelper.getServerFqdn(null));
        assertEquals(ConfigDefaults.get().getJavaHostname(),
                KickstartHostHelper.getServerFqdn(null));
    }

    /**
     * Empty input behaves like null.
     */
    @Test
    public void testEmptyFallsBackToConfiguredHostname() {
        assertEquals(ConfigDefaults.get().getJavaHostname(),
                KickstartHostHelper.getServerFqdn(""));
    }

    /**
     * An unresolvable host is returned unchanged.
     */
    @Test
    public void testUnresolvableHostReturnedUnchanged() {
        String unresolvable = "nonexistent-host-xy.invalid";
        assertEquals(unresolvable, KickstartHostHelper.getServerFqdn(unresolvable));
    }
}
