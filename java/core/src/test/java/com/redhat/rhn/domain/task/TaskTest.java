/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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
package com.redhat.rhn.domain.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.hibernate.Session;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * TaskTest
 */
public class TaskTest extends RhnBaseTestCase {

    @Test
    public void testTask() {

        Org org = UserTestUtils.createOrg(this);
        String testname = "task_object_unit_test_" + TestUtils.randomString();
        Long testdata = 42L;
        Task t = TaskFactory.createTask(org, testname, testdata);

        flushAndEvict(t);

        //look the sucker back up
        Session session = HibernateFactory.getSession();
        Task t2 = TaskFactory.lookup(org, testname, testdata);
        // need to flush and evict t2 here otherwise
        // the TaskFactory.lookup() down below will return the
        // SAME reference and cause the equals to fail.
        flushAndEvict(t2);

        assertNotNull(t2);
        assertEquals(testname, t2.getName());
        assertEquals(testdata, t2.getData());
        assertEquals(0, t.getPriority());

        Task t3 = null;
        assertNotEquals(t2, t3);
        assertNotEquals(t2, session);
        t3 = TaskFactory.lookup(org, testname, testdata);

        assertEquals(t2, t3);
        t3.setName("foo");
        assertNotEquals(t2, t3, "t2 should not be equal to t3");
    }

    @Test
    public void testLookupNameLike() {
        Org org = UserTestUtils.createOrg(this);
        String testname = "task_object_unit_test_" + TestUtils.randomString();
        Long testdata = 42L;
        TaskFactory.createTask(org, testname, testdata);

        List lookedup = TaskFactory.getTaskListByNameLike("task_object_unit_test_");
        assertNotNull(lookedup);
        assertFalse(lookedup.isEmpty());
        assertNotNull(lookedup.get(0));
        assertInstanceOf(Task.class, lookedup.get(0));
    }
}
