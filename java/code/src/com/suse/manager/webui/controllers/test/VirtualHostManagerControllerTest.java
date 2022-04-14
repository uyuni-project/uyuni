/*
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

import static com.suse.manager.webui.utils.SparkTestUtils.createMockRequestWithParams;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerConfig;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.gatherer.GathererJsonIO;
import com.suse.manager.gatherer.GathererRunner;
import com.suse.manager.model.gatherer.GathererModule;
import com.suse.manager.webui.controllers.VirtualHostManagerController;
import com.suse.manager.webui.utils.SparkTestUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spark.Request;
import spark.RequestResponseFactory;
import spark.Response;

/**
 * Test for basic scenarios in VirtualHostManagerController.
 */
public class VirtualHostManagerControllerTest extends BaseTestCaseWithUser {

    // common request that should be enough for controller functions without
    // dealing with parameters in the url
    private Response response;
    private VirtualHostManagerFactory factory;
    private final String baseUri = "http://localhost:8080/rhn";
    private static final Gson GSON = new GsonBuilder().create();
    private static final String VIRT_HOST_GATHERER_MODULES = "{\n" +
        "    \"Kubernetes\": {\n" +
        "        \"module\": \"Kubernetes\",\n" +
        "        \"url\": \"\",\n" +
        "        \"username\": \"\",\n" +
        "        \"password\": \"\",\n" +
        "        \"client-cert\": \"\",\n" +
        "        \"client-key\": \"\",\n" +
        "        \"ca-cert\": \"\",\n" +
        "        \"kubeconfig\": \"\",\n" +
        "        \"context\": \"\"\n" +
        "    },\n" +
        "    \"File\": {\n" +
        "        \"module\": \"File\",\n" +
        "        \"url\": \"\"\n" +
        "    },\n" +
        "    \"VMware\": {\n" +
        "        \"module\": \"VMware\",\n" +
        "        \"hostname\": \"\",\n" +
        "        \"port\": 443,\n" +
        "        \"username\": \"\",\n" +
        "        \"password\": \"\"\n" +
        "    }\n" +
        "}\n";

    /**
     * {@inheritDoc}
     */
    @Override
    @BeforeEach
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

        Map<String, GathererModule> modules = new GathererJsonIO()
                .readGathererModules(VIRT_HOST_GATHERER_MODULES);
        VirtualHostManagerController.setGathererModules(modules);
    }

    /**
     * Test the list endpoint.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGet() {
        VirtualHostManager vhm = createVirtualHostManagerWithLabel("myVHM", user.getOrg());
        String json = (String)VirtualHostManagerController
                .get(getRequestWithCsrf(""), response, user);
        Map<String, Object> model = GSON.fromJson(json, Map.class);
        List<Map<String, Object>> data = (List<Map<String, Object>>)model.get("data");
        assertEquals(1, data.size());
        assertEquals("myVHM", data.get(0).get("label"));
        assertEquals("File", data.get(0).get("gathererModule"));
        assertEquals(user.getOrg().getName(), data.get(0).get("orgName"));
        assertEquals((Object)vhm.getId(), ((Double)data.get(0).get("id")).longValue());
    }

    /**
     * Test the show endpoint from a wrong organization.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetWrongOrg() {
        Org otherOrg = UserTestUtils.createNewOrgFull("foobar org");
        String label = "TestVHM_" + TestUtils.randomString(10);
        createVirtualHostManagerWithLabel(label, otherOrg);

        Request request = getRequestWithCsrf("/manager/api/vhms");
        String result = (String)
                VirtualHostManagerController.get(request, response, user);
        Map<String, Object> model = GSON.fromJson(result, Map.class);
        assertTrue(((List)model.get("data")).isEmpty());
    }

    /**
     * Test create.
     */
    @Test
    public void testCreate() {
        String label = "TestVHM_" + TestUtils.randomString(10);
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("label", label);
        queryParams.put("module", "File");
        queryParams.put("module_url", "file:///some/file");
        Request request = createMockRequestWithParams("/manager/api/vhms/create",
                queryParams);

        VirtualHostManagerController.setMockFactory(new VirtualHostManagerFactory() {
            @Override
            public boolean isConfigurationValid(String moduleName,
                    Map<String, String> parameters, String... ignoreParams) {
                return true;
            }
        });
        VirtualHostManagerController.create(request, response, user);

        VirtualHostManager created = VirtualHostManagerFactory.getInstance()
                .lookupByLabel(label);
        assertEquals(label, created.getLabel());
        assertEquals("File", created.getGathererModule());
        assertEquals(1, created.getConfigs().size());
        VirtualHostManagerConfig config = created.getConfigs().iterator().next();
        assertEquals("url", config.getParameter());
        assertEquals("file:///some/file", config.getValue());
        config.getVirtualHostManager();
    }

    /**
     * Test create a VHM with a missing cfg param.
     * Should result in an error.
     */
    @Test
    public void testCreateNoCfgParam() {
        String label = "TestVHM_" + TestUtils.randomString(10);
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("module", "file");
        queryParams.put("label", label);

        Request request = createMockRequestWithParams("/manager/api/vhms/create", queryParams,
                new Object[] {});
        String result = VirtualHostManagerController.create(request, response, user);
        Map<String, Object> res = GSON.fromJson(result, Map.class);
        List<String> errs = (List<String>)res.get("messages");
        assertEquals(1, errs.size());
        assertEquals("All fields are mandatory.", errs.get(0));
    }

    /**
     * Test delete.
     */
    @Test
    public void testDelete() throws UnsupportedEncodingException {
        String label = "TestVHM_" + TestUtils.randomString(10);
        VirtualHostManager vhm = createVirtualHostManagerWithLabel(label, user.getOrg());
        Request request = getDeleteRequestWithCsrfAndBody("/manager/api/vhms/delete/:id", "", vhm.getId());
        VirtualHostManagerController.delete(request, response, user);
        HibernateFactory.getSession().flush();

        assertNull(factory.lookupByLabel(label));
    }

    /**
     * Test the delete endpoint from a wrong organization.
     */
    @Test
    public void testGetDeleteWrongOrg() throws UnsupportedEncodingException {
        Org otherOrg = UserTestUtils.createNewOrgFull("foobar org");
        String label = "TestVHM_" + TestUtils.randomString(10);
        VirtualHostManager vhm = createVirtualHostManagerWithLabel(label, otherOrg);
        Request request = getPostRequestWithCsrfAndBody("/manager/api/vhms/delete/" + vhm.getId(), "");
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
        Request request = SparkTestUtils.createMockRequest(baseUri + uri, vars);
        request.session(true).attribute("csrf_token", "bleh");
        return request;
    }

    /**
     * Creates a request with csrf token.
     *
     * @param uri the uri
     * @param vars the vars
     * @return the request with csrf
     */
    private Request getPostRequestWithCsrfAndBody(String uri, String body, Object... vars)
            throws UnsupportedEncodingException {
        Request request = SparkTestUtils.createMockRequestWithBody(baseUri + uri, Collections.emptyMap(), body, vars);
        request.session(true).attribute("csrf_token", "bleh");
        return request;
    }

    private Request getDeleteRequestWithCsrfAndBody(String uri, String body, Object... vars)
            throws UnsupportedEncodingException {
        Request request = SparkTestUtils.createDeleteMockRequestWithBody(
                baseUri + uri, Collections.emptyMap(), body, vars);
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
                    new HashMap<>() {
                        {
                            put("url", "notimportant");
                            put("username", "Bing Bong");
                            put("password", "imaginary friend");
                        }
                    });
        factory.save(vhm);
        return vhm;
    }
}
