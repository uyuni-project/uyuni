/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
 * Copyright (c) 2025 SUSE LLC
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
package com.redhat.rhn.domain.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.TestConnectionManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.util.List;

public class TestFactory extends HibernateFactory {

    private static final Logger log = LogManager.getLogger(TestFactory.class);
    private static final TestFactory singleton = new TestFactory();

    @Override
    protected Logger getLogger() {
        return log;
    }

    public static TestInterface createTest() {
        return new TestEntity();
    }

    public static TestInterface lookupByFoobar(String f) {
        return singleton.lookupObjectByParam(TestEntity.class, "foobar", f);
    }

    public static List<TestInterface> lookupAll() {
        Session session = HibernateFactory.getSession();
        return session.createQuery("FROM TestEntity", TestInterface.class).list();
    }

    public static TestInterface save(TestInterface t) {
        return singleton.saveObject(t);
    }

    public static Session getSession() {
        setConnectionManager(new TestConnectionManager());
        return HibernateFactory.getSession();
    }
}
