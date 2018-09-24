/**
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
package com.redhat.rhn.taskomatic.task.test;

import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.system.SystemHandler;
import com.redhat.rhn.frontend.xmlrpc.system.test.SystemHandlerTestUtils;
import com.redhat.rhn.taskomatic.task.DailySummary;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;
import com.suse.manager.utils.MailHelper;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.stringContainsInOrder;

/**
 * DailySummaryTest
 * @version $Rev$
 */
public class DailySummaryTest extends JMockBaseTestCaseWithUser {

    private SystemHandler handler = new SystemHandler();
    private SystemHandlerTestUtils shtu = new SystemHandlerTestUtils();
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);
    }

    public void testDequeueOrg() {
        WriteMode clear = ModeFactory.getWriteMode("test_queries",
            "delete_from_daily_summary_queue");
        clear.executeUpdate(new HashMap());

        DailySummary ds = new DailySummary();
        Long oid = UserTestUtils.createOrg("testOrg" + this.getClass().getSimpleName());
        assertNotNull(oid);
        int rows = ds.dequeueOrg(oid);
        assertEquals(0, rows);

        WriteMode m = ModeFactory.getWriteMode("test_queries",
                "insert_into_daily_summary_queue");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("org_id", oid);
        rows = m.executeUpdate(params);
        assertEquals(1, rows);
        rows = ds.dequeueOrg(oid);
        assertEquals(1, rows);
    }

    public void testGetAwolServers() {
        return;
    }

    public void testGetActionInfo() {
        return;
    }

    public void aTestRenderAwolServersMessage() {
        return;
    }

    public void aTestPrepareEmail() {
        return;
    }

    public void aTestRenderActionMessage() {
        return;
    }

    public void testQueueOrgEmails() throws Exception {
        // set up an AWOL system beloning to one Org, accessible by one User
        Long oid = UserTestUtils.createOrg("testOrg" + this.getClass().getSimpleName());
        User user = UserTestUtils.createUser("test", oid);
        user.setEmailNotify(1);
        user.setTaskoNotify(true);
        WriteMode m = ModeFactory.getWriteMode("test_queries",
                "insert_into_daily_summary_queue");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("org_id", oid);
        m.executeUpdate(params);
        user.setEmailNotify(1);
        Server s = ServerTestUtils.createTestSystem(user);
        Server ms = MinionServerFactoryTest.createTestMinionServer(user);
        s.getServerInfo().setCheckin(new Date(System.currentTimeMillis()- 2 * 24 * 60 * 60 * 1000));
        Date scheduleDate = new Date();
        WriteMode fm = ModeFactory.getWriteMode("test_queries",
                "create_actions_dailysummary");
        Map<String, Object> fparams = new HashMap<String, Object>();
        fparams.put("org_id", oid);
        fparams.put("server_id", s.getId());
        fm.executeUpdate(fparams);

        SystemHandler mh =  shtu.getMockedHandler();
        mh.scheduleApplyHighstate(user, ms.getId().intValue(), scheduleDate, false);
        mh.scheduleApplyHighstate(user, ms.getId().intValue(), scheduleDate, true);

        TestUtils.saveAndFlush(ms);
        TestUtils.saveAndFlush(s);
        HibernateFactory.getSession().flush();
        HibernateFactory.commitTransaction();

        // set up a mock for the mailer
        MailHelper mailHelper = mock(MailHelper.class);

        // set expectations on the mail sending method
        context().checking(new Expectations() { {
            exactly(1).of(mailHelper)
                    .sendEmail(with(any(String.class)),
                            with(any(String.class)),
                            with(stringContainsInOrder(Arrays.asList("Apply highstate in test-mode              1",
                                                                     "Apply states (total)                      2",
                                                                     "Service Pack Migration (Dry Run)          1",
                                                                     "Service Pack Migration (Total)            2"))));
        } });

        // run test
        DailySummary ds = new DailySummary();
        ds.queueOrgEmails(oid, () -> mailHelper);
    }

    public void aTestExcecute() {
        // using jesusr_redhat orgid for this test.  Run only on hosted.
        // TODO: how do we create good test data for something like this?
        return;
    }
}
