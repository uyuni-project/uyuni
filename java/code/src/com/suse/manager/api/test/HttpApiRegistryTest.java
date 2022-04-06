/*
 * Copyright (c) 2022 SUSE LLC
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
package com.suse.manager.api.test;

import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.HandlerFactory;
import com.redhat.rhn.testing.RhnJmockBaseTestCase;

import com.suse.manager.api.ApiIgnore;
import com.suse.manager.api.ApiType;
import com.suse.manager.api.HttpApiRegistry;
import com.suse.manager.api.ReadOnly;
import com.suse.manager.api.RouteFactory;
import com.suse.manager.api.SparkRegistrationHelper;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;

import spark.Route;

public class HttpApiRegistryTest extends RhnJmockBaseTestCase {
    /**
     * Stub API handler for testing
     */
    public static class RegistryTestHandler extends BaseHandler {
        private void notExposed() { }
        @ReadOnly public void myFirstEndpoint() { }
        public void mySecondEndpoint() { }
        @ReadOnly @ApiIgnore public void ignored() { }
        @ApiIgnore(ApiType.HTTP) public void alsoIgnored() { }
        @ApiIgnore(ApiType.XMLRPC) public void notIgnored() { }
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        context().setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    /**
     * Tests creation of correct Spark GET or POST requests according to handler contents
     */
    public void testInitRoutes() {
        RouteFactory routeFactory = new RouteFactory(RouteFactoryTest.createTestSerializerFactory());
        HandlerFactory handlerFactory = new HandlerFactory();
        handlerFactory.addHandler("test.path", new RegistryTestHandler());

        SparkRegistrationHelper helper = context().mock(SparkRegistrationHelper.class);
        HttpApiRegistry registry = new HttpApiRegistry(handlerFactory, routeFactory, helper);

        context().checking(new Expectations() {{
            oneOf(helper).addGetRoute(with("/manager/api/test/path/myFirstEndpoint"), with(any(Route.class)));
            oneOf(helper).addPostRoute(with("/manager/api/test/path/mySecondEndpoint"), with(any(Route.class)));
            never(helper).addPostRoute(with("/manager/api/test/path/notExposed"), with(any(Route.class)));
            never(helper).addGetRoute(with("/manager/api/test/path/ignored"), with(any(Route.class)));
            never(helper).addPostRoute(with("/manager/api/test/path/alsoIgnored"), with(any(Route.class)));
            oneOf(helper).addPostRoute(with("/manager/api/test/path/notIgnored"), with(any(Route.class)));
        }});

        registry.initRoutes();
    }
}
