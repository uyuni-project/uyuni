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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.action.virtualization.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.action.virtualization.BaseVirtualizationGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationCreateGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationDeleteGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationGuestPackageInstall;
import com.redhat.rhn.domain.action.virtualization.VirtualizationHostPackageInstall;
import com.redhat.rhn.domain.action.virtualization.VirtualizationPoolCreateAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationPoolCreateActionSource;
import com.redhat.rhn.domain.action.virtualization.VirtualizationRebootGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationResumeGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSetMemoryGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSetVcpusGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationShutdownGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationStartGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSuspendGuestAction;
import com.redhat.rhn.frontend.context.Context;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import com.suse.manager.virtualization.GuestCreateDetails;
import com.suse.manager.virtualization.PoolSourceAuthentication;
import com.suse.manager.virtualization.PoolSourceDevice;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualGuestsUpdateActionJson;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class VirtualizationActionsTest extends BaseTestCaseWithUser {

    @Test
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

    @Test
    public void testDomainLifecycleActions() throws Exception {
        HashMap<ActionType, Class<? extends BaseVirtualizationGuestAction>> types = new HashMap<>();
        types.put(ActionFactory.TYPE_VIRTUALIZATION_DELETE, VirtualizationDeleteGuestAction.class);
        types.put(ActionFactory.TYPE_VIRTUALIZATION_REBOOT, VirtualizationRebootGuestAction.class);
        types.put(ActionFactory.TYPE_VIRTUALIZATION_RESUME, VirtualizationResumeGuestAction.class);
        types.put(ActionFactory.TYPE_VIRTUALIZATION_SHUTDOWN, VirtualizationShutdownGuestAction.class);
        types.put(ActionFactory.TYPE_VIRTUALIZATION_START, VirtualizationStartGuestAction.class);
        types.put(ActionFactory.TYPE_VIRTUALIZATION_SUSPEND, VirtualizationSuspendGuestAction.class);

        for (Entry<ActionType, Class<? extends BaseVirtualizationGuestAction>> entry : types.entrySet()) {
            Action a = ActionFactoryTest.createAction(user, entry.getKey());
            flushAndEvict(a);

            Action a1 = ActionFactory.lookupById(a.getId());
            assertNotNull(a1);

            assertTrue(entry.getValue().isInstance(a1));
        }
    }

    @Test
    public void testDomainForceoff() throws Exception {
        Action a = ActionFactoryTest.createAction(user, ActionFactory.TYPE_VIRTUALIZATION_SHUTDOWN);
        VirtualizationShutdownGuestAction va = (VirtualizationShutdownGuestAction)a;
        va.setForce(true);
        flushAndEvict(va);

        Action a1 = ActionFactory.lookupById(a.getId());
        assertNotNull(a1);

        assertTrue(a1 instanceof VirtualizationShutdownGuestAction);
        VirtualizationShutdownGuestAction rebootAction = (VirtualizationShutdownGuestAction)a1;
        assertTrue(rebootAction.isForce());
    }

    @Test
    public void testDomainReset() throws Exception {
        Action a = ActionFactoryTest.createAction(user, ActionFactory.TYPE_VIRTUALIZATION_REBOOT);
        VirtualizationRebootGuestAction va = (VirtualizationRebootGuestAction)a;
        va.setForce(true);
        flushAndEvict(va);

        Action a1 = ActionFactory.lookupById(a.getId());
        assertNotNull(a1);

        assertTrue(a1 instanceof VirtualizationRebootGuestAction);
        VirtualizationRebootGuestAction rebootAction = (VirtualizationRebootGuestAction)a1;
        assertTrue(rebootAction.isForce());
    }

    @Test
    public void testSetMemory() throws Exception {
        Action a = ActionFactoryTest.createAction(user, ActionFactory.TYPE_VIRTUALIZATION_SET_MEMORY);
        flushAndEvict(a);

        Action a1 = ActionFactory.lookupById(a.getId());
        assertNotNull(a1);

        VirtualizationSetMemoryGuestAction va = (VirtualizationSetMemoryGuestAction)a1;
        assertEquals(Integer.valueOf(1234), va.getMemory());
    }

    @Test
    public void testSetVcpus() throws Exception {
        Action a = ActionFactoryTest.createAction(user, ActionFactory.TYPE_VIRTUALIZATION_SET_VCPUS);
        flushAndEvict(a);

        Action a1 = ActionFactory.lookupById(a.getId());
        assertNotNull(a1);

        VirtualizationSetVcpusGuestAction va = (VirtualizationSetVcpusGuestAction)a1;
        assertEquals(Integer.valueOf(12), va.getVcpu());
    }

    /**
     * Test that virtualization creation actions are properly persisted and looked up
     *
     * @throws Exception something bad happened
     */
    @Test
    public void testCreateLookup() throws Exception {
        Context.getCurrentContext().setTimezone(TimeZone.getDefault());

        VirtualizationCreateGuestAction a1 = (VirtualizationCreateGuestAction)ActionFactoryTest
                .createAction(user, ActionFactory.TYPE_VIRTUALIZATION_CREATE);
        a1.setDetails(new GuestCreateDetails());
        a1.getDetails().setType("kvm");
        a1.getDetails().setName("guest0");
        a1.getDetails().setArch("x86_64");
        a1.getDetails().setMemory(1024L);
        a1.getDetails().setVcpu(2L);
        a1.getDetails().setOsType("hvm");
        a1.getDetails().setKernelOptions("kernelopts");
        a1.getDetails().setCobblerSystem("cobbler:system:id");
        a1.getDetails().setKickstartHost("https://cobbler.host.local");

        List<VirtualGuestsUpdateActionJson.DiskData> disks = new ArrayList<>();
        VirtualGuestsUpdateActionJson.DiskData disk0 = a1.getDetails().new DiskData();
        disk0.setTemplate("templateimage.qcow2");
        disk0.setBus("virtio");
        disk0.setPool("default");
        disks.add(disk0);
        a1.getDetails().setDisks(disks);

        List<String> nets = Arrays.asList("net0", "net1");
        a1.getDetails().setInterfaces(
            nets.stream().map(net -> {
                VirtualGuestsUpdateActionJson.InterfaceData detail = a1.getDetails().new InterfaceData();
                detail.setSource(net);
                return detail;
            }).collect(Collectors.toList()));

        flushAndEvict(a1);

        Action a = ActionFactory.lookupById(a1.getId());

        assertNotNull(a);
        assertTrue(a instanceof VirtualizationCreateGuestAction);
        VirtualizationCreateGuestAction actual = (VirtualizationCreateGuestAction)a;
        assertEquals("kvm", actual.getDetails().getType());
        assertEquals("guest0", actual.getDetails().getName());
        assertEquals("x86_64", actual.getDetails().getArch());
        assertEquals(Long.valueOf(1024), actual.getDetails().getMemory());
        assertEquals(Long.valueOf(2), actual.getDetails().getVcpu());
        assertEquals("hvm", actual.getDetails().getOsType());
        assertEquals("kernelopts", actual.getDetails().getKernelOptions());
        assertEquals("cobbler:system:id", actual.getDetails().getCobblerSystem());
        assertEquals("https://cobbler.host.local", actual.getDetails().getKickstartHost());

        assertEquals(1, actual.getDetails().getDisks().size());
        assertEquals("templateimage.qcow2", actual.getDetails().getDisks().get(0).getTemplate());
        assertEquals("virtio", actual.getDetails().getDisks().get(0).getBus());
        assertEquals("default", actual.getDetails().getDisks().get(0).getPool());
        assertEquals(2, actual.getDetails().getInterfaces().size());
        assertEquals("net0", actual.getDetails().getInterfaces().get(0).getSource());
        assertEquals("net1", actual.getDetails().getInterfaces().get(1).getSource());
    }

    @Test
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
