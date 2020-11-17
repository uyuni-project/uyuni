/**
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
import org.jmock.api.MockObjectNamingScheme;
import org.jmock.internal.ExpectationBuilder;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * jMock boilerplate.
 */
@ExtendWith(JUnit5Mockery.class)
public class MockObjectTestCase {

    @RegisterExtension
    final protected JUnit5Mockery context = new JUnit5Mockery();

    @BeforeEach
    public void setUp() throws Exception {
        // todo maybe remove
    }

    @AfterEach
    public void tearDown() throws Exception {
        // todo maybe remove
    }

    public Mockery context() {
        return context;
    }

    public void setDefaultResultForType(Class<?> type, Object result) {
        context.setDefaultResultForType(type, result);
    }

    public void setImposteriser(Imposteriser imposteriser) {
        context.setImposteriser(imposteriser);
    }

    public void setNamingScheme(MockObjectNamingScheme namingScheme) {
        context.setNamingScheme(namingScheme);
    }

    public void checking(ExpectationBuilder expectations) {
        context.checking(expectations);
    }

    public <T> T mock(Class<T> typeToMock, String name) {
        return context.mock(typeToMock, name);
    }


    public <T> T mock(Class<T> typeToMock) {
        return context.mock(typeToMock);
    }

    public States states(String name) {
        return context.states(name);
    }

}
