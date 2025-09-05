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
package com.redhat.rhn.frontend.servlets.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.frontend.servlets.SessionFilter;
import com.redhat.rhn.testing.MockFilterChain;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * AuthFilterTest
 */
public class SessionFilterDeadlockTest extends BaseFilterTst {

    @Test
    public void testDeadlockFilter() throws Exception {
        // Make sure the chain blows up.
        chain = new MockFilterChain() {
            @Override
            public void doFilter(ServletRequest req, ServletResponse resp)
            throws IOException {
                throw new IOException("Test IOException");
            }
        };
        SessionFilter filter = new SessionFilter();
        HibernateFactory.getSession();
        int caughtCount = 0;

        Logger log = LogManager.getLogger(SessionFilter.class);
        Level orig = log.getLevel();
        Configurator.setLevel(this.getClass().getName(), Level.OFF);
        for (int i = 0; i < 5; i++) {
            try {
                filter.doFilter(request, response, chain);
            }
            catch (IOException ioe) {
                caughtCount++;
            }
        }
        Configurator.setLevel(this.getClass().getName(), orig);
        assertEquals(5, caughtCount);
        HibernateFactory.getSession();
        assertTrue(HibernateFactory.inTransaction());
    }



}

