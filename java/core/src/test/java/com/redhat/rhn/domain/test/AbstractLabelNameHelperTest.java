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
package com.redhat.rhn.domain.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.redhat.rhn.domain.AbstractLabelNameHelper;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.Test;

/**
 * AbstractLabelNameHelperTest
 */
public class AbstractLabelNameHelperTest extends RhnBaseTestCase {

    @Test
    public void testEquals() {
        AbstractLabelNameHelper h1 = new AbstractLabelNameHelper();
        AbstractLabelNameHelper h2 = null;

        h1.setLabel("foo");
        h1.setName("bar");
        h1.setId(1L);

        assertNotEquals(h1, h2);

        h2 = new AbstractLabelNameHelper();
        h2.setLabel("bar");
        h2.setName("foo");
        h2.setId(2L);
        assertNotEquals(h1, h2);

        h2.setLabel("foo");
        h2.setName("bar");
        h2.setId(null);
        assertNotEquals(h1, h2);

        h2.setId(1L);
        assertEquals(h1, h2);
        assertEquals(h1, h1);
    }
}
