/*
 * Copyright (c) 2021 SUSE LLC
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
package com.suse.manager.xmlrpc.admin.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.cloudpayg.CloudRmtHost;
import com.redhat.rhn.domain.cloudpayg.CloudRmtHostFactory;
import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.domain.cloudpayg.PaygSshDataFactory;
import com.redhat.rhn.domain.credentials.CloudRMTCredentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.domain.scc.SCCRepositoryCloudRmtAuth;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.xmlrpc.admin.AdminPaygHandler;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(JUnit5Mockery.class)
public class AdminPaygHandlerTest extends BaseHandlerTestCase {

    private AdminPaygHandler handler;
    private TaskomaticApi taskoApiMock;

    @RegisterExtension
    protected final JUnit5Mockery context = new JUnit5Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        context.setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        taskoApiMock = context.mock(TaskomaticApi.class);
        handler = new AdminPaygHandler(taskoApiMock);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        PaygSshDataFactory.lookupPaygSshData().forEach(PaygSshDataFactory::deletePaygSshData);
        HibernateFactory.commitTransaction();
    }

    @Test
    public void testListRoleCheck() {
        try {
            handler.list(regular);
            fail("PermissionCheckFailureException should be thrown");
        }
        catch (PermissionCheckFailureException e) {
        }
        try {
            handler.list(admin);
            fail("PermissionCheckFailureException should be thrown");
        }
        catch (PermissionCheckFailureException e) {
        }
    }

    @Test
    public void testCreateRoleCheck() throws TaskomaticApiException {
        context.checking(new Expectations() {
            { never(taskoApiMock).scheduleSinglePaygUpdate(with(any(PaygSshData.class))); }
        });
        try {
            handler.create(regular, null, null, null, null, null, null, null);
            fail("PermissionCheckFailureException should be thrown");
        }
        catch (PermissionCheckFailureException e) {
        }
        try {
            handler.create(regular, null, null, null, null, null, null, null);
            fail("PermissionCheckFailureException should be thrown");
        }
        catch (PermissionCheckFailureException e) {
        }
    }

    @Test
    public void testSetDetailsRoleCheck() throws TaskomaticApiException {
        context.checking(new Expectations() {
            { never(taskoApiMock).scheduleSinglePaygUpdate(with(any(PaygSshData.class))); }
        });
        try {
            handler.setDetails(regular, null, null);
            fail("PermissionCheckFailureException should be thrown");
        }
        catch (PermissionCheckFailureException e) {
        }
        try {
            handler.setDetails(regular, null, null);
            fail("PermissionCheckFailureException should be thrown");
        }
        catch (PermissionCheckFailureException e) {
        }
    }

    @Test
    public void testDeleteRoleCheck() {
        try {
            handler.delete(regular, null);
            fail("PermissionCheckFailureException should be thrown");
        }
        catch (PermissionCheckFailureException e) {
        }
        try {
            handler.delete(regular, null);
            fail("PermissionCheckFailureException should be thrown");
        }
        catch (PermissionCheckFailureException e) {
        }
    }

    @Test
    public void testCreate() throws TaskomaticApiException {
        context.checking(new Expectations() {
            {
                oneOf(taskoApiMock)
                        .scheduleSinglePaygUpdate(with(any(PaygSshData.class)));
            }
        });
        handler.create(satAdmin, "My special instance", "my-instance", 21,
                "username", "password",
                "key", "key_password");
        Optional<PaygSshData> payg = PaygSshDataFactory.lookupByHostname("my-instance");
        assertTrue(payg.isPresent());
        assertEquals(payg.get().getDescription(), "My special instance");
        assertEquals(payg.get().getPort(), Integer.valueOf(21));
        assertEquals(payg.get().getUsername(), "username");
        assertEquals(payg.get().getPassword(), "password");
        assertEquals(payg.get().getKey(), "key");
        assertEquals(payg.get().getKeyPassword(), "key_password");
        assertNull(payg.get().getErrorMessage());
        assertEquals(payg.get().getStatus(), PaygSshData.Status.P);
        assertNullBastion(payg.get());

        context.assertIsSatisfied();
    }

    @Test
    public void testCreateFull() throws TaskomaticApiException {
        context.checking(new Expectations() {
            {
                oneOf(taskoApiMock)
                        .scheduleSinglePaygUpdate(with(any(PaygSshData.class)));
            }
        });

        handler.create(satAdmin, "My special instance", "my-instance", 21,
                "username", "password",
                "key", "key_password",
                "bastion_instance", 23,
                "b_user", "b_pass",
                "b_key", "b_key_pass");
        Optional<PaygSshData> payg = PaygSshDataFactory.lookupByHostname("my-instance");
        assertTrue(payg.isPresent());
        assertEquals(payg.get().getDescription(), "My special instance");
        assertEquals(payg.get().getPort(), Integer.valueOf(21));
        assertEquals(payg.get().getUsername(), "username");
        assertEquals(payg.get().getPassword(), "password");
        assertEquals(payg.get().getKey(), "key");
        assertEquals(payg.get().getKeyPassword(), "key_password");
        assertEquals(payg.get().getBastionHost(), "bastion_instance");
        assertEquals(payg.get().getBastionPort(), Integer.valueOf(23));
        assertEquals(payg.get().getBastionUsername(), "b_user");
        assertEquals(payg.get().getBastionPassword(), "b_pass");
        assertEquals(payg.get().getBastionKey(), "b_key");
        assertEquals(payg.get().getBastionKeyPassword(), "b_key_pass");
        assertEquals(payg.get().getStatus(), PaygSshData.Status.P);
        assertNull(payg.get().getErrorMessage());
        context.assertIsSatisfied();
    }

    @Test
    public void testUpdate1() throws TaskomaticApiException {
        context.checking(new Expectations() {
            {
                oneOf(taskoApiMock)
                        .scheduleSinglePaygUpdate(with(any(PaygSshData.class)));
            }
        });
        PaygSshDataFactory.savePaygSshData(createPaygSshData(true, ""));

        Map<String, Object> data = new HashMap<>();
        data.put("description", "My new special instance");
        data.put("port", 123);
        data.put("username", "username_");
        data.put("password", "password_");
        data.put("key", "key_");
        data.put("key_password", "key_password_");
        data.put("bastion_host", "bastion_host_");
        data.put("bastion_port", 321);
        data.put("bastion_username", "bastion_username_");
        data.put("bastion_password", "bastion_password_");
        data.put("bastion_key", "bastion_key_");
        data.put("bastion_key_password", "bastion_keyPassword_");

        handler.setDetails(satAdmin, "my-instance", data);
        Optional<PaygSshData> payg = PaygSshDataFactory.lookupByHostname("my-instance");

        assertTrue(payg.isPresent());
        assertEquals(payg.get().getDescription(), "My new special instance");
        assertEquals(payg.get().getPort(), Integer.valueOf(123));
        assertEquals(payg.get().getUsername(), "username_");
        assertEquals(payg.get().getPassword(), "password_");
        assertEquals(payg.get().getKey(), "key_");
        assertEquals(payg.get().getKeyPassword(), "key_password_");
        assertEquals(payg.get().getBastionHost(), "bastion_host_");
        assertEquals(payg.get().getBastionPort(), Integer.valueOf(321));
        assertEquals(payg.get().getBastionUsername(), "bastion_username_");
        assertEquals(payg.get().getBastionPassword(), "bastion_password_");
        assertEquals(payg.get().getBastionKey(), "bastion_key_");
        assertEquals(payg.get().getBastionKeyPassword(), "bastion_keyPassword_");
        assertEquals(payg.get().getStatus(), PaygSshData.Status.P);
        assertNull(payg.get().getErrorMessage());
        context.assertIsSatisfied();
    }

    @Test
    public void testUpdate2() throws TaskomaticApiException {
        context.checking(new Expectations() {
            {
                oneOf(taskoApiMock)
                        .scheduleSinglePaygUpdate(with(any(PaygSshData.class)));
            }
        });
        PaygSshDataFactory.savePaygSshData(createPaygSshData(true, ""));

        Map<String, Object> data = new HashMap<>();

        handler.setDetails(satAdmin, "my-instance", data);
        Optional<PaygSshData> payg = PaygSshDataFactory.lookupByHostname("my-instance");

        assertTrue(payg.isPresent());
        assertEquals(payg.get().getDescription(), "My special instance");
        assertEquals(payg.get().getPort(), Integer.valueOf(21));
        assertEquals(payg.get().getUsername(), "username");
        assertEquals(payg.get().getPassword(), "password");
        assertEquals(payg.get().getKey(), "key");
        assertEquals(payg.get().getKeyPassword(), "keyPassword");
        assertEquals(payg.get().getBastionHost(), "bastionHost");
        assertEquals(payg.get().getBastionPort(), Integer.valueOf(23));
        assertEquals(payg.get().getBastionUsername(), "bastionUsername");
        assertEquals(payg.get().getBastionPassword(), "bastionPassword");
        assertEquals(payg.get().getBastionKey(), "bastionKey");
        assertEquals(payg.get().getBastionKeyPassword(), "bastionKeyPassword");
        assertEquals(payg.get().getStatus(), PaygSshData.Status.P);
        assertNull(payg.get().getErrorMessage());
        context.assertIsSatisfied();
    }

    @Test
    public void testUpdate3() throws TaskomaticApiException {
        context.checking(new Expectations() {
            {
                oneOf(taskoApiMock)
                        .scheduleSinglePaygUpdate(with(any(PaygSshData.class)));
            }
        });
        PaygSshDataFactory.savePaygSshData(createPaygSshData(true, ""));

        Map<String, Object> data = new HashMap<>();
        data.put("port", null);
        data.put("password", null);
        data.put("key", null);
        data.put("key_password", null);
        data.put("bastion_host", null);
        data.put("bastion_port", null);
        data.put("bastion_username", null);
        data.put("bastion_password", null);
        data.put("bastion_key", null);
        data.put("bastion_key_password", null);

        handler.setDetails(satAdmin, "my-instance", data);
        Optional<PaygSshData> payg = PaygSshDataFactory.lookupByHostname("my-instance");

        assertTrue(payg.isPresent());
        assertNull(payg.get().getPort());
        assertEquals(payg.get().getUsername(), "username");
        assertNull(payg.get().getPassword());
        assertNull(payg.get().getKey());
        assertNull(payg.get().getKeyPassword());

        assertNull(payg.get().getBastionHost());
        assertNull(payg.get().getBastionPort());
        assertNull(payg.get().getBastionUsername());
        assertNull(payg.get().getBastionPassword());
        assertNull(payg.get().getBastionKey());
        assertNull(payg.get().getBastionKeyPassword());

        assertEquals(payg.get().getStatus(), PaygSshData.Status.P);
        assertNull(payg.get().getErrorMessage());
        assertNullBastion(payg.get());

        context.assertIsSatisfied();
    }

    @Test
    public void testList() {
        PaygSshDataFactory.savePaygSshData(createPaygSshData(true, "_1"));
        PaygSshDataFactory.savePaygSshData(createPaygSshData(true, "_2"));
        PaygSshDataFactory.savePaygSshData(createPaygSshData(false, "_3"));

        List<PaygSshData> data = handler.list(satAdmin);
        assertEquals(data.size(), 3);
    }

    @Test
    public void testGetDetails() {
        PaygSshData paygData = createPaygSshData(true, "");
        PaygSshDataFactory.savePaygSshData(paygData);
        PaygSshDataFactory.savePaygSshData(createPaygSshData(false, "_1"));

        PaygSshData data = handler.getDetails(satAdmin, paygData.getHost());
        assertEquals(paygData.getDescription(), "My special instance");
        assertEquals(paygData.getPort(), data.getPort());
        assertEquals(paygData.getUsername(), data.getUsername());
        assertEquals(paygData.getPassword(), data.getPassword());
        assertEquals(paygData.getKey(), data.getKey());
        assertEquals(paygData.getKeyPassword(), data.getKeyPassword());
        assertEquals(paygData.getBastionHost(), data.getBastionHost());
        assertEquals(paygData.getBastionPort(), data.getBastionPort());
        assertEquals(paygData.getBastionUsername(), data.getBastionUsername());
        assertEquals(paygData.getBastionPassword(), data.getBastionPassword());
        assertEquals(paygData.getBastionKey(), data.getBastionKey());
        assertEquals(paygData.getBastionKeyPassword(), data.getBastionKeyPassword());
    }

    @Test
    public void testDelete1() {
        PaygSshDataFactory.savePaygSshData(createPaygSshData(false, "_1"));
        PaygSshData paygData = createPaygSshData(true, "");
        PaygSshDataFactory.savePaygSshData(paygData);

        assertEquals(PaygSshDataFactory.lookupPaygSshData().size(), 2);

        handler.delete(satAdmin, paygData.getHost());

        assertEquals(PaygSshDataFactory.lookupPaygSshData().size(), 1);
    }

    @Test
    public void testDelete() {
        PaygSshDataFactory.savePaygSshData(createPaygSshData(false, "_1"));
        PaygSshData paygData = createPaygSshData(true, "");
        PaygSshDataFactory.savePaygSshData(paygData);

        CloudRmtHost cloudRmtHost = CloudRmtHostFactory.createCloudRmtHost();
        cloudRmtHost.setHost("rmt_host");
        cloudRmtHost.setPaygSshData(paygData);
        cloudRmtHost.setIp("1.2.3.4");
        CloudRmtHostFactory.saveCloudRmtHost(cloudRmtHost);

        CloudRMTCredentials credentials = CredentialsFactory.createCloudRmtCredentials("username", "password", "url");
        credentials.setPaygSshData(paygData);
        CredentialsFactory.storeCredentials(credentials);

        SCCRepository repo = createTestRepo(0L);
        SCCCachingFactory.saveRepository(repo);

        SCCRepositoryCloudRmtAuth newAuth = new SCCRepositoryCloudRmtAuth();
        newAuth.setCredentials(credentials);
        newAuth.setRepo(repo);
        SCCCachingFactory.saveRepositoryAuth(newAuth);

        credentials.setPaygSshData(paygData);
        paygData.setRmtHosts(cloudRmtHost);
        PaygSshDataFactory.savePaygSshData(paygData);

        HibernateFactory.getSession().flush();

        assertEquals(PaygSshDataFactory.lookupPaygSshData().size(), 2);
        assertEquals(CloudRmtHostFactory.lookupCloudRmtHosts().size(), 1);
        assertNotNull(CredentialsFactory.lookupCredentialsById(credentials.getId()));
        assertEquals(SCCCachingFactory.lookupRepositoryAuth().size(), 1);

        handler.delete(satAdmin, paygData.getHost());

        assertEquals(PaygSshDataFactory.lookupPaygSshData().size(), 1);
        assertEquals(CloudRmtHostFactory.lookupCloudRmtHosts().size(), 0);
        assertNull(CredentialsFactory.lookupCredentialsById(credentials.getId()));
        assertEquals(SCCCachingFactory.lookupRepositoryAuth().size(), 0);

    }

    private void assertNullBastion(PaygSshData paygSshData) {
        assertNull(paygSshData.getBastionHost());
        assertNull(paygSshData.getBastionPort());
        assertNull(paygSshData.getBastionUsername());
        assertNull(paygSshData.getBastionPassword());
        assertNull(paygSshData.getBastionKey());
        assertNull(paygSshData.getBastionKeyPassword());
    }

    private SCCRepository createTestRepo(Long id) {
        SCCRepository repo = new SCCRepository();
        repo.setSccId(id);
        repo.setDescription(TestUtils.randomString());
        repo.setDistroTarget(TestUtils.randomString());
        repo.setName(TestUtils.randomString());
        repo.setUrl(TestUtils.randomString());
        repo.setAutorefresh(true);
        return repo;
    }

    private PaygSshData createPaygSshData(boolean bastion, String sufix) {
        PaygSshData paygSshData = PaygSshDataFactory.createPaygSshData();
        paygSshData.setDescription("My special instance" + sufix);
        paygSshData.setHost("my-instance" + sufix);
        paygSshData.setPort(21);
        paygSshData.setUsername("username" + sufix);
        paygSshData.setPassword("password" + sufix);
        paygSshData.setKey("key" + sufix);
        paygSshData.setKeyPassword("keyPassword" + sufix);
        if (bastion) {
            paygSshData.setBastionHost("bastionHost" + sufix);
            paygSshData.setBastionPort(23);
            paygSshData.setBastionUsername("bastionUsername" + sufix);
            paygSshData.setBastionPassword("bastionPassword" + sufix);
            paygSshData.setBastionKey("bastionKey" + sufix);
            paygSshData.setBastionKeyPassword("bastionKeyPassword" + sufix);
        }
        paygSshData.setErrorMessage("my status");
        return paygSshData;
    }
}
