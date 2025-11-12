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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.action.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionStatus;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.Test;

/**
 * ActionStatusTest
 */
public class ActionStatusTest extends RhnBaseTestCase {

    /**
     * Test Equals
     */
    @Test
    public void testEquals() {
        ActionStatus s1 = new ActionStatus();
        ActionStatus s2 = null;
        assertNotEquals(s1, s2);
        s1 = ActionFactory.STATUS_QUEUED;
        s2 = ActionFactory.STATUS_QUEUED;
        assertEquals(s1, s2);
        ActionStatus s3 = ActionFactory.STATUS_FAILED;
        assertNotEquals(s1, s3);
        assertEquals(s1, s1);
    }

    /**
     * Test findByName query
     * This method can be used to test the
     * second level cache in hibernate. Turn on sql output
     * in the hibernate.properties file and make sure that
     * we're not going to the db twice
     */
    @Test
    public void testFindByLabel() {
        ActionStatus r1 = ActionFactory.STATUS_COMPLETED;
        ActionStatus r2 = ActionFactory.STATUS_COMPLETED;
        assertEquals(r2.getName(), r1.getName());
    }

}
