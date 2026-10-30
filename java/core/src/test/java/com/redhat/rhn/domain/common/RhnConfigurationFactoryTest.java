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

package com.redhat.rhn.domain.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.testing.BaseTestCase;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RhnConfigurationFactoryTest extends BaseTestCase {

    private final RhnConfigurationFactory factory = RhnConfigurationFactory.getSingleton();

    @Test
    public void testEveryDatabaseKeyHasAnEnumConstant() {
        List<String> dbKeys = HibernateFactory.getSession()
                .createNativeQuery("SELECT key FROM rhnConfiguration", String.class)
                .stream().sorted().toList();
        List<String> enumKeys = Arrays.stream(RhnConfiguration.KEYS.values()).map(Enum::name).sorted().toList();

        assertEquals(enumKeys, dbKeys);
    }

    @Test
    public void testListAll() {
        List<RhnConfiguration.KEYS> keys = factory.listAll().stream().map(RhnConfiguration::getKey).toList();

        assertEquals(RhnConfiguration.KEYS.values().length, keys.size());
        assertTrue(keys.contains(RhnConfiguration.KEYS.URLGRABBER_LOGSPEC));
    }

    @Test
    public void testHiddenKeys() {
        // keys with their own page or API
        Set<RhnConfiguration.KEYS> expected = EnumSet.of(
                RhnConfiguration.KEYS.EXTAUTH_DEFAULT_ORGID,
                RhnConfiguration.KEYS.EXTAUTH_USE_ORGUNIT,
                RhnConfiguration.KEYS.EXTAUTH_KEEP_TEMPROLES,
                RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN,
                RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MAX,
                RhnConfiguration.KEYS.PSW_CHECK_LOWER_CHAR_FLAG,
                RhnConfiguration.KEYS.PSW_CHECK_UPPER_CHAR_FLAG,
                RhnConfiguration.KEYS.PSW_CHECK_DIGIT_FLAG,
                RhnConfiguration.KEYS.PSW_CHECK_CONSECUTIVE_CHAR_FLAG,
                RhnConfiguration.KEYS.PSW_CHECK_SPECIAL_CHAR_FLAG,
                RhnConfiguration.KEYS.PSW_CHECK_RESTRICTED_OCCURRENCE_FLAG,
                RhnConfiguration.KEYS.PSW_CHECK_MAX_OCCURRENCE,
                RhnConfiguration.KEYS.PSW_CHECK_SPECIAL_CHARACTERS,
                RhnConfiguration.KEYS.HTTP_PROXY,
                RhnConfiguration.KEYS.HTTP_PROXY_USERNAME,
                RhnConfiguration.KEYS.HTTP_PROXY_PASSWORD);
        Set<RhnConfiguration.KEYS> hidden = Arrays.stream(RhnConfiguration.KEYS.values())
                .filter(RhnConfiguration.KEYS::isHidden)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(RhnConfiguration.KEYS.class)));

        assertEquals(expected, hidden);
    }

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

    @Test
    public void testSetDefaultBooleanStoresNull() {
        // the default value is '0'
        factory.setValue(RhnConfiguration.KEYS.REPOSYNC_NEVRA_FILTER, false);
        assertNull(factory.getConfiguration(RhnConfiguration.KEYS.REPOSYNC_NEVRA_FILTER).getValue());

        factory.setValue(RhnConfiguration.KEYS.REPOSYNC_NEVRA_FILTER, true);
        assertTrue(factory.getBoolean(RhnConfiguration.KEYS.REPOSYNC_NEVRA_FILTER));
    }

    @Test
    public void testSetEmptyValueStoresNull() {
        factory.setValue(RhnConfiguration.KEYS.HTTP_PROXY, "proxy.example.com:3128");
        assertEquals("proxy.example.com:3128", factory.getString(RhnConfiguration.KEYS.HTTP_PROXY));

        factory.setValue(RhnConfiguration.KEYS.HTTP_PROXY, "");
        assertNull(factory.getConfiguration(RhnConfiguration.KEYS.HTTP_PROXY).getValue());
        assertNull(factory.getString(RhnConfiguration.KEYS.HTTP_PROXY));
    }

    @Test
    public void testSetBlankValueUsesDefault() {
        factory.setValue(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN, " ");
        assertNull(factory.getConfiguration(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN).getValue());
        assertEquals(4, factory.getInteger(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN));

        factory.getConfiguration(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN).setValue("");
        assertEquals(4, factory.getInteger(RhnConfiguration.KEYS.PSW_CHECK_LENGTH_MIN));
    }

    @Test
    public void testEmptyValue() {
        factory.resetToDefault(RhnConfiguration.KEYS.EXTAUTH_DEFAULT_ORGID);
        assertNull(factory.getLong(RhnConfiguration.KEYS.EXTAUTH_DEFAULT_ORGID));

        factory.setValue(RhnConfiguration.KEYS.EXTAUTH_DEFAULT_ORGID, 1L);
        assertEquals(1L, factory.getLong(RhnConfiguration.KEYS.EXTAUTH_DEFAULT_ORGID));
    }
}
