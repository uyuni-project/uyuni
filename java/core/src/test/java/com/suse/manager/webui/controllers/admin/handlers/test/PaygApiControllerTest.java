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
package com.suse.manager.webui.controllers.admin.handlers.test;

import static com.redhat.rhn.testing.RhnBaseTestCase.assertContains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.cloudpayg.CloudRmtHostFactory;
import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.domain.cloudpayg.PaygSshDataFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.admin.PaygAdminManager;
import com.suse.manager.webui.controllers.admin.beans.PaygFullResponse;
import com.suse.manager.webui.controllers.admin.beans.PaygProperties;
import com.suse.manager.webui.controllers.admin.handlers.PaygApiContoller;
import com.suse.manager.webui.controllers.test.BaseControllerTestCase;
import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

public class PaygApiControllerTest extends BaseControllerTestCase {

    private TaskomaticApi taskomaticMock;
    private static final Gson GSON = new GsonBuilder().create();

    private PaygApiContoller PaygApiContoller;
    private User satAdmin;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        clearDb();

        satAdmin = UserTestUtils.createUser("satUser", user.getOrg().getId());
        satAdmin.addPermanentRole(RoleFactory.SAT_ADMIN);

        context.setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        taskomaticMock = context.mock(TaskomaticApi.class);
        PaygApiContoller = new PaygApiContoller(new PaygAdminManager(taskomaticMock));
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        clearDb();
    }

    private void clearDb() {
        CloudRmtHostFactory.lookupCloudRmtHosts().forEach(CloudRmtHostFactory::deleteCloudRmtHost);
        PaygSshDataFactory.lookupPaygSshData().forEach(PaygSshDataFactory::deletePaygSshData);
        HibernateFactory.commitTransaction();
    }

    private PaygSshData createPaygSshData() {
        PaygSshData paygSshData = PaygSshDataFactory.createPaygSshData();
        paygSshData.setDescription("My special instance");
        paygSshData.setHost("my-instance");
        paygSshData.setPort(21);
        paygSshData.setUsername("username");
        paygSshData.setPassword("password");
        paygSshData.setKey("key");
        paygSshData.setKeyPassword("keyPassword");
        paygSshData.setErrorMessage("My status");
        PaygSshDataFactory.savePaygSshData(paygSshData);
        return paygSshData;
    }

    @Test
    public void testRemovePermitionCheck() {
        try {
            PaygSshData paygInfo = createPaygSshData();
            PaygApiContoller.removePaygInstance(
                    getRequestWithCsrf("/manager/api/admin/config/payg/:id", paygInfo.getId()),
                    response, user);
            fail("permission check not ok");
        }
        catch (PermissionException e) {
            // it's what we expect
        }
    }

    @Test
    public void testRemove() {
        PaygSshData paygInfo = createPaygSshData();
        String json = PaygApiContoller.removePaygInstance(
                getRequestWithCsrf("/manager/api/admin/config/payg/:id", paygInfo.getId()),
                response, satAdmin);
        ResultJson resultJson = GSON.fromJson(json, ResultJson.class);
        assertTrue(resultJson.isSuccess());
        assertEquals(resultJson.getMessages().size(), 1);
        assertContains(resultJson.getMessages().get(0).toString(), "success");
        assertNull(resultJson.getErrors());
    }

    @Test
    public void testUpdatePaygPermitionCheck() {
        try {
            PaygSshData paygInfo = createPaygSshData();
            PaygApiContoller.updatePayg(
                    getRequestWithCsrf("/manager/api/admin/config/payg/:id", paygInfo.getId()),
                    response, user);
            fail("permission check not ok");
        }
        catch (PermissionException e) {
            // it's what we expect
        }
    }

    @Test
    public void testUpdatePayg1() throws UnsupportedEncodingException {
        PaygSshData paygInfo = createPaygSshData();
        String dataJson = PaygApiContoller.updatePayg(
                getPostRequestWithCsrfAndBody("/manager/api/admin/config/payg/:id",
                        "",
                        paygInfo.getId()),
                response, satAdmin);
        ResultJson returnData = GSON.fromJson(dataJson, ResultJson.class);
        assertFalse(returnData.isSuccess());
        assertEquals(returnData.getMessages().size(), 1);
    }

    @Test
    public void testUpdatePayg2() throws UnsupportedEncodingException, TaskomaticApiException {
        context.checking(new Expectations() {
            {
                oneOf(taskomaticMock)
                        .scheduleSinglePaygUpdate(with(any(PaygSshData.class)));
            }
        });

        PaygSshData paygInfo = createPaygSshData();
        PaygProperties properties = new PaygProperties("d", "h", "8001",
                "u", "p", "k", "kp",
                "bh", "8002", "bu", "bp", "bk", "bkp", true, true);

        String dataJson = PaygApiContoller.updatePayg(
                getPostRequestWithCsrfAndBody("/manager/api/admin/config/payg/:id",
                        GSON.toJson(properties),
                        paygInfo.getId()),
                response, satAdmin);
        ResultJson<PaygFullResponse> returnData = GSON.fromJson(dataJson,
                new TypeToken<ResultJson<PaygFullResponse>>() { }.getType());
        assertTrue(returnData.isSuccess());
        assertNull(returnData.getMessages());
        assertEquals(properties.getDescription(), returnData.getData().getProperties().getDescription());
        assertEquals(paygInfo.getHost(), returnData.getData().getProperties().getHost());
        assertEquals(properties.getPort(), returnData.getData().getProperties().getPort());
        assertEquals(properties.getUsername(), returnData.getData().getProperties().getUsername());
        assertEquals("", returnData.getData().getProperties().getPassword());
        assertEquals("", returnData.getData().getProperties().getKey());
        assertEquals("", returnData.getData().getProperties().getKeyPassword());

        assertEquals(properties.getBastionHost(), returnData.getData().getProperties().getBastionHost());
        assertEquals(properties.getBastionPort(), returnData.getData().getProperties().getBastionPort());
        assertEquals(properties.getBastionUsername(), returnData.getData().getProperties().getBastionUsername());
        assertEquals("", returnData.getData().getProperties().getBastionPassword());
        assertEquals("", returnData.getData().getProperties().getBastionKey());
        assertEquals("", returnData.getData().getProperties().getBastionKeyPassword());

        //FIXME check data in the database

        context.assertIsSatisfied();
    }


    @Test
    public void testCreatePaygPermitionCheck() {
        try {
            PaygApiContoller.createPayg(
                    getRequestWithCsrf("/manager/api/admin/config/payg"),
                    response, user);
            fail("permission check not ok");
        }
        catch (PermissionException e) {
            // it's what we expect
        }
    }

    @Test
    public void testCreate() throws UnsupportedEncodingException, TaskomaticApiException {
        context.checking(new Expectations() {
            {
                oneOf(taskomaticMock)
                        .scheduleSinglePaygUpdate(with(any(PaygSshData.class)));
            }
        });

        PaygProperties properties = new PaygProperties("d", "h", "8001",
                "u", "p", "k", "kp",
                "bh", "8002", "bu", "bp", "bk", "bkp", true, true);

        String dataJson = PaygApiContoller.createPayg(
                getPostRequestWithCsrfAndBody("/manager/api/admin/config/payg",
                        GSON.toJson(properties)),
                response, satAdmin);
        ResultJson<Long> returnData = GSON.fromJson(dataJson, new TypeToken<ResultJson<Long>>() { }.getType());
        assertNotNull(returnData.getData());
        Optional<PaygSshData> dbPropertiesOpt = PaygSshDataFactory.lookupById(returnData.getData().intValue());
        assertTrue(dbPropertiesOpt.isPresent());

        assertTrue(returnData.isSuccess());
        assertNull(returnData.getMessages());
        assertEquals(properties.getDescription(), dbPropertiesOpt.get().getDescription());
        assertEquals(properties.getHost(), dbPropertiesOpt.get().getHost());
        assertEquals(properties.getPort(), dbPropertiesOpt.get().getPort().toString());
        assertEquals(properties.getUsername(), dbPropertiesOpt.get().getUsername());
        assertEquals(properties.getPassword(), dbPropertiesOpt.get().getPassword());
        assertEquals(properties.getKey(), dbPropertiesOpt.get().getKey());
        assertEquals(properties.getKeyPassword(), dbPropertiesOpt.get().getKeyPassword());

        assertEquals(properties.getBastionHost(), dbPropertiesOpt.get().getBastionHost());
        assertEquals(properties.getBastionPort(), dbPropertiesOpt.get().getBastionPort().toString());
        assertEquals(properties.getBastionUsername(), dbPropertiesOpt.get().getBastionUsername());
        assertEquals(properties.getBastionPassword(), dbPropertiesOpt.get().getBastionPassword());
        assertEquals(properties.getBastionKey(), dbPropertiesOpt.get().getBastionKey());
        assertEquals(properties.getBastionKeyPassword(), dbPropertiesOpt.get().getBastionKeyPassword());

        context.assertIsSatisfied();
    }
}
