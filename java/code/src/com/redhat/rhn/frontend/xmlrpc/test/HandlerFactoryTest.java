/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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

package com.redhat.rhn.frontend.xmlrpc.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.HandlerFactory;
import com.redhat.rhn.frontend.xmlrpc.channel.ChannelHandler;
import com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HandlerFactoryTest extends RhnBaseTestCase {
    private HandlerFactory factory = null;

    @BeforeEach
    public void setUp() throws Exception {
        factory = HandlerFactory.getDefaultHandlerFactory();
    }

    @Test
    public void testHandlerFactoryNotFound() {
        assertTrue(factory.getHandler("NoHandler").isEmpty(), "handler should not exist.");
    }

    @Test
    public void testHandlerFactory() {
        BaseHandler handler = factory.getHandler("channel").get();
        assertEquals(ChannelHandler.class, handler.getClass());
    }

    @Test
    public void testDescendingClass() {
        BaseHandler handler = factory.getHandler("channel.software").get();
        assertNotNull(handler);
        assertEquals(ChannelSoftwareHandler.class, handler.getClass());
    }
}
