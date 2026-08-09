/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.ldap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

/**
 * Unit tests for Active Directory {@code ;range=} option parsing.
 */
public class LdapRangedAttributesTest {

    @Test
    public void parsesOpenEndedTerminalRange() {
        Optional<LdapRangedAttributes.Range> range =
                LdapRangedAttributes.parseRange(Set.of("range=1500-*"));

        assertTrue(range.isPresent());
        assertTrue(range.get().complete());
        assertEquals(1500, range.get().start());
    }

    @Test
    public void parsesPartialRangePage() {
        Optional<LdapRangedAttributes.Range> range =
                LdapRangedAttributes.parseRange(Set.of("RANGE=0-1499"));

        assertTrue(range.isPresent());
        assertFalse(range.get().complete());
        assertEquals(0, range.get().start());
        assertEquals(1499, range.get().end());
    }

    @Test
    public void ignoresAttributesWithoutRangeOption() {
        assertTrue(LdapRangedAttributes.parseRange(Set.of("binary")).isEmpty());
        assertTrue(LdapRangedAttributes.parseRange(Set.of()).isEmpty());
    }
}
