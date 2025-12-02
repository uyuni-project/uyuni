/*
 * Copyright (c) 2025 SUSE LLC
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

package com.suse.proxy.update;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.UyuniGeneralException;
import com.redhat.rhn.testing.MockObjectTestCase;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * Tests for the ProxyConfigUpdate class
 */
public class ProxyConfigUpdateFacadeImplTest extends MockObjectTestCase {

    /**
     * Tests the success case when all handlers are successful
     */
    @Test
    public void testSuccessWhenUpdate() throws NoSuchFieldException, IllegalAccessException {
        ProxyConfigUpdateContextHandler okHandler1 = new OkHandler();
        ProxyConfigUpdateContextHandler okHandler2 = new OkHandler();
        ProxyConfigUpdateContextHandler okHandler3 = new OkHandler();

        ProxyConfigUpdateFacadeImpl proxyConfigUpdate = new ProxyConfigUpdateFacadeImpl();
        replaceHandlers(proxyConfigUpdate, List.of(okHandler1, okHandler2, okHandler3));

        proxyConfigUpdate.update(null, null, null);
    }

    /**
     * Tests a scenario where there are 3 handlers.
     * The first one is successful;
     * The second one registers with 3 errors;
     * The third one registers with 2 errors.
     * It is expected that the process halts after the second handler and the errors are reported in a
     * {@link UyuniGeneralException} with the expected error messages.
     */
    @Test
    public void testFailWhenUpdateWithError() throws NoSuchFieldException, IllegalAccessException {
        final String[] expectedErrorMessages = {"oops", "three", "errors?"};

        ProxyConfigUpdateContextHandler okHandler = new OkHandler();
        ProxyConfigUpdateContextHandler failingHandler = new FailingHandler();
        ProxyConfigUpdateContextHandler anotherFailingHandler = new AnotherFailingHandler();

        ProxyConfigUpdateFacadeImpl proxyConfigUpdate = new ProxyConfigUpdateFacadeImpl();
        replaceHandlers(proxyConfigUpdate, List.of(okHandler, failingHandler, anotherFailingHandler));

        try {
            proxyConfigUpdate.update(null, null, null);
            fail("Expected UyuniGeneralException to be thrown");
        }
        catch (UyuniGeneralException e) {
            Assertions.assertEquals(expectedErrorMessages.length, e.getErrorMessages().length);
            for (String expectedErrorMessage : expectedErrorMessages) {
                assertTrue(Arrays.asList(e.getErrorMessages()).contains(expectedErrorMessage));
            }
        }
    }

    /**
     * A handler that does nothing, mocking a valid step
     */
    public static class OkHandler implements ProxyConfigUpdateContextHandler {
        @Override
        public void handle(ProxyConfigUpdateContext context) {
            // does nothing
        }
    }

    /**
     * A handler that registers three errors, mocking an invalid step
     */
    public static class FailingHandler implements ProxyConfigUpdateContextHandler {
        @Override
        public void handle(ProxyConfigUpdateContext context) {
            context.getErrorReport().register("oops");
            context.getErrorReport().register("three");
            context.getErrorReport().register("errors?");
        }
    }

    /**
     * Another handler that registers errors, mocking an invalid step
     */
    public static class AnotherFailingHandler implements ProxyConfigUpdateContextHandler {
        @Override
        public void handle(ProxyConfigUpdateContext context) {
            context.getErrorReport().register("more");
            context.getErrorReport().register("errors!");
        }
    }

    /**
     * Replaces the handlers in the ProxyConfigUpdate chain of responsibility private field
     * @param proxyConfigUpdate the ProxyConfigUpdate instance
     * @param handlers the new handlers
     * @throws NoSuchFieldException if the field is not found
     * @throws IllegalAccessException if the field cannot be accessed
     */
    @SuppressWarnings("squid:S3011")
    private void replaceHandlers(
            ProxyConfigUpdateFacadeImpl proxyConfigUpdate,
            List<ProxyConfigUpdateContextHandler> handlers
    ) throws NoSuchFieldException, IllegalAccessException {
        Field field = ProxyConfigUpdateFacadeImpl.class.getDeclaredField("contextHandlerChain");
        field.setAccessible(true);
        field.set(proxyConfigUpdate, handlers);
    }
}
