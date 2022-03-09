/**
 * Copyright (c) 2013 SUSE LLC
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
package com.redhat.rhn.manager.audit.test;

import com.redhat.rhn.manager.audit.CVEAuditSystemBuilder;
import junit.framework.TestCase;

import com.redhat.rhn.manager.audit.CVEAuditSystem;
import com.redhat.rhn.manager.audit.ChannelIdNameLabelTriple;
import com.redhat.rhn.manager.audit.ErrataIdAdvisoryPair;

/**
 * Tests for {@link CVEAuditSystem}.
 *
 */
public class CVEAuditSystemTest extends TestCase {

    /**
     * Make sure that the set of channels is working as expected.
     */
    public void testChannels() {
        CVEAuditSystemBuilder system = new CVEAuditSystemBuilder(0L);
        system.addChannel(new ChannelIdNameLabelTriple(1L, "foo", "f", 0L));
        system.addChannel(new ChannelIdNameLabelTriple(1L, "bar", "b", 0L));
        assertEquals(1, system.getChannels().size());
        assertTrue(system.getChannels().contains(
                new ChannelIdNameLabelTriple(1L, "xyz", "x", 0L)));

        system.addChannel(new ChannelIdNameLabelTriple(2L, "foo", "f", 0L));
        assertEquals(2, system.getChannels().size());
        assertTrue(system.getChannels().contains(
                new ChannelIdNameLabelTriple(2L, "xyz", "x", 0L)));
    }

    /**
     * Make sure that the set of erratas is working as expected.
     */
    public void testErratas() {
        CVEAuditSystemBuilder system = new CVEAuditSystemBuilder(0L);
        system.addErrata(new ErrataIdAdvisoryPair(1L, "foo"));
        system.addErrata(new ErrataIdAdvisoryPair(1L, "bar"));
        assertEquals(1, system.getErratas().size());
        assertTrue(system.getErratas().contains(new ErrataIdAdvisoryPair(1L, "xyz")));

        system.addErrata(new ErrataIdAdvisoryPair(2L, "foo"));
        assertEquals(2, system.getErratas().size());
        assertTrue(system.getErratas().contains(new ErrataIdAdvisoryPair(2L, "xyz")));
    }
}
