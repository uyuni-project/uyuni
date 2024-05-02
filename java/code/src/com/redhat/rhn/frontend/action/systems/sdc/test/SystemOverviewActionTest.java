/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.systems.sdc.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.RhnMockStrutsTestCase;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.test.TestSaltApi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 * SystemOverviewActionTest
 */
public class SystemOverviewActionTest extends RhnMockStrutsTestCase {

    protected Server s;
    private final SaltApi saltApi = new TestSaltApi();
    private final ServerGroupManager serverGroupManager = new ServerGroupManager(saltApi);
    private final VirtManager virtManager = new VirtManagerSalt(saltApi);
    private final MonitoringManager monitoringManager = new FormulaMonitoringManager(saltApi);
    private final SystemEntitlementManager systemEntitlementManager = new SystemEntitlementManager(
            new SystemUnentitler(virtManager, monitoringManager, serverGroupManager),
            new SystemEntitler(saltApi, virtManager, monitoringManager, serverGroupManager)
    );

    /**
     * {@inheritDoc}
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setRequestPathInfo("/systems/details/Overview");

        s = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());

        request.addParameter("sid", s.getId().toString());
    }

    @Test
    public void testSystemStatusNoErrata() {
        actionPerform();
        assertEquals(Boolean.FALSE, request.getAttribute("hasUpdates"));
    }

    @Test
    public void testSystemStatusWithErrata() throws Exception {
        Errata e = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        e.setAdvisoryType(ErrataFactory.ERRATA_TYPE_SECURITY);

        Org org = user.getOrg();
        Package p = PackageTest.createTestPackage(org);

        UserFactory.save(user);
        OrgFactory.save(org);

        int rows = ErrataCacheManager.insertNeededErrataCache(
                s.getId(), e.getId(), p.getId());
        assertEquals(1, rows);

        actionPerform();
        assertEquals(Boolean.TRUE, request.getAttribute("hasUpdates"));
    }

    @Test
    public void testSystemInactive() {
        s.getServerInfo().setCheckin(new Date(1));
        TestUtils.saveAndFlush(s);
        actionPerform();
        assertEquals(request.getAttribute("systemInactive"), Boolean.TRUE);
    }

    @Test
    public void testSystemActive() {
        Calendar pcal = Calendar.getInstance();
        pcal.setTime(new Timestamp(System.currentTimeMillis()));
        pcal.roll(Calendar.MINUTE, -5);

        s.getServerInfo().setCheckin(pcal.getTime());
        TestUtils.saveAndFlush(s);
        actionPerform();
        assertEquals(request.getAttribute("systemInactive"), Boolean.FALSE);
    }

    @Test
    public void testSystemUnentitled() {
       systemEntitlementManager.removeAllServerEntitlements(s);
       actionPerform();
       assertEquals(request.getAttribute("unentitled"), Boolean.TRUE);
    }

    @Test
    public void testSystemEntitled() {
        actionPerform();
        assertEquals(request.getAttribute("unentitled"), Boolean.FALSE);
    }

    @Test
    public void testLockSystem() {
        request.addParameter("lock", "1");
        actionPerform();
        verifyActionMessage("sdc.details.overview.locked.alert");
        assertNotNull(s.getLock());
    }

    @Test
    public void testUnlockSystem() {
        SystemManager.lockServer(user, s, "test reason");
        request.addParameter("lock", "0");
        actionPerform();
        verifyActionMessage("sdc.details.overview.unlocked.alert");
        assertNull(s.getLock());
    }

    @Test
    public void testActivateSatelliteApplet() {

        request.addParameter("applet", "1");
        actionPerform();
        verifyActionMessage("sdc.details.overview.applet.scheduled");
    }

    @Test
    public void testLivePatchVersion() throws Exception {
        String kernelLiveVersion = "kgraft_patch_2_1_1";
        MinionServer m = MinionServerFactoryTest.createTestMinionServer(user);
        m.setKernelLiveVersion(kernelLiveVersion);
        TestUtils.saveAndFlush(m);

        request.addParameter("sid", m.getId().toString());
        actionPerform();

        assertEquals(kernelLiveVersion, request.getAttribute("kernelLiveVersion"));
    }
}
