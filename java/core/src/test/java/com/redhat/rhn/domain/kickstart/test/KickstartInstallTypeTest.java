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
package com.redhat.rhn.domain.kickstart.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.domain.kickstart.KickstartInstallType;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

/**
 * KickstartInstallTypeTest
 */
public class KickstartInstallTypeTest extends RhnBaseTestCase {

    @Test
    public void testKsInstallType() {
        Long testid = 1L;

        KickstartInstallType kit1 = TestUtils.lookupFromCacheById(testid, KickstartInstallType.class);
        assertNotNull(kit1);
        assertEquals(kit1.getId(), testid);

        KickstartInstallType kit2 = TestUtils.lookupFromCacheById(testid, KickstartInstallType.class);
        assertEquals(kit1.getLabel(), kit2.getLabel());
    }

}
