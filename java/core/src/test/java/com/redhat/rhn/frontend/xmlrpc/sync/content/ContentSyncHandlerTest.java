/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.frontend.xmlrpc.sync.content;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.frontend.xmlrpc.BaseHandlerTestCase;
import com.redhat.rhn.frontend.xmlrpc.ContentSyncException;

import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.IssHub;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ContentSyncHandlerTest extends BaseHandlerTestCase {

    private final ContentSyncHandler handler = new ContentSyncHandler();

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        admin.addPermanentRole(RoleFactory.SAT_ADMIN);
    }

    @Test
    void denyApiOnPeripheral() {
        HubFactory hubFactory = new HubFactory();
        IssHub hub = new IssHub("hub.domain.top", null);
        hubFactory.save(hub);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        Assertions.assertThrows(ContentSyncException.class, () -> handler.addChannel(admin, "dummy", null));
        Assertions.assertThrows(ContentSyncException.class, () -> handler.addChannels(admin, "dummy", null));
        Assertions.assertThrows(ContentSyncException.class, () ->
                handler.addCredentials(admin, "dummy-user", "dummy-passwd", false));
        Assertions.assertThrows(ContentSyncException.class, () -> handler.deleteCredentials(admin, "dummy-user"));
    }
}
