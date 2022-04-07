/*
 * Copyright (c) 2014 SUSE LLC
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.redhat.rhn.manager.setup.MirrorCredentialsDto;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link MirrorCredentialsDto}.
 */
public class MirrorCredentialsTest extends RhnBaseTestCase {

    /**
     * Tests MirrorCredentialsDto.equals()
     */
    @Test
    public void testEquality() {
        MirrorCredentialsDto mc1 = new MirrorCredentialsDto("user1", "pw1");
        MirrorCredentialsDto mc2 = new MirrorCredentialsDto("user1", "pw1");
        assertEquals(mc1, mc2);

        MirrorCredentialsDto mc3 = new MirrorCredentialsDto("user1", "pw1");
        MirrorCredentialsDto mc4 = new MirrorCredentialsDto("user2", "pw1");
        assertFalse(mc3.equals(mc4));

        MirrorCredentialsDto mc5 = new MirrorCredentialsDto("user1", "pw1");
        MirrorCredentialsDto mc6 = new MirrorCredentialsDto("user1", "pw2");
        assertFalse(mc5.equals(mc6));
    }
}
