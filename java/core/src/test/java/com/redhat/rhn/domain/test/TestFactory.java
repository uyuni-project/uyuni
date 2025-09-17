/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/*
 */
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
        return singleton.lookupObjectByNamedQuery("Test.findByFoobar", Map.of("fooBar", f));
    }

    public static List<TestInterface> lookupAll() {
        return singleton.listObjectsByNamedQuery("Test.findAll", Map.of());
    }

    public static void save(TestInterface t) {
        singleton.saveObject(t);
    }

}
