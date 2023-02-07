/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.domain.rhnset.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.domain.rhnset.RhnSetElement;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.Test;

/**
 * RhnSetElementTest
 */
public class RhnSetElementTest extends RhnBaseTestCase {

    @Test
    public void testDefaultCtor() {
        RhnSetElement rse = new RhnSetElement();
        assertNotNull(rse);
        assertNull(rse.getUserId());
        assertNull(rse.getLabel());
        assertNull(rse.getElement());
        assertNull(rse.getElementTwo());
    }

    @Test
    public void testArgCtorWithNulls() {
        RhnSetElement rse = new RhnSetElement(null, null, null, null);
        assertNotNull(rse);
        assertNull(rse.getUserId());
        assertNull(rse.getLabel());
        assertNull(rse.getElement());
        assertNull(rse.getElementTwo());
    }

    @Test
    public void testTwoArgCtor() {
        Long id = 10L;
        Long elem = 400L;
        String label = "label foo";
        RhnSetElement rse = new RhnSetElement(id, label, elem, elem);
        assertNotNull(rse);
        assertEquals(id, rse.getUserId());
        assertEquals(label, rse.getLabel());
        assertEquals(elem, rse.getElement());
        assertEquals(elem, rse.getElementTwo());
    }

    @Test
    public void testThreeArgCtor() {
        Long id = 10L;
        Long elem = 400L;
        String label = "label foo";
        RhnSetElement rse = new RhnSetElement(id, label, elem, elem, elem);
        assertNotNull(rse);
        assertEquals(id, rse.getUserId());
        assertEquals(label, rse.getLabel());
        assertEquals(elem, rse.getElement());
        assertEquals(elem, rse.getElementTwo());
        assertEquals(elem, rse.getElementThree());
    }

    @Test
    public void testBeanProperties() {
        Long id = 10L;
        Long elem = 400L;
        String label = "label foo";
        RhnSetElement rse = new RhnSetElement();
        assertNotNull(rse);
        rse.setUserId(id);
        assertEquals(id, rse.getUserId());

        rse.setLabel(label);
        assertEquals(label, rse.getLabel());

        rse.setElement(elem);
        assertEquals(elem, rse.getElement());

        rse.setElement(null);
        assertNull(rse.getElement());

        rse.setElementTwo(elem);
        assertEquals(elem, rse.getElementTwo());

        rse.setElementTwo(null);
        assertNull(rse.getElementTwo());

        rse.setElementThree(elem);
        assertEquals(elem, rse.getElementThree());

        rse.setElementThree(null);
        assertNull(rse.getElementThree());
    }

    @Test
    public void testEquals() {
        Long uid = 42L;
        Long elem = 3131L;
        Long elemTwo = 3132L;
        Long elemThree = 3133L;
        String label = "testEquals label";

        RhnSetElement r1 = new RhnSetElement();
        RhnSetElement r2 = new RhnSetElement();
        assertEquals(r1, r2);

        r1.setUserId(uid);
        r2.setUserId(uid);
        assertEquals(r1, r2);
        r1.setLabel(label);
        r2.setLabel(label);
        assertEquals(r1, r2);
        r1.setElement(elem);
        r2.setElement(elem);
        assertEquals(r1, r2);

        r1.setElementTwo(elemTwo);
        r2.setElementTwo(elemTwo);
        assertEquals(r1, r2);
        r2.setElementTwo(elem);
        assertNotEquals(r1, r2);
        r2.setElementTwo(null);
        assertNotEquals(r2, r1);
        assertNotEquals(r1, r2);
        r1.setElementTwo(null);
        assertEquals(r1, r2);

        r1.setElementThree(elemThree);
        r2.setElementThree(elemThree);
        assertEquals(r1, r2);
        r2.setElementThree(elem);
        assertNotEquals(r1, r2);
        r2.setElementThree(null);
        assertNotEquals(r2, r1);
        assertNotEquals(r1, r2);
        r1.setElementThree(null);
        assertEquals(r1, r2);

    }

    @Test
    public void testStringConstructor() {
        Long uid = 42L;
        String label = "testEquals label";
        Long elem = 3131L;
        Long elemTwo = 3132L;
        RhnSetElement r1 = new RhnSetElement(uid, label,
                                                    elem + "|" + elemTwo);
        RhnSetElement r2 = new RhnSetElement(uid, label,
                                                    elem, elemTwo);
        assertEquals(r1, r2);

        Long elemThree = 3133L;
        r1 = new RhnSetElement(uid, label, elem + "|" + elemTwo + "|" + elemThree);
        r2 = new RhnSetElement(uid, label, elem, elemTwo, elemThree);

        assertEquals(r1, r2);
    }
}
