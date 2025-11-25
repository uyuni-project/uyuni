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
 * SPDX-License-Identifier: GPL-2.0-only
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

    private static Logger log = LogManager.getLogger(TestFactory.class);
    private static TestFactory singleton = new TestFactory();

    private TestFactory() {
        super();
    }

    /**
     * Return the Implementation class used by the derived
     * class's Factory
     * @return the implementation class
     */
    protected Class getImplementationClass() {
        return TestImpl.class;
    }

    /** Get the Logger for the derived class so log messages
    *   show up on the correct class
    */
    @Override
    protected Logger getLogger() {
        return log;
    }

    public static TestInterface createTest() {
        return new TestImpl();
    }

    public static TestInterface lookupByFoobar(String f) {
        // Get PersonalInfo row
        return singleton.lookupObjectByParam(TestImpl.class, "fooBar", f);
    }

    public static List<TestInterface> lookupAll() {
        Session session = HibernateFactory.getSession();
        return session.createQuery("FROM TestImpl", TestInterface.class)
                .list();
    }

    public static void save(TestInterface t) {
        singleton.saveObject(t);
    }

    public static Session getSession() {
        setConnectionManager(new TestConnectionManager());
        return HibernateFactory.getSession();
    }
}
