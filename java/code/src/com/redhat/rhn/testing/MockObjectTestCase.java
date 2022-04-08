/*
 * Copyright (c) 2020 SUSE LLC
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
import org.jmock.States;
import org.jmock.api.Imposteriser;
import org.jmock.internal.ExpectationBuilder;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * jMock boilerplate.
 */
@ExtendWith(JUnit5Mockery.class)
public class MockObjectTestCase {

    @RegisterExtension
    protected final JUnit5Mockery context = new JUnit5Mockery();

    /**
     * @return the mock context
     */
    public Mockery context() {
        return context;
    }

    /**
     * @param imposteriser Imposteriser to use for mocks
     */
    public void setImposteriser(Imposteriser imposteriser) {
        context.setImposteriser(imposteriser);
    }

    /**
     * @param expectations expectations to set to the mocks
     */
    public void checking(ExpectationBuilder expectations) {
        context.checking(expectations);
    }

    /**
     * Mock a class
     *
     * @param typeToMock class of the type to mock
     * @param name the name of the mock object in failures
     * @param <T> the type to mock
     * @return the mocked object
     */
    public <T> T mock(Class<T> typeToMock, String name) {
        return context.mock(typeToMock, name);
    }

    /**
     * Mock a class
     *
     * @param typeToMock class of the type to mock
     * @param <T> the type to mock
     * @return the mocked object
     */
    public <T> T mock(Class<T> typeToMock) {
        return context.mock(typeToMock);
    }

    /**
     * State machine used for expectations order
     * @param name the name of the state machine
     * @return the state machine
     */
    public States states(String name) {
        return context.states(name);
    }

}
