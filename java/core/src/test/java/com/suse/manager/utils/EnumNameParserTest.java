/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.manager.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.redhat.rhn.manager.audit.PatchStatus;

import com.suse.oval.OvalSeverity;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class EnumNameParserTest {

    @Test
    public void testParseIgnoringInvalidNames() {
        List<String> invalid = new ArrayList<>();
        assertEquals(EnumSet.of(OvalSeverity.CRITICAL, OvalSeverity.UNKNOWN),
                EnumNameParser.parse(List.of("CRITICAL", "UNKNOWN", "bogus"), OvalSeverity.class,
                        e -> invalid.add(e.getMessage())));
        assertEquals(1, invalid.size());
    }

    @Test
    public void testParseNullIsEmpty() {
        assertEquals(EnumSet.noneOf(PatchStatus.class), EnumNameParser.parse(null, PatchStatus.class, e -> { }));
    }

    @Test
    public void testParseRejectingInvalidNames() {
        assertThrows(IllegalStateException.class, () ->
                EnumNameParser.parse(List.of("PATCHED", "bogus"), PatchStatus.class, e -> {
                    throw new IllegalStateException(e);
                }));
    }
}
