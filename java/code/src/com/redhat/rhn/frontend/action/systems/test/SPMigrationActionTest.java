/*
 * Copyright (c) 2022 SUSE LLC
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
package com.redhat.rhn.frontend.action.systems.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFamilyFactoryTest;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductSet;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.systems.SPMigrationAction;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.user.UserManager;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.RhnMockDynaActionForm;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.TestUtils;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Unit test for {@link SPMigrationAction}
 */
public class SPMigrationActionTest {

    private Server server;
    private RhnMockHttpServletRequest request;
    private RequestContext requestContext;
    private SUSEProduct baseProduct;
    private SUSEProduct addonProduct;
    private RhnMockDynaActionForm form;
    private Channel baseChannel;

    @BeforeEach
    public void setUp() throws Exception {
        request = TestUtils.getRequestWithSessionAndUser();
        requestContext = new RequestContext(request);
        User user = requestContext.getCurrentUser();
        server = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
        UserManager.storeUser(user);
        baseProduct = SUSEProductTestUtils.createTestSUSEProduct(ChannelFamilyFactoryTest.createTestChannelFamily());
        addonProduct = SUSEProductTestUtils.createTestSUSEProduct(ChannelFamilyFactoryTest.createTestChannelFamily());
        form = new RhnMockDynaActionForm();
        baseChannel = ChannelTestUtils.createBaseChannel(user);

        form.set("step", "confirm");
        form.set("addonProducts", new Long[]{5L});
        form.set("baseProduct", baseProduct.getId());
        form.set("childChannels", new Long[]{});
        form.set("baseChannel", baseChannel.getId());

        server.setInstalledProducts(Set.of(SUSEProductTestUtils.getInstalledProduct(baseProduct)));
    }

    @Test
    public void testExecuteSchedule() throws Exception {
        ActionMapping mapping = new ActionMapping();
        ActionForward target = new ActionForward("schedule", "path", false);

        String sid = server.getId().toString();
        request.addParameter("sid", sid);
        request.addParameter(RequestContext.DISPATCH, "schedule");
        RhnMockHttpServletResponse response = new RhnMockHttpServletResponse();
        mapping.addForwardConfig(target);
        SPMigrationAction action = new SPMigrationAction();
        ActionForward result = action.execute(mapping, form, request, response);
        assertEquals(result.getName(), "schedule");
    }

    @Test
    public void testSetConfirmAttributes() {
        SPMigrationAction action = new SPMigrationAction();
        Long[] targetAddonProducts = new Long[] {addonProduct.getId()};
        action.setConfirmAttributes(
            request,
            requestContext,
            server,
            form,
            baseProduct.getId(),
            targetAddonProducts,
            baseChannel.getId(),
            new Long[] {},
            true
        );
        assertEquals(
            new SUSEProductSet(baseProduct.getId(), List.of(targetAddonProducts)).toString(),
            request.getAttribute("targetProducts").toString()
        );
        assertEquals(baseProduct, request.getAttribute("baseProduct"));
        assertEquals(List.of(addonProduct), request.getAttribute("addonProducts"));
        assertTrue((Boolean) request.getAttribute("allowVendorChange"));
        assertEquals(baseChannel, request.getAttribute("baseChannel"));
        assertEquals(Collections.emptyList(), request.getAttribute("childChannels"));
        assertNotNull(request.getAttribute("date"));
    }
}
