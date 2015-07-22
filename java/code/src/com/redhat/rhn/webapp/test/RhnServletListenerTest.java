/**
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
package com.redhat.rhn.webapp.test;

import com.redhat.rhn.webapp.RhnServletListener;
import junit.framework.TestCase;

/**
 * RhnServletListenerTest
 * @version $Rev$
 */
public class RhnServletListenerTest extends TestCase {

    public void testListenerStartup() {
        RhnServletListener rl = new RhnServletListener();
        // Test startup
        rl.contextInitialized(null);
        assertTrue(rl.hibernateStarted());
        assertTrue(rl.loggingStarted());
        assertTrue(rl.loggingStarted());

        // Test teardown
        rl.contextDestroyed(null);
        assertFalse(rl.hibernateStarted());
        assertFalse(rl.loggingStarted());
        assertFalse(rl.loggingStarted());

    }
}
