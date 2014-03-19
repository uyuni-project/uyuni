/**
 * Copyright (c) 2014 SUSE
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

package com.redhat.rhn.manager.setup.test;

import com.redhat.rhn.manager.setup.MirrorCredentials;
import com.redhat.rhn.testing.RhnBaseTestCase;

/**
 * Tests for {@link MirrorCredentials}.
 */
public class MirrorCredentialsTest extends RhnBaseTestCase {

    public void testEquality() throws Exception {
        MirrorCredentials mc1 = new MirrorCredentials("fake@domain.com", "user1", "pw1");
        MirrorCredentials mc2 = new MirrorCredentials("fake@domain.com", "user1", "pw1");
        assertEquals(mc1, mc2);

        MirrorCredentials mc3 = new MirrorCredentials("fake@domain.com", "user1", "pw1");
        MirrorCredentials mc4 = new MirrorCredentials("false@domain.com", "user1", "pw1");
        assertFalse(mc3.equals(mc4));
    }
}
