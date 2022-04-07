/*
 * Copyright (c) 2021 SUSE LLC
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
package com.suse.utils.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.suse.utils.Opt;

import org.junit.jupiter.api.Test;

import java.util.Optional;

public class OptTest {

    @Test
    public void testWrapFirstNonNull() {
        assertEquals(Optional.of("test"), Opt.wrapFirstNonNull(null, "test", null));
        assertEquals(Optional.of("this"), Opt.wrapFirstNonNull("this", "is", "a", "test"));
        assertEquals(Optional.empty(), Opt.wrapFirstNonNull());
        assertEquals(Optional.empty(), Opt.wrapFirstNonNull(null, null, null, null));
    }
}

