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
import com.redhat.rhn.common.errors.PermissionExceptionHandler;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.session.WebSession;
import com.redhat.rhn.frontend.events.TraceBackAction;
import com.redhat.rhn.frontend.events.TraceBackEvent;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.config.ExceptionConfig;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PermissionExceptionHandlerTest {

    private final Mockery context = new JUnit5Mockery();
    private TraceBackAction tba;

    @BeforeEach
    void setUp() {
        context.setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
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
    void testExecuteSetsErrorAttributeWithoutSession() throws Exception {
        // Turn off logging
        Logger log = LogManager.getLogger(PermissionExceptionHandler.class);
        Level originalLevel = log.getLevel();
        Config config = Config.get();
        String originalMail = config.getString("web.traceback_mail", "");

        try {
            Configurator.setLevel(getClass().getName(), Level.OFF);
            config.setString("web.traceback_mail", "jesusr@redhat.com");

            PermissionException ex = new PermissionException("Simply a test");

            // Mocks
            final ActionMapping mapping = context.mock(ActionMapping.class);
            final HttpServletRequest request = context.mock(HttpServletRequest.class);
            final HttpServletResponse response = context.mock(HttpServletResponse.class);
            final DynaActionForm form = context.mock(DynaActionForm.class);
            final WebSession webSession = context.mock(WebSession.class);

            // Expectations
            context.checking(new Expectations() {{
                oneOf(request).getAttribute("session");
                will(returnValue(webSession));
                allowing(webSession).getWebUserId();
                will(returnValue(123L));
                oneOf(request).setAttribute("error", ex);
                allowing(response).setStatus(with(any(Integer.class)));
                oneOf(mapping).getInputForward();
                will(returnValue(new ActionForward()));
                allowing(request);
            }});

            // Execute handler
            PermissionExceptionHandler handler = new PermissionExceptionHandler();
            handler.execute(ex, new ExceptionConfig(), mapping, form, request, response);

            // Verify
            context.assertIsSatisfied();
        }
        finally {
            Thread.sleep(1000); // wait for async message
            config.setString("web.traceback_mail", originalMail);
            Configurator.setLevel(getClass().getName(), originalLevel);
        }
    }
}
