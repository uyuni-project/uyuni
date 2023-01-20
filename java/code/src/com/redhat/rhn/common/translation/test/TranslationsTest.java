/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.common.translation.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.translation.TranslationException;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class TranslationsTest extends RhnBaseTestCase {
    @Test
    public void testNoTranslator() {
        try {
            // Try a translation that should be impossible.  This should make
            // sure that nobody ever writes a translator to do this.
            TestTranslations.convert("hmmm", List.class);
            fail("Shouldn't be able to translate from String to list");
        }
        catch (TranslationException e) {
            // Expected exception, shouldn't have a cause.
            assertNull(e.getCause());
        }
    }

    @Test
    public void testlongDateTranslation() {

        long current = System.currentTimeMillis();
        Date translated = (Date)TestTranslations.convert(current,
                                                   Date.class);
        assertEquals(new Date(current), translated);
    }

    @Test
    public void testFailedTranslation() {
        try {
            TestTranslations.convert("hmmm", java.lang.Integer.class);
            fail("Translation should have failed");
        }
        catch (TranslationException e) {
            assertEquals(e.getCause().getClass(),
                         java.lang.NumberFormatException.class);
        }
    }

    @Test
    public void testPrivateTranslator() {
        try {
            TestTranslations.convert(1, java.lang.Long.class);
            fail("Translation should have failed");
        }
        catch (TranslationException e) {
            assertNull(e.getCause());
        }
    }

    @Test
    public void testListToString() {
        List list = new ArrayList<>();
        list.add(10);
        list.add("list");
        String s = (String) TestTranslations.convert(list, String.class);
        assertNotNull(s);
        assertEquals("[10, list]", s);

        list = new LinkedList<>();
        list.add(20);
        list.add("list");
        s = (String) TestTranslations.convert(list, String.class);
        assertNotNull(s);
        assertEquals("[20, list]", s);
    }
}
