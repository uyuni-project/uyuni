/**
 * Copyright (c) 2015 SUSE LLC
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

package com.suse.manager.webui.controllers.test;

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.virtualhostmanager.InvalidGathererConfigException;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.UserTestUtils;
import com.suse.manager.webui.controllers.VirtualHostManagerController;
import com.suse.manager.webui.utils.SparkTestUtils;
import spark.Request;
import spark.RequestResponseFactory;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Test for basic scenarios in VirtualHostManagerController
 */
public class VirtualHostManagerControllerTest extends BaseTestCaseWithUser {

    // common request that should be enough for controller functions without dealing
    // with parameters in the url
    private Request commonRequest;
    private Response response;
    private VirtualHostManagerFactory factory;

    public void setUp() throws Exception {
        super.setUp();

        commonRequest = getRequestWithCsrf("http://localhost:8080");
        response = RequestResponseFactory.create(new RhnMockHttpServletResponse());
        factory = new VirtualHostManagerFactory() {
            @Override
            protected void validateGathererConfiguration(String moduleName,
                    Map<String, String> parameters) {
                // no op
            }
        };
    }

    public void testGetAll() throws Exception {
        createVirtualHostManagerWithLabel("myVHM", user.getOrg());
        Map modelMap = (Map) VirtualHostManagerController.getAll(getRequestWithCsrf(""),
                response, user).getModel();
        // just test for non-emptiness
        assertNotNull(modelMap.get("virtualHostManagers"));
    }

    public void testGetWrongOrg() throws InvalidGathererConfigException {
        Org otherOrg = UserTestUtils.createNewOrgFull("foobar org");
        String label = "myVHM";
        createVirtualHostManagerWithLabel(label, otherOrg);

        Request request = getRequestWithCsrf("/:vhmlabel", label);
        Object result = VirtualHostManagerController.get(request, response, user)
                .getModel();

        assertFalse(((Map) result).containsKey(label));
    }

    public void testGet() throws InvalidGathererConfigException {
        String label = "myVHM";
        VirtualHostManager manager = createVirtualHostManagerWithLabel(label,user.getOrg());

        Request request = getRequestWithCsrf("/:vhmlabel", label);
        Object result =
                VirtualHostManagerController.get(request, response, user).getModel();
        assertEquals(manager, ((Map) result).get("virtualHostManager"));
    }

    public void testDelete() throws InvalidGathererConfigException {
        String label = "myVHM";
        createVirtualHostManagerWithLabel(label, user.getOrg());

        Request request = getRequestWithCsrf("/:vhmlabel", label);
        VirtualHostManagerController.delete(request, response, user);

        assertNull(factory.lookupByLabel(label));
    }

    public void testGetDeleteWrongOrg() throws InvalidGathererConfigException {
        Org otherOrg = UserTestUtils.createNewOrgFull("foobar org");
        String label = "myVHM";
        createVirtualHostManagerWithLabel(label, otherOrg);
        VirtualHostManagerController.delete(commonRequest, response, user);

        // the original VHM is not deleted
        assertNotNull(factory.lookupByLabel(label));
    }

    public void testAdd() throws Exception {
        // todo
    }

    public void testRefresh() throws Exception {
        // todo
    }

    // Utils methods

    private Request getRequestWithCsrf(String uri, Object ... vars) {
        Request request = SparkTestUtils.createMockRequest(uri, vars);
        request.session(true).attribute("csrf_token", "bleh");
        return request;
    }

    private VirtualHostManager createVirtualHostManagerWithLabel(String label, Org otherOrg) throws InvalidGathererConfigException {
        return factory.createVirtualHostManager(label,
                user.getOrg(),
                "File",
                new HashMap<String, String>() {{
                    put("url", "notimportant");
                }});
    }
}