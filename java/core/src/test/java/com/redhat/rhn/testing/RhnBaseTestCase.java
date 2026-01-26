/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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
package com.redhat.rhn.testing;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.MessageQueue;

import com.suse.manager.webui.services.test.TestSaltApi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Date;

/**
 * RhnBaseTestCase is the base class for all RHN TestCases.
 * It ensures that the HibernateSession is closed after each
 * test to simulate what happens when the code is run
 * in a web application server.
 */
public abstract class RhnBaseTestCase implements SaltTestCaseUtils  {

    protected Path tmpSaltRoot;

    /**
     * Default Constructor
     */
    public RhnBaseTestCase() {
        MessageQueue.configureDefaultActions(new TestSaltApi());
    }

    /**
     * Called once per test method.
     * @throws Exception if an error occurs during setup.
     */
    @BeforeEach
    protected void setUp() throws Exception {
        tmpSaltRoot = setupSaltConfigurationForTests();
    }

    /**
     * Tears down the fixture, and closes the HibernateSession.
     * @see HibernateFactory#closeSession()
     */
    @AfterEach
    public void tearDown() throws Exception {
        TestCaseHelper.tearDownHelper();

        cleanupSaltConfiguration(tmpSaltRoot);
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
            throw new RhnRuntimeException("Sleep interrupted", e);
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
        assertTrue(coll.contains(elem));
    }

    /**
     * Assert that <code>coll</code> does not contain <code>elem</code>
     * @param <A> element type
     * @param coll a collection
     * @param elem the element that should not be in the collection
     */
    public static <A> void assertNotContains(Collection<A> coll, A elem) {
        assertFalse(coll.contains(elem));
    }

    /**
     * Assert that <code>coll</code> is not empty
     * @param coll the collection
     */
    public static void assertNotEmpty(Collection<?> coll) {
        assertNotEmpty(null, coll);
    }

    /**
     * Assert that <code>coll</code> is not empty
     * @param msg the message to print if the assertion fails
     * @param coll the collection
     */
    public static void assertNotEmpty(String msg, Collection<?> coll) {
        assertNotNull(coll);
        if (coll.isEmpty()) {
            fail(msg);
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
                throw new RhnRuntimeException(error);
            }
        }

        if (!dir.exists() && !dir.mkdirs()) {
            throw new RhnRuntimeException(error);
        }
    }
}
