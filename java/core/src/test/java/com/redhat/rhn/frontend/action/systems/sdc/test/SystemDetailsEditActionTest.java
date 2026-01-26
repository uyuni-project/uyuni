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
package com.redhat.rhn.frontend.action.systems.sdc.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.entitlement.VirtualizationEntitlement;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.frontend.action.systems.sdc.SystemDetailsEditAction;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.RhnPostMockStrutsTestCase;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.manager.webui.services.test.TestSystemQuery;

import org.apache.struts.util.LabelValueBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * SystemDetailsEditActionTest
 */
public class SystemDetailsEditActionTest extends RhnPostMockStrutsTestCase {

    protected Server s;
    private final SystemQuery systemQuery = new TestSystemQuery();
    private final SaltApi saltApi = new TestSaltApi();
    private final SystemEntitlementManager systemEntitlementManager = new SystemEntitlementManager(
            new SystemUnentitler(saltApi), new SystemEntitler(saltApi)
    );

    /**
     * {@inheritDoc}
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setRequestPathInfo("/systems/details/Edit");
        TestUtils.saveAndFlush(user.getOrg());

        // mocking JSP is hard, so let's test with traditional
        s = ServerTestUtils.createTestSystem(user, ServerConstants.getServerGroupTypeEnterpriseEntitled());
        ChannelTestUtils.setupBaseChannelForVirtualization(user, s.getBaseChannel());

        UserTestUtils.addVirtualization(user.getOrg());
        TestUtils.saveAndFlush(user.getOrg());

        request.addParameter("sid", s.getId().toString());
    }

    @Test
    public void testBasicFormSubmission() {
        request.addParameter(SystemDetailsEditAction.NAME, "Augustus");
        request.addParameter(SystemDetailsEditAction.DESCRIPTION, "First Emperor");
        request.addParameter(SystemDetailsEditAction.ADDRESS_ONE, "Palatine Hill");
        request.addParameter(SystemDetailsEditAction.ADDRESS_TWO, "Forum Romanum");
        request.addParameter(SystemDetailsEditAction.CITY, "Rome");
        request.addParameter(SystemDetailsEditAction.COUNTRY, "it");
        request.addParameter(SystemDetailsEditAction.BUILDING, "Imperial Palace");
        request.addParameter(SystemDetailsEditAction.ROOM, "Imperial Throne Room");
        request.addParameter(SystemDetailsEditAction.RACK, "Imperial PC Rack");
        request.addParameter(RhnAction.SUBMITTED, Boolean.TRUE.toString());
        actionPerform();
        s = TestUtils.reload(s);
        verifyActionMessage("sdc.details.edit.propertieschanged");
        verifyForwardPath("/systems/details/Overview.do?sid=" + s.getId());
        assertEquals("Augustus", s.getName());
        assertEquals("First Emperor", s.getDescription());
        assertEquals("Palatine Hill", s.getLocation().getAddress1());
        assertEquals("Forum Romanum", s.getLocation().getAddress2());
        assertEquals("Rome", s.getLocation().getCity());
        assertEquals("it", s.getLocation().getCountry());
        assertEquals("Imperial Palace", s.getLocation().getBuilding());
        assertEquals("Imperial Throne Room", s.getLocation().getRoom());
        assertEquals("Imperial PC Rack", s.getLocation().getRack());
    }

    @Test
    public void testInvalidFormSubmission() {
        String originalName = s.getName();
        request.addParameter(SystemDetailsEditAction.NAME, "ha");
        request.addParameter(RhnAction.SUBMITTED, Boolean.TRUE.toString());
        actionPerform();
        verifyForward("error");
        /* Verifying nothing changed */
        assertEquals(originalName, s.getName());
    }

    @Test
    public void testBaseEntitlementListForEntitledSystem() {
        actionPerform();
        verifyForward(RhnHelper.DEFAULT_FORWARD);
        List options = (List) request
                              .getAttribute(SystemDetailsEditAction
                                            .BASE_ENTITLEMENT_OPTIONS);

        boolean unentitledValueFound = false;

        for (Object optionIn : options) {
            LabelValueBean bean = (LabelValueBean) optionIn;

            if (bean.getValue().equals("unentitle")) {
                unentitledValueFound = true;
            }
        }

        assertTrue(unentitledValueFound);
    }

    @Test
    public void testAddonEntitlemntsList() {
        actionPerform();
        Object addonsAtt =
                request.getAttribute(SystemDetailsEditAction.ADDON_ENTITLEMENTS);
        assertNotNull(addonsAtt);
        Set<Entitlement> addons = (Set<Entitlement>) addonsAtt;
        assertFalse(addons.isEmpty());
    }

    @Test
    public void testBaseEntitlementListForUnetitledSystem() {
        systemEntitlementManager.removeAllServerEntitlements(s);
        TestUtils.saveAndFlush(s);
        actionPerform();
        verifyForward(RhnHelper.DEFAULT_FORWARD);
        List options = (List) request
                              .getAttribute(SystemDetailsEditAction
                                            .BASE_ENTITLEMENT_OPTIONS);

        boolean unentitledValueFound = false;

        for (Object optionIn : options) {
            LabelValueBean bean = (LabelValueBean) optionIn;

            if (bean.getValue().equals("none")) {
                unentitledValueFound = true;
            }
        }

        assertTrue(unentitledValueFound);
    }

    @Test
    public void testAddEntitlement() {
        //add the base entitlement to the request to make sure we can
        // process both base and addon.  See BZ 229448
        request.addParameter(SystemDetailsEditAction.BASE_ENTITLEMENT,
                EntitlementManager.MANAGEMENT.getLabel());

        addRequestParameter(EntitlementManager.VIRTUALIZATION_ENTITLED,
                Boolean.TRUE.toString());
        request.addParameter(SystemDetailsEditAction.NAME, s.getName());

        addSubmitted();
        actionPerform();
        s = TestUtils.reload(s);
        assertTrue(s.getAddOnEntitlements().contains(EntitlementManager.VIRTUALIZATION));
    }

    @Test
    public void testSetBaseEntitlement() throws Exception {
        UserTestUtils.addManagement(user.getOrg());
        Long id = s.getId();
        String name = s.getName();
        systemEntitlementManager.removeAllServerEntitlements(s);
        s = ServerFactory.lookupById(id);
        assertNull(s.getBaseEntitlement());

        request.addParameter(SystemDetailsEditAction.NAME, name);
        request.addParameter(SystemDetailsEditAction.BASE_ENTITLEMENT,
                EntitlementManager.MANAGEMENT.getLabel());
        addSubmitted();
        actionPerform();
        s = ServerFactory.lookupById(id);
        assertEquals(s.getBaseEntitlement(), EntitlementManager.MANAGEMENT);
    }

    @Test
    public void testUnentitle() {
        request.addParameter(SystemDetailsEditAction.NAME, s.getName());
        assertEquals(s.getBaseEntitlement(), EntitlementManager.MANAGEMENT);
        request.addParameter(SystemDetailsEditAction.BASE_ENTITLEMENT,
                "unentitle");
        addSubmitted();
        actionPerform();
        s = TestUtils.reload(s);

        Assertions.assertNull(s.getBaseEntitlement(), "we shouldnt have a base entitlement");
    }


    @Test
    public void testCheckboxesTrue() {
        Iterator i = s.getValidAddonEntitlementsForServer().iterator();

        while (i.hasNext()) {
            Entitlement e = (Entitlement) i.next();
            assertFalse(s.hasEntitlement(e));
            request.addParameter(e.getLabel(), Boolean.TRUE.toString());
            System.out.println("added: " + e.getLabel());
        }

        s.setAutoUpdate("N");
        request.addParameter(SystemDetailsEditAction.AUTO_UPDATE,
                             Boolean.TRUE.toString());
        request.addParameter(SystemDetailsEditAction.NAME, s.getName());
        request.addParameter(RhnAction.SUBMITTED, Boolean.TRUE.toString());
        TestUtils.saveAndFlush(s);
        actionPerform();
        verifyForwardPath("/systems/details/Overview.do?sid=" + s.getId());
        /* here we look the server back up since entitling a server involves
         * a stored procedure call
         */
        TestUtils.flushAndEvict(s);
        s = ServerFactory.lookupById(s.getId());
        i = s.getValidAddonEntitlementsForServer().iterator();

        while (i.hasNext()) {
            Entitlement e = (Entitlement) i.next();
            if (!(e instanceof VirtualizationEntitlement)) {
                Assertions.assertTrue(s.hasEntitlement(e), "Didnt find entitlement in server: " + e.getLabel());
            }
        }
        assertTrue(s.hasEntitlement(EntitlementManager.VIRTUALIZATION));

        assertEquals("Y", s.getAutoUpdate());
    }

    @Test
    public void testCheckboxesFalse() {
        Iterator i = s.getValidAddonEntitlementsForServer().iterator();

        while (i.hasNext()) {
            Entitlement e = (Entitlement) i.next();
            systemEntitlementManager.addEntitlementToServer(s, e);
            TestUtils.flushAndEvict(s);
            s = ServerFactory.lookupById(s.getId());
            request.addParameter(e.getLabel(), Boolean.FALSE.toString());
        }

        s.setAutoUpdate("Y");
        request .addParameter(SystemDetailsEditAction.AUTO_UPDATE,
                              Boolean.FALSE.toString());
        request.addParameter(SystemDetailsEditAction.NAME, s.getName());
        request.addParameter(RhnAction.SUBMITTED, Boolean.TRUE.toString());
        TestUtils.saveAndFlush(s);
        actionPerform();
        verifyForwardPath("/systems/details/Overview.do?sid=" + s.getId());
        TestUtils.flushAndEvict(s);
        s = ServerFactory.lookupById(s.getId());
        i = s.getValidAddonEntitlementsForServer().iterator();

        while (i.hasNext()) {
            Entitlement e = (Entitlement) i.next();
            Assertions.assertFalse(s.hasEntitlement(e), "does have: " + e);
        }

        assertEquals("Y", s.getAutoUpdate());
    }
}
