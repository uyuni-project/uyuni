/**
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.domain.action.virtualization.test;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.action.virtualization.BaseVirtualizationAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationDeleteAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationGuestPackageInstall;
import com.redhat.rhn.domain.action.virtualization.VirtualizationHostPackageInstall;
import com.redhat.rhn.domain.action.virtualization.VirtualizationRebootAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationResumeAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSetMemoryAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSetVcpusAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationShutdownAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationStartAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSuspendAction;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import java.util.HashMap;
import java.util.Map.Entry;

public class VirtualizationActionsTest extends BaseTestCaseWithUser {

    public void testPackageInstall() throws Exception {
        Action a1 = ActionFactoryTest.createAction(user,
                ActionFactory.TYPE_VIRTUALIZATION_GUEST_PACKAGE_INSTALL);

        flushAndEvict(a1);
        Long id = a1.getId();
        Action a = ActionFactory.lookupById(id);

        assertNotNull(a);
        assertTrue(a instanceof VirtualizationGuestPackageInstall);

        Action a2 = ActionFactoryTest.createAction(user,
                ActionFactory.TYPE_VIRTUALIZATION_HOST_PACKAGE_INSTALL);
        flushAndEvict(a2);
        id = a2.getId();

        a = ActionFactory.lookupById(id);
        assertNotNull(a);
        assertTrue(a instanceof VirtualizationHostPackageInstall);

    }

    public void testDomainLifecycleActions() throws Exception {
        HashMap<ActionType, Class<? extends BaseVirtualizationAction>> types = new HashMap<>();
        types.put(ActionFactory.TYPE_VIRTUALIZATION_DELETE, VirtualizationDeleteAction.class);
        types.put(ActionFactory.TYPE_VIRTUALIZATION_REBOOT, VirtualizationRebootAction.class);
        types.put(ActionFactory.TYPE_VIRTUALIZATION_RESUME, VirtualizationResumeAction.class);
        types.put(ActionFactory.TYPE_VIRTUALIZATION_SHUTDOWN, VirtualizationShutdownAction.class);
        types.put(ActionFactory.TYPE_VIRTUALIZATION_START, VirtualizationStartAction.class);
        types.put(ActionFactory.TYPE_VIRTUALIZATION_SUSPEND, VirtualizationSuspendAction.class);

        for (Entry<ActionType, Class<? extends BaseVirtualizationAction>> entry : types.entrySet()) {
            Action a = ActionFactoryTest.createAction(user, entry.getKey());
            flushAndEvict(a);

            Action a1 = ActionFactory.lookupById(a.getId());
            assertNotNull(a1);

            assertTrue(entry.getValue().isInstance(a1));
        }
    }

    public void testSetMemory() throws Exception {
        Action a = ActionFactoryTest.createAction(user, ActionFactory.TYPE_VIRTUALIZATION_SET_MEMORY);
        flushAndEvict(a);

        Action a1 = ActionFactory.lookupById(a.getId());
        assertNotNull(a1);

        VirtualizationSetMemoryAction va = (VirtualizationSetMemoryAction)a1;
        assertEquals(Integer.valueOf(1234), va.getMemory());
    }

    public void testSetVcpus() throws Exception {
        Action a = ActionFactoryTest.createAction(user, ActionFactory.TYPE_VIRTUALIZATION_SET_VCPUS);
        flushAndEvict(a);

        Action a1 = ActionFactory.lookupById(a.getId());
        assertNotNull(a1);

        VirtualizationSetVcpusAction va = (VirtualizationSetVcpusAction)a1;
        assertEquals(Integer.valueOf(12), va.getVcpu());
    }
}
