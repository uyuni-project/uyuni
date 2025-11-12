/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.testing;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.EntitlementServerGroup;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.ServerGroupType;
import com.redhat.rhn.domain.server.test.ServerGroupTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.ServerGroupManager;

import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.services.iface.SaltApi;


/**
 * ServerGroupTestUtils
 */
public class ServerGroupTestUtils {

    private ServerGroupTestUtils() {
    }


    public static final String NAME = "ManagedGroup";
    public static final String DESCRIPTION = "User Managed Group ";

    /**
     * Create Entitled Server group (Update, Management, Provis, etc..)
     * @param org to own
     * @param typeIn you want
     * @return EntitlementServerGroup created
     */
    public static EntitlementServerGroup createEntitled(Org org, ServerGroupType typeIn) {
        return (EntitlementServerGroup) ServerGroupTest.createTestServerGroup(org, typeIn);
    }

    /**
     * Create a ManagedServerGroup ( a group of servers )
     * @param user to own
     * @return ManagedServerGroup created
     */
    public static ManagedServerGroup createManaged(User user) {
        SaltStateGeneratorService.INSTANCE.setSkipSetOwner(true);
        ServerGroupTest.checkSysGroupAdminRole(user);
        return GlobalInstanceHolder.SERVER_GROUP_MANAGER.
                                        create(user, NAME + TestUtils.randomString(),
                                                    DESCRIPTION);
    }

    /**
     * Remove ManagedServerGroup
     * @param user owning user
     * @param group group to be removed
     * @param saltApi the mocked Salt Api
     */
    public static void removeManaged(User user, ManagedServerGroup group, SaltApi saltApi) {
        ServerGroupTest.checkSysGroupAdminRole(user);
        ServerGroupManager groupManager = new ServerGroupManager(saltApi);
        groupManager.remove(user, group);
    }
}
