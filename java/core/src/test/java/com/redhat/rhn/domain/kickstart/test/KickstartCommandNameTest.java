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
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.kickstart.KickstartCommandName;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import org.hibernate.Session;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * KickstartCommandNameTest
 */
public class KickstartCommandNameTest extends BaseTestCaseWithUser {

    @Test
    public void testCommandName() {

        Session session = HibernateFactory.getSession();
        List<KickstartCommandName> l1 = session.createQuery(
                """
                    FROM KickstartCommandName AS t
                    WHERE t.name NOT IN
                    ('partitions', 'raids', 'logvols', 'volgroups', 'include', 'repo', 'custom', 'custom_partition')
                    ORDER BY t.order
                    """, KickstartCommandName.class)
                .setCacheable(true)
                .list();

        assertFalse(l1.isEmpty());

        KickstartCommandName c = l1.get(0);
        assertEquals(c.getOrder(), Long.valueOf(1));

        List<KickstartCommandName> l2 = KickstartFactory.lookupKickstartCommandNames();
        assertEquals(l1.size(), l2.size() + 2);
    }

}
