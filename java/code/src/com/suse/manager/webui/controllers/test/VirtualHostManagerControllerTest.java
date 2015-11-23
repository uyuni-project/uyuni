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
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.gatherer.GathererRunner;
import com.suse.manager.model.gatherer.GathererModule;
import com.suse.manager.webui.controllers.VirtualHostManagerController;
import com.suse.manager.webui.utils.SparkTestUtils;

import spark.Request;
import spark.RequestResponseFactory;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Test for basic scenarios in VirtualHostManagerController.
 */
public class VirtualHostManagerControllerTest extends BaseTestCaseWithUser {

    // common request that should be enough for controller functions without
    // dealing with parameters in the url
    private Response response;
    private VirtualHostManagerFactory factory;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        response = RequestResponseFactory.create(new RhnMockHttpServletResponse());
        factory = VirtualHostManagerFactory.getInstance();
        VirtualHostManagerController.setGathererRunner(new GathererRunner() {
            @Override
            public Map<String, GathererModule> listModules() {
                return new HashMap<>();
            }
        });
    }

    /**
     * Test the list endpoint.
     */
    @SuppressWarnings("unchecked")
    public void testList() {
        createVirtualHostManagerWithLabel("myVHM", user.getOrg());
        Map<String, Object> modelMap = (Map<String, Object>) VirtualHostManagerController
                .list(getRequestWithCsrf(""), response, user).getModel();

        assertNotNull(modelMap.get("virtualHostManagers"));
    }

    /**
     * Test the show endpoint.
     */
    @SuppressWarnings("unchecked")
    public void testShow() {
        String label = "myVHM";
        VirtualHostManager manager = createVirtualHostManagerWithLabel(label,
                user.getOrg());

        Request request = getRequestWithCsrf("/:id", manager.getId());
        Object result = VirtualHostManagerController.show(request, response, user)
                .getModel();

        assertEquals(manager, ((Map<String, Object>) result).get("virtualHostManager"));
    }

    /**
     * Test the show endpoint from a wrong organization.
     */
    @SuppressWarnings("unchecked")
    public void testShowWrongOrg() {
        Org otherOrg = UserTestUtils.createNewOrgFull("foobar org");
        String label = "myVHM";
        VirtualHostManager vhm = createVirtualHostManagerWithLabel(label, otherOrg);

        Request request = getRequestWithCsrf("/:id", vhm.getId());
        Object result =
                VirtualHostManagerController.show(request, response, user).getModel();

        assertFalse(((Map<String, Object>) result).containsKey(label));
    }

    /**
     * Test delete.
     */
    public void testDelete() {
        String label = "myVHM";
        VirtualHostManager vhm = createVirtualHostManagerWithLabel(label, user.getOrg());

        Request request = getRequestWithCsrf("/:id", vhm.getId());
        VirtualHostManagerController.delete(request, response, user);

        assertNull(factory.lookupByLabel(label));
    }

    /**
     * Test the delete endpoint from a wrong organization.
     */
    public void testGetDeleteWrongOrg() {
        Org otherOrg = UserTestUtils.createNewOrgFull("foobar org");
        String label = "myVHM";
        VirtualHostManager vhm = createVirtualHostManagerWithLabel(label, otherOrg);
        Request request = getRequestWithCsrf("/:id", vhm.getId());
        VirtualHostManagerController.delete(request, response, user);

        // the original VHM is not deleted
        assertNotNull(factory.lookupByLabel(label));
    }

    // Utils methods

    /**
     * Creates a request with csrf token.
     *
     * @param uri the uri
     * @param vars the vars
     * @return the request with csrf
     */
    private Request getRequestWithCsrf(String uri, Object... vars) {
        Request request = SparkTestUtils.createMockRequest(uri, vars);
        request.session(true).attribute("csrf_token", "bleh");
        return request;
    }

    /**
     * Creates and saves a virtual host manager with a certain label.
     *
     * @param label the label
     * @param org the org
     * @return the virtual host manager
     */
    private VirtualHostManager createVirtualHostManagerWithLabel(String label,
            Org org) {
        VirtualHostManager vhm =
            factory.createVirtualHostManager(label, org, "File",
                new HashMap<String, String>() { {
                        put("url", "notimportant");
                        put("username", "Bing Bong");
                        put("password", "imaginary friend");
                    } });
        factory.save(vhm);
        return vhm;
    }
}
