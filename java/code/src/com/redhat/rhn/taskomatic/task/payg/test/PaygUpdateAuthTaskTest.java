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

package com.redhat.rhn.taskomatic.task.payg.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.cloudpayg.CloudRmtHostFactory;
import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.domain.cloudpayg.PaygSshDataFactory;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.notification.types.NotificationType;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.taskomatic.task.payg.PaygAuthDataExtractor;
import com.redhat.rhn.taskomatic.task.payg.PaygDataExtractException;
import com.redhat.rhn.taskomatic.task.payg.PaygUpdateAuthTask;
import com.redhat.rhn.taskomatic.task.payg.beans.PaygInstanceInfo;
import com.redhat.rhn.taskomatic.task.payg.beans.PaygProductInfo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jcraft.jsch.JSchException;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.jmock.lib.concurrent.Synchroniser;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PaygUpdateAuthTaskTest extends BaseHandlerTestCase {

    private static PaygAuthDataExtractor paygAuthDataExtractorMock;
    private static final PaygUpdateAuthTask PAYG_DATA_TASK = new PaygUpdateAuthTask();

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    private static final Mockery CONTEXT = new JUnit3Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    static {
        CONTEXT.setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        paygAuthDataExtractorMock = CONTEXT.mock(PaygAuthDataExtractor.class);
        PAYG_DATA_TASK.setPaygDataExtractor(paygAuthDataExtractorMock);
    }

    private PaygSshData paygData;
    private PaygInstanceInfo paygInstanceInfo;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        clearDb();

        paygData = createPaygSshData();
        PaygSshDataFactory.savePaygSshData(paygData);
        paygInstanceInfo = createPaygInstanceInfo();
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        clearDb();
    }

    private void clearDb() {
        CloudRmtHostFactory.lookupCloudRmtHosts().forEach(CloudRmtHostFactory::deleteCloudRmtHost);
        PaygSshDataFactory.lookupPaygSshData().forEach(PaygSshDataFactory::deletePaygSshData);
        UserNotificationFactory.deleteNotificationMessagesBefore(Date.from(Instant.now()));
        HibernateFactory.commitTransaction();
    }

    public void testJschException() throws Exception {
        CONTEXT.checking(new Expectations() {
            {
                oneOf(paygAuthDataExtractorMock).extractAuthData(with(any(PaygSshData.class)));
                will(throwException(new JSchException("My JSchException exception")));
            }});
        PAYG_DATA_TASK.execute(null);
        paygData = HibernateFactory.reload(paygData);
        assertContains(paygData.getErrorMessage(), "My JSchException exception");
        assertEquals(paygData.getStatus(), PaygSshData.Status.E);
        assertEquals(1, UserNotificationFactory.listAllNotificationMessages().size());
        assertEquals(NotificationType.PaygAuthenticationUpdateFailed,
                UserNotificationFactory.listAllNotificationMessages().get(0).getType());
    }

    public void testPaygDataExtractException() throws Exception {
        CONTEXT.checking(new Expectations() {
            {
                oneOf(paygAuthDataExtractorMock).extractAuthData(with(any(PaygSshData.class)));
                will(throwException(new PaygDataExtractException("My PaygDataExtractException")));
            }});
        PAYG_DATA_TASK.execute(null);
        paygData = HibernateFactory.reload(paygData);
        assertContains(paygData.getErrorMessage(), "My PaygDataExtractException");
        assertEquals(paygData.getStatus(), PaygSshData.Status.E);
        assertEquals(1, UserNotificationFactory.listAllNotificationMessages().size());
        assertEquals(NotificationType.PaygAuthenticationUpdateFailed,
                UserNotificationFactory.listAllNotificationMessages().get(0).getType());
    }

    public void testGenericException() throws Exception {
        CONTEXT.checking(new Expectations() {
            {

                oneOf(paygAuthDataExtractorMock).extractAuthData(with(any(PaygSshData.class)));
                will(throwException(new Exception("My Exception")));
            }});
        PAYG_DATA_TASK.execute(null);
        paygData = HibernateFactory.reload(paygData);
        assertNull(paygData.getErrorMessage());
        assertEquals(paygData.getStatus(), PaygSshData.Status.E);
        assertEquals(1, UserNotificationFactory.listAllNotificationMessages().size());
        assertEquals(NotificationType.PaygAuthenticationUpdateFailed,
                UserNotificationFactory.listAllNotificationMessages().get(0).getType());
    }

    public void testSuccessClearStatus() throws Exception {
        CONTEXT.checking(new Expectations() {
            {
                oneOf(paygAuthDataExtractorMock).extractAuthData(with(any(PaygSshData.class)));
                will(returnValue(paygInstanceInfo));
            }});
        PAYG_DATA_TASK.execute(null);
        paygData = HibernateFactory.reload(paygData);
        assertNull(paygData.getErrorMessage());
        assertEquals(paygData.getStatus(), PaygSshData.Status.S);
        assertEquals(0, UserNotificationFactory.listAllNotificationMessages().size());
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
        return paygSshData;
    }

    private PaygInstanceInfo createPaygInstanceInfo() {
        List<PaygProductInfo> products = new LinkedList<>();
        products.add(new PaygProductInfo("SLES", "15.2", "x86_64"));
        products.add(new PaygProductInfo("sle-module-basesystem", "15.2", "x86_64"));

        Map<String, String> basicAuth = new HashMap<>();
        basicAuth.put("username", "SCC_05c394f");
        basicAuth.put("password", "0e248802");

        String headerAuth = "X-Instance-Data:PGRvY3VtZW50PnsKICAiYWNjb3VudElkIiA6ICI2NDEwODAwN";

        Map<String, String> rmtHost = new HashMap<>();
        rmtHost.put("hostname", "smt-ec2.susecloud.net");
        rmtHost.put("ip", "18.156.40.199");
        rmtHost.put("server_ca", "-----BEGIN CERTIFICATE-----");

        PaygInstanceInfo info = new PaygInstanceInfo(products, basicAuth, headerAuth, rmtHost);
        return info;
    }
}
