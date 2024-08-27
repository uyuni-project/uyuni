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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.server.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.virtualization.test.TestVirtManager;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.salt.netapi.calls.LocalCall;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * VirtualInstanceFactoryTest
 */
public class VirtualInstanceFactoryTest extends RhnBaseTestCase {

    private VirtualInstanceFactory virtualInstanceDAO;
    private User user;
    private GuestBuilder builder;
    private SystemEntitlementManager systemEntitlementManager;

    @Override
    @BeforeEach
    public void setUp() {
        virtualInstanceDAO = new VirtualInstanceFactory();
        user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        builder = new GuestBuilder(user);
        SaltApi saltApi = new TestSaltApi() {
            public void updateLibvirtEngine(MinionServer minion) {
            }
            public <R> Optional<R> callSync(LocalCall<R> call, String minionId) {
                return Optional.empty();
            }
       };
        ServerGroupManager serverGroupManager = new ServerGroupManager(saltApi);
        VirtManager virtManager = new TestVirtManager() {
            public void updateLibvirtEngine(MinionServer minion) {
            };
        };
        MonitoringManager monitoringManager = new FormulaMonitoringManager(saltApi);
        systemEntitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(virtManager, monitoringManager, serverGroupManager),
                new SystemEntitler(saltApi, virtManager, monitoringManager, serverGroupManager)
        );
    }

    private void assertGuestDeleted(VirtualInstance guest) {
        assertNull(virtualInstanceDAO.lookupById(guest.getId()));
    }

    private void assertGuestDeleted(VirtualInstance guest, Server guestSystem) {
        assertNull(ServerFactory.lookupById(guestSystem.getId()));
        assertGuestDeleted(guest);
    }

    private void flushAndEvictGuest(VirtualInstance guest) {
        Session session = VirtualInstanceFactory.getSession();

        flushAndEvict(guest);

        if (guest.isRegisteredGuest()) {
            session.evict(guest.getGuestSystem());
        }

        if (guest.getHostSystem() != null) {
            session.evict(guest.getHostSystem());
        }
    }

    @Test
    public void testSaveUnregisteredGuestAndLoadById() throws Exception {
        VirtualInstance guest = builder.createUnregisteredGuest()
                .withVirtHost().build();

        virtualInstanceDAO.saveVirtualInstance(guest);

        flushAndEvictGuest(guest);

        VirtualInstance retrievedVirtualInstance = virtualInstanceDAO
                .lookupById(guest.getId());

        assertEquals(guest, retrievedVirtualInstance);
    }

    @Test
    public void testSaveRegisteredGuestAndLoadById() throws Exception {
        VirtualInstance guest = builder.createGuest().withVirtHost().build();
        Server guestSystem = guest.getGuestSystem();

        virtualInstanceDAO.saveVirtualInstance(guest);

        flushAndEvict(guest);

        VirtualInstance retrievedGuest = virtualInstanceDAO.lookupById(guest
                .getId());

        assertEquals(guest, retrievedGuest);
        assertEquals(guestSystem, retrievedGuest.getGuestSystem());
    }

    @Test
    public void testGetGuestsAndNotHost() throws Exception {

        VirtualInstance vi = builder.createUnregisteredGuest()
            .withVirtHost().build();
        virtualInstanceDAO.saveVirtualInstance(vi);
        Long sid = vi.getHostSystem().getId();
        flushAndEvictGuest(vi);

        //step 2 - fetch the guest from the database so that it is attached to the session
        VirtualInstance retrievedGuest = virtualInstanceDAO.lookupById(vi
                .getId());

        assertNotNull(retrievedGuest.getHostSystem());

        Server s = ServerFactory.lookupById(sid);
        assertEquals(1, s.getGuests().size());

    }

    @Test
    public void testSaveAndRetrieveInfo() throws Exception {
        VirtualInstance guest = builder.createUnregisteredGuest()
                .withVirtHost().withName("the_virtual_one").asParaVirtGuest()
                .inStoppedState().build();

        virtualInstanceDAO.saveVirtualInstance(guest);
        flushAndEvict(guest);

        VirtualInstance retrievedGuest = virtualInstanceDAO.lookupById(guest
                .getId());

        assertEquals(guest.getName(), retrievedGuest.getName());
        assertEquals(guest.getType(), retrievedGuest.getType());
        assertEquals(guest.getState(), retrievedGuest.getState());
        assertEquals(guest.getTotalMemory(), retrievedGuest.getTotalMemory());
        assertEquals(guest.getNumberOfCPUs(), retrievedGuest.getNumberOfCPUs());
    }

    /*
     * Commeting out test for satellite.
    @Test
    public void testFindGuestsWithNonVirtHostByOrg() throws Exception {
        Set expectedViews = new HashSet<>();

        expectedViews.add(builder.createGuest().withNonVirtHost()
                .withPersistence().build().asGuestAndNonVirtHostView());
        expectedViews.add(builder.createGuest().withNonVirtHost()
                .withPersistence().build().asGuestAndNonVirtHostView());
        expectedViews.add(builder.createGuest().withNonVirtHostInAnotherOrg()
                .withPersistence().build().asGuestAndNonVirtHostView());
        expectedViews.add(builder.createGuest().withVirtHostInAnotherOrg()
                .withPersistence().build().asGuestAndNonVirtHostView());

        builder.createGuest().withVirtHost().withPersistence().build();
        builder.createGuest().withPersistence().build();

        Set actualViews = virtualInstanceDAO
                .findGuestsWithNonVirtHostByOrg(user.getOrg());

        assertTrue(CollectionUtils
                .isEqualCollection(expectedViews, actualViews));
    }*/


    @Test
    public void testFindGuestsWithoutAHostByOrg() throws Exception {
        Set expectedViews = new HashSet<>();

        expectedViews.add(builder.createGuest().withPersistence().build()
                .asGuestAndNonVirtHostView());
        expectedViews.add(builder.createGuest().withPersistence().build()
                .asGuestAndNonVirtHostView());

        builder.createGuest().withNonVirtHostInAnotherOrg().withPersistence().build();
        builder.createGuest().withNonVirtHost().withPersistence().build();
        builder.createGuest().withVirtHost().withPersistence().build();

        Set actualViews = virtualInstanceDAO.findGuestsWithoutAHostByOrg(user
                .getOrg());

        assertTrue(CollectionUtils
                .isEqualCollection(expectedViews, actualViews));
    }

    @Test
    public void testGetParaVirt() {
        assertEquals("Para-Virtualized", VirtualInstanceFactory.getInstance().
                getParaVirtType().getName());
        assertEquals("para_virtualized", VirtualInstanceFactory.getInstance().
                getParaVirtType().getLabel());
    }

    @Test
    public void testFullyVirt() {
        assertEquals("Fully Virtualized", VirtualInstanceFactory.getInstance().
                getFullyVirtType().getName());
        assertEquals("fully_virtualized", VirtualInstanceFactory.getInstance().
                getFullyVirtType().getLabel());
    }

    @Test
    public void testGetRunning() {
        assertEquals("running", VirtualInstanceFactory.getInstance()
                .getRunningState().getLabel());
        assertEquals("Running", VirtualInstanceFactory.getInstance()
                .getRunningState().getName());
    }

    @Test
    public void testGetStopped() {
        assertEquals("stopped", VirtualInstanceFactory.getInstance()
                .getStoppedState().getLabel());
        assertEquals("Stopped", VirtualInstanceFactory.getInstance()
                .getStoppedState().getName());
    }

    @Test
    public void testGetCrashed() {
        assertEquals("crashed", VirtualInstanceFactory.getInstance()
                .getCrashedState().getLabel());
        assertEquals("Crashed", VirtualInstanceFactory.getInstance()
                .getCrashedState().getName());
    }

    @Test
    public void testGetPaused() {
        assertEquals("paused", VirtualInstanceFactory.getInstance()
                .getPausedState().getLabel());
        assertEquals("Paused", VirtualInstanceFactory.getInstance()
                .getPausedState().getName());
    }

    @Test
    public void testGetUnknown() {
        assertEquals("unknown", VirtualInstanceFactory.getInstance()
                .getUnknownState().getLabel());
        assertEquals("Unknown", VirtualInstanceFactory.getInstance()
                .getUnknownState().getName());
    }

    @Test
    public void testSetState() throws Exception {
        Server host = ServerTestUtils.createVirtHostWithGuest(systemEntitlementManager);
        VirtualInstance vi = host.getGuests().iterator().next();
        vi.setState(VirtualInstanceFactory.getInstance().getRunningState());
        TestUtils.saveAndFlush(vi);
        assertNotNull(vi.getState());
    }

    @Test
    public void testLookupGuestByUuid() throws Exception {
        Server host = ServerTestUtils.createVirtHostWithGuest(systemEntitlementManager);
        VirtualInstance guest = host.getGuests().iterator().next();

        List<VirtualInstance> virtualInstances = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid(guest.getUuid());
        VirtualInstance fromDb = virtualInstances.iterator().next();

        assertEquals(1, virtualInstances.size());
        assertEquals(guest, fromDb);
    }

    @Test
    public void testLookupHostVirtualInstanceByHostId() throws Exception {
        Server host = ServerTestUtils.createVirtHostWithGuest(systemEntitlementManager);

        VirtualInstance fromDb = HibernateFactory.getSession()
                .createQuery("from VirtualInstance where hostSystem = :host and guestSystem IS NULL",
                        VirtualInstance.class)
                .setParameter("host", host)
                .uniqueResult();

        VirtualInstance hostVirtInstance = VirtualInstanceFactory.getInstance()
                .lookupHostVirtInstanceByHostId(host.getId());

        assertEquals(fromDb, hostVirtInstance);
    }

    @Test
    public void testLookupGuestByHostIdAndUuid() throws Exception {
        Server host = ServerTestUtils.createVirtHostWithGuest(systemEntitlementManager);
        VirtualInstance guest = host.getGuests().iterator().next();

        VirtualInstance fromDb = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByHostIdAndUuid(host.getId(), guest.getUuid());

        assertEquals(guest, fromDb);
    }
}
