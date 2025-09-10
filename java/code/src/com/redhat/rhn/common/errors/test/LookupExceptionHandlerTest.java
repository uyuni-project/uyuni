/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.common.errors.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.errors.LookupExceptionHandler;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.frontend.events.TraceBackAction;
import com.redhat.rhn.frontend.events.TraceBackEvent;
import com.redhat.rhn.testing.MockObjectTestCase;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.TestUtils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.config.ExceptionConfig;
import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Vector;

/**
 * LookupExceptionHandlerTest
 */
public class LookupExceptionHandlerTest extends MockObjectTestCase {

    private TraceBackAction tba;

    @BeforeEach
    public void setUp() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        tba = new TraceBackAction();
        MessageQueue.registerAction(tba, TraceBackEvent.class);
        MessageQueue.startMessaging();
    }

    @Test
    public void testExecute() throws Exception {

        /*
         * Turn off logging and tracebacks
         * Logging complains and sends warnings (expected)
         * Tracebacks will get sent to root@localhost
         */
        Logger log = LogManager.getLogger(LookupExceptionHandler.class);
        Level orig = log.getLevel();
        Config c = Config.get();
        String mail = c.getString("web.traceback_mail", "");
        try {
            Configurator.setLevel(this.getClass().getName(), Level.OFF);
            c.setString("web.traceback_mail", "jesusr@redhat.com");

            LookupException ex = new LookupException("Simply a test");

            final ActionMapping mapping = mock(ActionMapping.class, "mapping");
            context().checking(new Expectations() { {
                oneOf(mapping).getInputForward();
                will(returnValue(new ActionForward()));
            } });

            RhnMockHttpServletRequest request = TestUtils
                    .getRequestWithSessionAndUser();
            request.setMethod("POST");
            request.setRequestURI("http://localhost:8080");
            request.setParameterNames(new Vector<String>().elements());

            RhnMockHttpServletResponse response = new RhnMockHttpServletResponse();
            DynaActionForm form = new DynaActionForm();

            LookupExceptionHandler leh = new LookupExceptionHandler();

            leh.execute(ex, new ExceptionConfig(), mapping, form, request, response);
            assertEquals(ex, request.getAttribute("error"));
        }
        finally {
            //Turn tracebacks and logging back on
            Thread.sleep(1000); //wait for message to be sent
            c.setString("web.traceback_mail", mail);
            Configurator.setLevel(this.getClass().getName(), orig);
        }
    }

    @AfterEach
    public void tearDown() {
        MessageQueue.stopMessaging();
        MessageQueue.deRegisterAction(tba, TraceBackEvent.class);
    }

}
