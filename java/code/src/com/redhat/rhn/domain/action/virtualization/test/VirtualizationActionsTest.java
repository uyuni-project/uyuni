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
import com.redhat.rhn.domain.action.virtualization.VirtualizationCreateAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationCreateActionDiskDetails;
import com.redhat.rhn.domain.action.virtualization.VirtualizationCreateActionInterfaceDetails;
import com.redhat.rhn.domain.action.virtualization.VirtualizationDeleteAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationGuestPackageInstall;
import com.redhat.rhn.domain.action.virtualization.VirtualizationHostPackageInstall;
import com.redhat.rhn.domain.action.virtualization.VirtualizationPoolCreateAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationPoolCreateActionSource;
import com.redhat.rhn.domain.action.virtualization.VirtualizationRebootAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationResumeAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSetMemoryAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSetVcpusAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationShutdownAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationStartAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSuspendAction;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import com.suse.manager.virtualization.PoolSourceAuthentication;
import com.suse.manager.virtualization.PoolSourceDevice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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

    public void testDomainForceoff() throws Exception {
        Action a = ActionFactoryTest.createAction(user, ActionFactory.TYPE_VIRTUALIZATION_SHUTDOWN);
        VirtualizationShutdownAction va = (VirtualizationShutdownAction)a;
        va.setForce(true);
        flushAndEvict(va);

        Action a1 = ActionFactory.lookupById(a.getId());
        assertNotNull(a1);

        assertTrue(a1 instanceof VirtualizationShutdownAction);
        VirtualizationShutdownAction rebootAction = (VirtualizationShutdownAction)a1;
        assertTrue(rebootAction.isForce());
    }

    public void testDomainReset() throws Exception {
        Action a = ActionFactoryTest.createAction(user, ActionFactory.TYPE_VIRTUALIZATION_REBOOT);
        VirtualizationRebootAction va = (VirtualizationRebootAction)a;
        va.setForce(true);
        flushAndEvict(va);

        Action a1 = ActionFactory.lookupById(a.getId());
        assertNotNull(a1);

        assertTrue(a1 instanceof VirtualizationRebootAction);
        VirtualizationRebootAction rebootAction = (VirtualizationRebootAction)a1;
        assertTrue(rebootAction.isForce());
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

    /**
     * Test that virtualization creation actions are properly persisted and looked up
     *
     * @throws Exception something bad happened
     */
    public void testCreateLookup() throws Exception {
        VirtualizationCreateAction a1 = (VirtualizationCreateAction)ActionFactoryTest
                .createAction(user, ActionFactory.TYPE_VIRTUALIZATION_CREATE);
        a1.setType("kvm");
        a1.setName("guest0");
        a1.setArch("x86_64");
        a1.setMemory(1024L);
        a1.setVcpus(2L);
        a1.setOsType("hvm");

        List<VirtualizationCreateActionDiskDetails> disks = new ArrayList<>();
        VirtualizationCreateActionDiskDetails disk0 = new VirtualizationCreateActionDiskDetails();
        disk0.setTemplate("templateimage.qcow2");
        disk0.setBus("virtio");
        disk0.setAction(a1);
        disk0.setPool("default");
        disks.add(disk0);
        a1.setDisks(disks);

        List<String> nets = Arrays.asList("net0", "net1");
        a1.setInterfaces(
            nets.stream().map(net -> {
                VirtualizationCreateActionInterfaceDetails detail = new VirtualizationCreateActionInterfaceDetails();
                detail.setSource(net);
                detail.setAction(a1);
                return detail;
            }).collect(Collectors.toList()));

        flushAndEvict(a1);

        Action a = ActionFactory.lookupById(a1.getId());

        assertNotNull(a);
        assertTrue(a instanceof VirtualizationCreateAction);
        VirtualizationCreateAction actual = (VirtualizationCreateAction)a;
        assertEquals("kvm", actual.getType());
        assertEquals("guest0", actual.getName());
        assertEquals("x86_64", actual.getArch());
        assertEquals(Long.valueOf(1024), actual.getMemory());
        assertEquals(Long.valueOf(2), actual.getVcpus());
        assertEquals("hvm", actual.getOsType());

        assertEquals(1, actual.getDisks().size());
        assertEquals("templateimage.qcow2", actual.getDisks().get(0).getTemplate());
        assertEquals("virtio", actual.getDisks().get(0).getBus());
        assertEquals("default", actual.getDisks().get(0).getPool());
        assertEquals(2, actual.getInterfaces().size());
        assertEquals("net0", actual.getInterfaces().get(0).getSource());
        assertEquals("net1", actual.getInterfaces().get(1).getSource());
    }

    public void testPoolCreate() throws Exception {
        VirtualizationPoolCreateAction a1 = (VirtualizationPoolCreateAction)ActionFactoryTest
                .createAction(user, ActionFactory.TYPE_VIRTUALIZATION_POOL_CREATE);
        a1.setName("pool0");
        a1.setType("iscsi");
        a1.setTarget("/dev/disk/by-path");
        VirtualizationPoolCreateActionSource src = new VirtualizationPoolCreateActionSource();
        src.setAuth(new PoolSourceAuthentication("myuser", "mysecret"));
        src.setHosts(Arrays.asList("iscsi.example.com"));
        src.setDevices(Arrays.asList(new PoolSourceDevice("iqn.2013-06.com.example:iscsi-pool")));
        a1.setSource(src);
        a1.setMode("0744");
        a1.setOwner("107");
        a1.setGroup("108");
        a1.setSeclabel("virt_image_t");

        flushAndEvict(a1);

        Action a = ActionFactory.lookupById(a1.getId());

        assertNotNull(a);
        assertTrue(a instanceof VirtualizationPoolCreateAction);
        VirtualizationPoolCreateAction actual = (VirtualizationPoolCreateAction)a;
        assertEquals("pool0", actual.getName());
        assertEquals("iscsi", actual.getType());
        assertEquals("/dev/disk/by-path", actual.getTarget());
        assertEquals("0744", actual.getMode());
        assertEquals("107", actual.getOwner());
        assertEquals("108", actual.getGroup());
        assertEquals("virt_image_t", actual.getSeclabel());
        assertEquals("myuser", actual.getSource().getAuth().getUsername());
        assertEquals("mysecret", actual.getSource().getAuth().getPassword());
        assertEquals("iscsi.example.com", actual.getSource().getHosts().get(0));
        assertEquals("iqn.2013-06.com.example:iscsi-pool", actual.getSource().getDevices().get(0).getPath());
    }
}
