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
package com.redhat.rhn.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.common.util.Asserts;

import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.manager.webui.services.test.TestSystemQuery;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Date;

/**
 * RhnBaseTestCase is the base class for all RHN TestCases.
 * It ensures that the HibernateSession is closed after each
 * test to similuate what happens when the code is run
 * in a web application server.
 */
public abstract class RhnBaseTestCase  {

    /**
     * Default Constructor
     */
    public RhnBaseTestCase() {
        MessageQueue.configureDefaultActions(new TestSystemQuery(), new TestSaltApi());
    }

    /**
     * Called once per test method.
     * @throws Exception if an error occurs during setup.
     */
    @BeforeEach
    protected void setUp() throws Exception {
        SaltStateGeneratorService.INSTANCE.setSkipSetOwner(true);
    }

    /**
     * Tears down the fixture, and closes the HibernateSession.
     * @see HibernateFactory#closeSession()
     */
    @AfterEach
    public void tearDown() throws Exception {
        TestCaseHelper.tearDownHelper();
    }

    /**
     * PLEASE Refrain from using this unless you really have to.
     *
     * Try clearSession() instead
     * @throws HibernateException hibernate exception
     */
    protected void commitAndCloseSession() throws HibernateException {
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();
    }

    protected void flushAndEvict(Object obj) throws HibernateException {
        Session session = HibernateFactory.getSession();
        session.flush();
        session.evict(obj);
    }

    protected <T> T reload(Class<T> objClass, Serializable id) throws HibernateException {
        assertNotNull(id);
        T obj = TestUtils.reload(objClass, id);
        return reload(obj);
    }

    /**
     * Reload a Hibernate entity.
     * @param obj the entity to reload
     * @param <T> type of object to reload
     * @return the new instance
     * @throws HibernateException in case of error
     */
    public static <T> T reload(T obj) throws HibernateException {
        assertNotNull(obj);
        T result = TestUtils.reload(obj);
        assertNotSame(obj, result);
        return result;
    }

    /**
     * Get a date representing "now" and wait for one second to
     * ensure that future attempts to get a date will use a date
     * that is definitely later.
     *
     * @return a date representing now
     */
    protected Date getNow() {
        Date now = new Date();
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            throw new RuntimeException("Sleep interrupted", e);
        }
        return now;
    }

    //
    // Utility methods for assertions
    //

    /**
     * Assert that <code>coll</code> contains <code>elem</code>
     * @param <A> element type
     * @param coll a collection
     * @param elem the element that should be in the collection
     */
    public static <A> void assertContains(Collection<A> coll, A elem) {
        Asserts.assertContains(coll, elem);
    }

    /**
     * Assert that <code>coll</code> is not empty
     * @param coll the collection
     */
    public static void assertNotEmpty(Collection coll) {
        assertNotEmpty(null, coll);
    }

    /**
     * Assert that <code>coll</code> is not empty
     * @param msg the message to print if the assertion fails
     * @param coll the collection
     */
    public static void assertNotEmpty(String msg, Collection coll) {
        assertNotNull(coll);
        if (coll.size() == 0) {
            fail(msg);
        }
    }

    /**
     * Assert that the beans <code>exp</code> and <code>act</code> have the same values
     * for property <code>propName</code>
     *
     * @param propName name of the proeprty to compare
     * @param exp the bean with the expected values
     * @param act the bean with the actual values
     */
    public static void assertPropertyEquals(String propName, Object exp, Object act) {
        assertEquals(getProperty(exp, propName), getProperty(act, propName));
    }

    private static Object getProperty(Object bean, String propName) {
        try {
            return PropertyUtils.getProperty(bean, propName);
        }
        catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Could not get property " + propName +
                    " from " + bean, e);
        }
    }

    /**
     * Assert that <code>fragment</code> is a substring of <code>body</code>
     * @param body the larger string in which to search
     * @param fragment the substring that must be contained in <code>body</code>
     */
    public static void assertContains(String body, String fragment) {
        if (!body.contains(fragment)) {
            fail("The string '" + body + "' must contain '" + fragment + "'");
        }
    }

    /**
     * Assert that <code>fragment</code> is a substring of <code>body</code>
     * @param msg the message to print if the assertion fails
     * @param body the larger string in which to search
     * @param fragment the substring that must be contained in <code>body</code>
     */
    public static void assertContains(String msg, String body, String fragment) {
        if (!body.contains(fragment)) {
            fail(msg);
        }
    }

    protected static void createDirIfNotExists(File dir) {
        String error =
                "Could not create the following directory:[" + dir.getPath() +
                    "] . Please create that directory before proceeding with the tests";
        if (dir.exists() && !dir.isDirectory()) {
            if (!dir.renameTo(new File(dir.getPath() + ".bak")) &&
                         !dir.delete()) {
                throw new RuntimeException(error);
            }
        }

        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException(error);
        }
    }
}
