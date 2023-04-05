/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.systems.virtualization.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartSession;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.EntitlementServerGroup;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.token.Token;
import com.redhat.rhn.domain.token.TokenFactory;
import com.redhat.rhn.frontend.action.kickstart.ScheduleKickstartWizardAction;
import com.redhat.rhn.frontend.action.kickstart.test.ScheduleKickstartWizardTest;
import com.redhat.rhn.frontend.action.systems.virtualization.ProvisionVirtualizationWizardAction;
import com.redhat.rhn.frontend.dto.ProfileDto;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.kickstart.KickstartScheduleCommand;
import com.redhat.rhn.manager.profile.test.ProfileManagerTest;
import com.redhat.rhn.manager.rhnpackage.test.PackageManagerTest;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.RhnMockStrutsTestCase;
import com.redhat.rhn.testing.ServerGroupTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.test.TestSaltApi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;


/**
 * ProvisionVirtualizationWizardActionTest
 */
public class ProvisionVirtualizationWizardActionTest extends RhnMockStrutsTestCase {

    private Server s;
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
        setRequestPathInfo("/systems/details/virtualization/ProvisionVirtualizationWizard");
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        s = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
        s.addChannel(ChannelFactoryTest.createBaseChannel(user));
        EntitlementServerGroup sg = ServerGroupTestUtils.createEntitled(user.getOrg(),
                ServerFactory.lookupServerGroupTypeByLabel("enterprise_entitled"));
        systemEntitlementManager.addEntitlementToServer(s, sg.getGroupType().getAssociatedEntitlement());
        Channel c = ChannelFactoryTest.createTestChannel(user);
        // Required so the Server has a base channel
        // otherwise we cant ks.
        s.addChannel(c);

        PackageManagerTest.addPackageToSystemAndChannel(
                ConfigDefaults.get().getKickstartPackageNames().get(0), s, c);
        TestUtils.saveAndFlush(s);
        TestUtils.saveAndFlush(c);

        PackageManagerTest.
            addUp2dateToSystemAndChannel(user, s,
                    KickstartScheduleCommand.UP2DATE_VERSION,  c);

        TestUtils.flushAndEvict(s);
        TestUtils.flushAndEvict(c);
        addRequestParameter(RequestContext.SID, s.getId().toString());
    }

    @Test
    public void testStepOne() {
        actionPerform();
        verifyNoActionErrors();
    }

    @Test
    public void testStepTwo() throws Exception {
        KickstartData k = KickstartDataTest.createKickstartWithProfile(user);
        ProfileManagerTest.createProfileWithServer(user);

        ActivationKey key = ActivationKeyFactory.createNewKey(user, "some key");
        ActivationKeyFactory.save(key);
        key = TestUtils.reload(key);
        Token t = TokenFactory.lookupById(key.getId());
        Set tokens = new HashSet<>();
        tokens.add(t);
        k.setDefaultRegTokens(tokens);


        // Step Two
        addRequestParameter(RequestContext.SID, s.getId().toString());
        addRequestParameter("wizardStep", "second");
        addRequestParameter("items_selected", k.getCobblerId().toString());
        addRequestParameter("scheduleAsap", "false");
        addRequestParameter("date_month", "2");
        addRequestParameter("date_day", "16");
        addRequestParameter("date_year", "2006");
        addRequestParameter("date_hour", "8");
        addRequestParameter("date_minute", "0");
        addRequestParameter("date_am_pm", "1");
        addRequestParameter(ProvisionVirtualizationWizardAction.GUEST_NAME, "testName");
        addRequestParameter(RequestContext.COBBLER_ID, k.getCobblerId());
        actionPerform();
        verifyNoActionErrors();
        assertNotNull(request.getAttribute(RequestContext.KICKSTART));
        assertNotNull(request.getAttribute(RequestContext.SYSTEM));
        verifyFormList(ScheduleKickstartWizardAction.SYNCH_PACKAGES,
                ProfileDto.class);
        verifyFormList(ScheduleKickstartWizardAction.SYNCH_SYSTEMS,
                Row.class);

    }

    public void executeStepThree(boolean addProxy) throws Exception {
        // Perform step 1
        actionPerform();
        verifyNoActionErrors();
        try {
            verifyActionMessage("kickstart.schedule.noprofiles");
        }
        catch (AssertionError e) {
            verifyActionMessages(new String[] {"kickstart.schedule.noprofiles"});
        }
        assertNotNull(request.getAttribute(RequestContext.SYSTEM));
        clearRequestParameters();

        // Perform step2
        KickstartData k = KickstartDataTest.createKickstartWithProfile(user);
        // Required so the server and profile match base channels
        k.getKickstartDefaults().getKstree().setChannel(s.getBaseChannel());

        // Create other server to sync
        Server otherServer = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
        otherServer.addChannel(ChannelFactoryTest.createTestChannel(user));

        addRequestParameter(RequestContext.SID, s.getId().toString());
        addRequestParameter("targetProfileType",
                KickstartScheduleCommand.TARGET_PROFILE_TYPE_SYSTEM);
        addRequestParameter("targetServerProfile", otherServer.getId().toString());
        addRequestParameter("wizardStep", "third");
        addRequestParameter("items_selected", k.getCobblerId().toString());
        addRequestParameter("scheduleAsap", "false");
        addRequestParameter("date_month", "2");
        addRequestParameter("date_day", "16");
        addRequestParameter("date_year", "2006");
        addRequestParameter("date_hour", "8");
        addRequestParameter("date_minute", "0");
        addRequestParameter("date_am_pm", "1");
        addRequestParameter(ProvisionVirtualizationWizardAction.GUEST_NAME, "testName");
        addRequestParameter(RequestContext.COBBLER_ID, k.getCobblerId());
        Server proxy = null;
        if (addProxy) {
            proxy = ScheduleKickstartWizardTest.addProxy(user, s);
            assertNotNull(proxy.getHostname());
            /** Assign a proxy host, this would be the case
             * When user selects a proxy entry from the proxies combo
             */
            addRequestParameter(ScheduleKickstartWizardAction.PROXY_HOST,
                                                        proxy.getId().toString());
        }

        actionPerform();
        verifyNoActionErrors();

        verifyActionMessage("kickstart.schedule.success");
        assertEquals(getActualForward(),
                "/systems/details/kickstart/SessionStatus.do?sid=" + s.getId());

        assertNotNull(KickstartFactory.lookupKickstartSessionByServer(s.getId()));
        if (addProxy && proxy != null) {
            verifyFormValue(ScheduleKickstartWizardAction.PROXY_HOST,
                    proxy.getId().toString());
            KickstartSession session = KickstartFactory.
                            lookupKickstartSessionByServer(s.getId());
            assertNotNull(session.getSystemRhnHost());
            assertEquals(proxy.getHostname(), session.getSystemRhnHost());
        }
    }


    @Test
    public void testStepThreeWithProxy() throws Exception {
         executeStepThree(true);
    }

    @Test
    public void testStepThreeNoProxy() throws Exception {
        executeStepThree(false);
    }
}
