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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.domain.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.testing.BaseTestCase;

import org.junit.jupiter.api.Test;

public class RhnConfigurationFactoryTest extends BaseTestCase {

    private final RhnConfigurationFactory factory = RhnConfigurationFactory.getSingleton();

    @Test
    public void testFallbackToDefault() {
        factory.resetToDefault(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN);

        assertNull(factory.getConfiguration(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN).getValue());
        assertEquals(factory.getDefaultInteger(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN),
                factory.getInteger(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN));
    }

    @Test
    public void testSetAndReset() {
        int defaultValue = factory.getDefaultInteger(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN);

        factory.setValue(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN, defaultValue + 1);
        assertEquals(defaultValue + 1, factory.getInteger(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN));
        assertEquals(defaultValue, factory.getDefaultInteger(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN));

        factory.resetToDefault(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN);
        assertNull(factory.getConfiguration(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN).getValue());
        assertEquals(defaultValue, factory.getInteger(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN));
    }

    @Test
    public void testSetDefaultValueStoresNull() {
        factory.setValue(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN,
                factory.getDefaultString(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN));

        assertNull(factory.getConfiguration(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN).getValue());
    }

    @Test
    public void testBoolean() {
        factory.setValue(RhnConfiguration.KEYS.EXTAUTH_USE_ORGUNIT, true);
        assertTrue(factory.getBoolean(RhnConfiguration.KEYS.EXTAUTH_USE_ORGUNIT));

        factory.setValue(RhnConfiguration.KEYS.EXTAUTH_USE_ORGUNIT, "1");
        assertTrue(factory.getBoolean(RhnConfiguration.KEYS.EXTAUTH_USE_ORGUNIT));

        factory.resetToDefault(RhnConfiguration.KEYS.EXTAUTH_USE_ORGUNIT);
        assertFalse(factory.getBoolean(RhnConfiguration.KEYS.EXTAUTH_USE_ORGUNIT));
    }

    /*
     * disable as long as we have no boolean value to test with
    @Test
    public void testSetDefaultBooleanStoresNull() {
        // the default value is '0'
        factory.setValue(RhnConfiguration.KEYS.DISCONNECTED, false);
        assertNull(factory.getConfiguration(RhnConfiguration.KEYS.DISCONNECTED).getValue());

        factory.setValue(RhnConfiguration.KEYS.DISCONNECTED, true);
        assertTrue(factory.getBoolean(RhnConfiguration.KEYS.DISCONNECTED));
    }
    */

    @Test
    public void testSetEmptyValueStoresNull() {
        factory.setValue(RhnConfiguration.KEYS.HTTP_PROXY, "proxy.example.com:3128");
        assertEquals("proxy.example.com:3128", factory.getString(RhnConfiguration.KEYS.HTTP_PROXY));

        factory.setValue(RhnConfiguration.KEYS.HTTP_PROXY, "");
        assertNull(factory.getConfiguration(RhnConfiguration.KEYS.HTTP_PROXY).getValue());
        assertNull(factory.getString(RhnConfiguration.KEYS.HTTP_PROXY));
    }

    @Test
    public void testEmptyValue() {
        factory.resetToDefault(RhnConfiguration.KEYS.EXTAUTH_DEFAULT_ORGID);
        assertNull(factory.getLong(RhnConfiguration.KEYS.EXTAUTH_DEFAULT_ORGID));

        factory.setValue(RhnConfiguration.KEYS.EXTAUTH_DEFAULT_ORGID, 1L);
        assertEquals(1L, factory.getLong(RhnConfiguration.KEYS.EXTAUTH_DEFAULT_ORGID));
    }
}
