/*
 * Copyright (c) 2016--2021 SUSE LLC
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
package com.redhat.rhn.domain.matcher.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.matcher.MatcherRunData;
import com.redhat.rhn.domain.matcher.MatcherRunDataFactory;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.Test;

/**
 * Tests the MatcherRunDataFactory.
 */
public class MatcherRunDataFactoryTest extends RhnBaseTestCase {

    /**
     * Tests updating the MatcherRunData and fetching it back from the db.
     */
    @Test
    public void testUpdateAndGet() {
        MatcherRunData data = new MatcherRunData();
        data.setInput("input");
        data.setOutput("output");
        data.setMessageReport("message");
        data.setSubscriptionReport("subs");
        data.setUnmatchedProductReport("unmatched");

        MatcherRunDataFactory.updateData(data);

        MatcherRunData fromDb = MatcherRunDataFactory.getSingle();
        assertEquals("input", fromDb.getInput());
        assertEquals("output", fromDb.getOutput());
        assertEquals("message", fromDb.getMessageReport());
        assertEquals("subs", fromDb.getSubscriptionReport());
        assertEquals("unmatched", fromDb.getUnmatchedProductReport());
    }

    /**
     * Tests that two subsequent updates don't produce multiple values in the db.
     */
    @Test
    public void testMultiUpdate() {
        MatcherRunDataFactory.updateData(new MatcherRunData());
        HibernateFactory.getSession().flush();
        MatcherRunDataFactory.updateData(new MatcherRunData());
        HibernateFactory.getSession().flush();

        // if multiple results, then exception would be thrown here:
        MatcherRunDataFactory.getSingle();
    }

    /**
     * Tests retrieval when the data hasn't been stored yet. In this case we assume
     * <code>null</code>.
     */
    @Test
    public void testEmpty() {
        assertNull(MatcherRunDataFactory.getSingle());
    }
}
