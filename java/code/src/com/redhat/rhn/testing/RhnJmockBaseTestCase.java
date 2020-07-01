/**
 * Copyright (c) 2015 SUSE LLC
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

import org.jmock.Mockery;
import org.jmock.api.Imposteriser;

import junit.framework.TestCase;

/**
 * RhnJmockBaseTestCase - This is the same thing as {@link RhnBaseTestCase}
 * but it encapsulates a JMock context
 */
public abstract class RhnJmockBaseTestCase extends TestCase {
    protected Mockery context = new Mockery();

    public RhnJmockBaseTestCase() {
    }

    /**
     * @param name The test case name
     */
    public RhnJmockBaseTestCase(String name) {
        super(name);
    }

    /**
     * Called once per test method.
     *
     * @throws Exception if an error occurs during test setup
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Called once per test method to clean up.
     *
     * @throws Exception if an error occurs during tear down
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        TestCaseHelper.tearDownHelper();
    }

    protected Mockery context() {
        return context;
    }

    protected void setImposteriser(Imposteriser imposteriser) {
        context.setImposteriser(imposteriser);
    }

    protected <T> T mock(Class<T> typeToMock) {
        return context.mock(typeToMock);
    }

    protected <T> T mock(Class<T> typeToMock, String name) {
        return context.mock(typeToMock, name);
    }

    protected void verify() {
        context.assertIsSatisfied();
    }
}
