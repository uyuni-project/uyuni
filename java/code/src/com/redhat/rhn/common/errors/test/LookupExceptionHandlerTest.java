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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.errors.LookupExceptionHandler;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.frontend.events.TraceBackAction;
import com.redhat.rhn.frontend.events.TraceBackEvent;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.config.ExceptionConfig;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LookupExceptionHandlerTest {

    private final Mockery context = new Mockery();
    private TraceBackAction tba;
    private ActionMapping mapping;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ActionForm form;

    @BeforeEach
    void setUp() {
        // Allow mocking concrete classes
        context.setImposteriser(ByteBuddyClassImposteriser.INSTANCE);

        // JMock mocks
        mapping = context.mock(ActionMapping.class);
        request = context.mock(HttpServletRequest.class);
        response = context.mock(HttpServletResponse.class);
        form = context.mock(ActionForm.class);

        // Messaging setup
        tba = new TraceBackAction();
        MessageQueue.registerAction(tba, TraceBackEvent.class);
        MessageQueue.startMessaging();
    }

    @AfterEach
    void tearDown() {
        try {
            MessageQueue.stopMessaging();
        }
        finally {
            MessageQueue.deRegisterAction(tba, TraceBackEvent.class);
        }
    }

    @Test
    void testExecuteSetsErrorAttribute() throws Exception {
        Logger log = LogManager.getLogger(LookupExceptionHandler.class);
        Level originalLevel = log.getLevel();
        Config config = Config.get();
        String originalMail = config.getString("web.traceback_mail", "");

        try {
            // Disable logging and set fake mail
            Configurator.setLevel(getClass().getName(), Level.OFF);
            config.setString("web.traceback_mail", "test@example.com");

            LookupException ex = new LookupException("Simply a test");
            LookupExceptionHandler handler = new LookupExceptionHandler();

            // JMock expectations
            context.checking(new Expectations() {{
                oneOf(mapping).getInputForward();
                will(returnValue(new ActionForward()));
                oneOf(request).setAttribute("error", ex);
                allowing(response).setStatus(with(any(Integer.class)));
                allowing(request);
            }});

            // Execute handler
            handler.execute(ex, new ExceptionConfig(), mapping, form, request, response);

            // Verify all expectations
            context.assertIsSatisfied();
        }
        finally {
            // Restore config
            config.setString("web.traceback_mail", originalMail);
            Configurator.setLevel(getClass().getName(), originalLevel);
        }
    }
}
