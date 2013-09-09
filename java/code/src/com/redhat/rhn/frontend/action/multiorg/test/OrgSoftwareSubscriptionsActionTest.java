/**
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
package com.redhat.rhn.frontend.action.multiorg.test;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.test.ChannelFamilyFactoryTest;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.taglibs.list.helper.ListHelper;
import com.redhat.rhn.testing.RhnPostMockStrutsTestCase;
import com.redhat.rhn.testing.TestUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * OrgSoftwareSubscriptionsActionTest
 * @version $Rev: 1 $
 */
public class OrgSoftwareSubscriptionsActionTest extends RhnPostMockStrutsTestCase {


    /** The channel family. */
    private ChannelFamily cfm;

    /**
     * Sets up channel family and subscriptions.
     *
     * @throws Exception if problems arise
     * @see com.redhat.rhn.testing.RhnPostMockStrutsTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        cfm = ChannelFamilyFactoryTest.createTestChannelFamily(UserFactory
                        .findRandomOrgAdmin(OrgFactory.getSatelliteOrg()));
        System.out.println("CFM TEST ID is " + cfm.getId().toString());
        Map<String, String> subsMap = new HashMap<String, String>();
        subsMap.put(cfm.getId().toString(), "10");
        request.getSession().setAttribute(
                "OrgSoftwareSubscriptions" + user.getOrg().getId().toString(), subsMap);

    }

    public void testExecute() throws Exception {
        user.getOrg().addRole(RoleFactory.SAT_ADMIN);
        user.addRole(RoleFactory.SAT_ADMIN);
        addRequestParameter(RequestContext.ORG_ID, user.getOrg().getId().toString());
        setRequestPathInfo("/admin/multiorg/OrgSoftwareSubscriptions");
        actionPerform();
        assertNotNull(request.getAttribute("org"));
        assertNotNull(request.getAttribute(ListHelper.DATA_SET));
        List pl = (List) request.getAttribute(ListHelper.DATA_SET);
        assertTrue(pl.size() > 0);
    }

    public void testExecuteSubmit() throws Exception {
        user.getOrg().addRole(RoleFactory.SAT_ADMIN);
        user.addRole(RoleFactory.SAT_ADMIN);
        addRequestParameter(RequestContext.ORG_ID, user.getOrg().getId().toString());
        addRequestParameter(cfm.getId().toString(), "10");
        addRequestParameter("updateOrganizations", "1");
        LocalizationService ls = LocalizationService.getInstance();
        addRequestParameter("dispatch", ls.getMessage("orgdetails.jsp.submit"));
        setRequestPathInfo("/admin/multiorg/OrgSoftwareSubscriptions");

        addSubmitted();
        actionPerform();
        assertTrue(getActualForward().contains("oid=" + user.getOrg().getId()));
        verifyActionMessage("org.entitlements.syssoft.success");
        cfm = (ChannelFamily) TestUtils.reload(cfm);
        Iterator i = cfm.getPrivateChannelFamilies().iterator();
        while (i.hasNext()) {
            TestUtils.reload(i.next());
        }
        assertEquals(10, cfm.getMaxMembers(user.getOrg()).longValue());

        // Check the setup request params
        assertNotNull(request.getAttribute(ListHelper.DATA_SET));
        List pl = (List) request.getAttribute(ListHelper.DATA_SET);
        assertTrue(pl.size() > 0);
    }
}

