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
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.channel;

import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

/**
 * Tests for the {@link AppStream} mapping.
 */
public class AppStreamTest extends BaseTestCaseWithUser {

    /**
     * Deleting an AppStream cascades to its API entries, as when a CLM build replaces
     * the modules of a target channel (gh#uyuni-project/uyuni#12892).
     */
    @Test
    public void testDeleteAppStreamWithApis() throws Exception {
        var session = HibernateFactory.getSession();
        AppStream appStream = new AppStream();
        appStream.setName("perl");
        appStream.setStream("5.26");
        appStream.setVersion("8000020190628143946");
        appStream.setContext("ee766497");
        appStream.setArch("x86_64");
        appStream.setChannel(ChannelFactoryTest.createTestChannel(user));
        session.persist(appStream);

        AppStreamApi api = new AppStreamApi("perl(strict)", appStream.getId());
        api.setAppStream(appStream);
        appStream.setRpms(new HashSet<>());
        appStream.getRpms().add(api);
        session.persist(api);
        session.flush();

        session.remove(appStream);
        session.flush();
        session.clear();

        assertNull(session.find(AppStream.class, appStream.getId()));
    }
}
