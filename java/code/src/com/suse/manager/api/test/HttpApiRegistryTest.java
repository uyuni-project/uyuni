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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import spark.Route;

public class HttpApiRegistryTest extends RhnJmockBaseTestCase {
    /**
     * Stub API handler for testing
     */
    public static class RegistryTestHandler extends BaseHandler {
        private void notExposed() {
            // Intentionally empty: Test helper method; Presence is required for registry discovery tests.
        }
        @ReadOnly public void myFirstEndpoint() {
            // Intentionally empty: Test endpoint stub; Route Registration is validated by expectations.
        }
        public void mySecondEndpoint() {
            // Intentionally empty: Second test endpoint stub; Behavior is validated elsewhere.
        }
        @ReadOnly @ApiIgnore public void ignored() {
            // Intentionally empty: Ignored endpoint used to verify ApiIgnore handling.
        }
        @ApiIgnore(ApiType.HTTP) public void alsoIgnored() {
            // Intentionally empty: Ignored for HTTP; Ensures only expected routes are registered.
        }
        @ApiIgnore(ApiType.XMLRPC) public void notIgnored() {
            // Intentionally empty: Not ignored for HTTP; Included to assert correct registration.
        }
    }

    @BeforeEach
    public void setUp() {
        context().setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    /**
     * Tests creation of correct Spark GET or POST requests according to handler contents
     */
    @Test
    public void testInitRoutes() {
        RouteFactory routeFactory = new RouteFactory(RouteFactoryTest.createTestSerializerFactory());
        HandlerFactory handlerFactory = new HandlerFactory();
        handlerFactory.addHandler("test.path", new RegistryTestHandler());

        SparkRegistrationHelper helper = context().mock(SparkRegistrationHelper.class);
        HttpApiRegistry registry = new HttpApiRegistry(handlerFactory, routeFactory, helper);

        context().checking(new Expectations() {{
            // Auth routes are added in all cases
            oneOf(helper).addPostRoute(with("/manager/api/auth/login"), with(any(Route.class)));
            oneOf(helper).addPostRoute(with("/manager/api/auth/logout"), with(any(Route.class)));
            oneOf(helper).addGetRoute(with("/manager/api/auth/logout"), with(any(Route.class)));

            // Test routes
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
