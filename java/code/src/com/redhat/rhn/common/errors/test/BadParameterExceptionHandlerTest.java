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

import static org.junit.Assert.assertNotNull;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.errors.BadParameterExceptionHandler;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.session.WebSession;
import com.redhat.rhn.frontend.action.common.BadParameterException;
import com.redhat.rhn.frontend.events.TraceBackAction;
import com.redhat.rhn.frontend.events.TraceBackEvent;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
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

/**
 * BadParameterExceptionHandlerTest
 */
public class BadParameterExceptionHandlerTest {

    private TraceBackAction tba;
    private Mockery context;

    @BeforeEach
    public void setUp() {
        context = new Mockery() {{
            setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        }};
        tba = new TraceBackAction();
        MessageQueue.registerAction(tba, TraceBackEvent.class);
        MessageQueue.startMessaging();
    }

    @Test
    public void testExecuteHandlesBadParameterException() throws Exception {
        Logger log = LogManager.getLogger(BadParameterExceptionHandler.class);
        Level origLevel = log.getLevel();
        Configurator.setLevel(this.getClass().getName(), Level.OFF);

        Config c = Config.get();
        String mail = c.getString("web.traceback_mail");

        try {
            c.setString("web.traceback_mail", "jesusr@redhat.com");

            BadParameterException ex = new BadParameterException("Invalid test parameter");

            final ActionMapping mapping = context.mock(ActionMapping.class);
            final HttpServletRequest request = context.mock(HttpServletRequest.class);
            final HttpServletResponse response = context.mock(HttpServletResponse.class);
            final WebSession webSession = context.mock(WebSession.class);

            context.checking(new Expectations() {{
                oneOf(mapping).getInputForward();
                will(returnValue(new ActionForward()));

                oneOf(request).getAttribute("session");
                will(returnValue(webSession));

                allowing(webSession).getWebUserId();
                will(returnValue(123L));

                oneOf(request).setAttribute("error", ex);
                allowing(response).setStatus(with(any(Integer.class)));

                allowing(request); // allow other harmless calls
            }});

            BadParameterExceptionHandler handler = new BadParameterExceptionHandler();
            handler.execute(ex, new ExceptionConfig(), mapping, null, request, response);

            assertNotNull(request.getAttribute("error"));
        }
        finally {
            // Restore config and logging
            Thread.sleep(1000);
            if (mail != null) {
                c.setString("web.traceback_mail", mail);
            }
            Configurator.setLevel(this.getClass().getName(), origLevel);
        }
    }

    @AfterEach
    public void tearDown() {
        MessageQueue.stopMessaging();
        MessageQueue.deRegisterAction(tba, TraceBackEvent.class);
    }
}
