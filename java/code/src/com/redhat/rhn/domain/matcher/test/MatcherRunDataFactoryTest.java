package com.redhat.rhn.domain.matcher.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.matcher.MatcherRunData;
import com.redhat.rhn.domain.matcher.MatcherRunDataFactory;
import com.redhat.rhn.testing.RhnBaseTestCase;

/**
 * Tests the MatcherRunDataFactory.
 */
public class MatcherRunDataFactoryTest extends RhnBaseTestCase {

    /**
     * Tests updating the MatcherRunData and fetching it back from the db.
     */
    public void testUpdateAndGet() {
        MatcherRunData data = new MatcherRunData();
        data.setInput("input");
        data.setOutput("output");
        data.setMessageReport("message");
        data.setSubscriptionReport("subs");
        data.setUnmatchedSystemReport("unmatched");

        MatcherRunDataFactory.updateData(data);

        MatcherRunData fromDb = MatcherRunDataFactory.getSingle();
        assertEquals("input", fromDb.getInput());
        assertEquals("output", fromDb.getOutput());
        assertEquals("message", fromDb.getMessageReport());
        assertEquals("subs", fromDb.getSubscriptionReport());
        assertEquals("unmatched", fromDb.getUnmatchedSystemReport());
    }

    /**
     * Tests that two subsequent updates don't produce multiple values in the db.
     */
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
    public void testEmpty() {
        assertNull(MatcherRunDataFactory.getSingle());
    }
}
