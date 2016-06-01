/**
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
package com.redhat.rhn.frontend.servlets.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.frontend.servlets.SessionFilter;

import org.hibernate.Session;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * SessionFilterTest
 * @version $Rev$
 */
public class SessionFilterTest extends MockObjectTestCase {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    public void setUp() throws IOException, ServletException {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);

        context().checking(new Expectations() { {
            atLeast(1).of(chain).doFilter(request, response);
        } });
    }

    public void testDoFilter() throws Exception {
        SessionFilter filter = new SessionFilter();
        Session sess = HibernateFactory.getSession();
        assertTrue(sess.isOpen());
        assertTrue(sess.isConnected());

        try {
            filter.doFilter(request, response, chain);
            assertFalse(sess.isOpen());
        }
        catch (IOException ioe) {
            // This should never happen ..
            throw new Exception("doFilter() failed ..");
        }
    }
}
