/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.preferences.locale.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.domain.user.RhnTimeZone;
import com.redhat.rhn.frontend.xmlrpc.InvalidLocaleCodeException;
import com.redhat.rhn.frontend.xmlrpc.InvalidTimeZoneException;
import com.redhat.rhn.frontend.xmlrpc.preferences.locale.PreferencesLocaleHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;

import org.junit.jupiter.api.Test;


public class PreferencesLocaleHandlerTest extends BaseHandlerTestCase {

    private PreferencesLocaleHandler handler = new PreferencesLocaleHandler();

    @Test
    public void testListTimeZone() {
        Object[] tzs = handler.listTimeZones();
        assertNotNull(tzs);
        assertTrue(tzs.length != 0, "TimeZone list is empty");
        assertEquals(RhnTimeZone.class, tzs[0].getClass());
    }

    @Test
    public void testSetTimeZoneInvalidId() {
        try {
            handler.setTimeZone(admin, admin.getLogin(), 0);
            fail("Expected an exception for timezoneid = 0");
        }
        catch (InvalidTimeZoneException itze) {
            // expected exception
        }
    }

    @Test
    public void testSetTimeZone() {
        Object[] tzs = handler.listTimeZones();
        assertNotNull(tzs);
        assertTrue(tzs.length != 0);
        RhnTimeZone tz = (RhnTimeZone)tzs[0];

        assertEquals(1,
           handler.setTimeZone(admin, admin.getLogin(), tz.getTimeZoneId()));

        RhnTimeZone usersTz = admin.getTimeZone();
        assertNotNull(usersTz);
        assertEquals(tz.getTimeZoneId(), usersTz.getTimeZoneId());
    }

    @Test
    public void testListLocales() {
        Object[] o = handler.listLocales();
        assertNotNull(o);
        String[] locales = Config.get().getStringArray("java.supported_locales");
        assertNotNull(locales);
        assertEquals(locales.length, o.length);
    }

    @Test
    public void testSetLocaleInvalidLocale() {
        try {
            handler.setLocale(admin, admin.getLogin(), "rd_NK");
            fail("rd_NK should be an invalid locale");
        }
        catch (InvalidLocaleCodeException ilce) {
            // expected exception
        }

        try {
            handler.setLocale(admin, admin.getLogin(), null);
            fail("null should be an invalid locale");
        }
        catch (InvalidLocaleCodeException ilce) {
            // expected exception
        }
    }

    @Test
    public void testSetLocale() {
        String l = admin.getPreferredLocale();
        assertNull(l);
        System.out.println(l);
        handler.setLocale(admin, admin.getLogin(), "en_US");
        assertEquals("en_US", admin.getPreferredLocale());
    }
}
