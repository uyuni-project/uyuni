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

package com.suse.proxy.test;

import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.assertExpectedErrors;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.proxy.update.ProxyConfigUpdateApplySaltState;
import com.suse.proxy.update.ProxyConfigUpdateContext;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.utils.Xor;

import org.jmock.Expectations;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Tests for ProxyConfigUpdateApplySaltState
 */
@SuppressWarnings("java:S3599")
public class ProxyConfigUpdateApplySaltStateTest extends BaseTestCaseWithUser {
    private static final String[] DEFAULT_EXPECTED_ERROR_MESSAGES = {"Failed to apply proxy configuration salt state."};
    private final ProxyConfigUpdateApplySaltState handler = new ProxyConfigUpdateApplySaltState();
    private SaltApi mockSaltApi;


    @SuppressWarnings({"java:S1171"})
    @RegisterExtension
    protected final JUnit5Mockery context = new JUnit5Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    /**
     * Helper method to create a State.ApplyResult object with the given result
     *
     * @param result the result to set
     * @return the State.ApplyResult object
     * @throws NoSuchFieldException   if the field is not found
     * @throws IllegalAccessException if the field is not accessible
     */
    private static State.ApplyResult getStateApplyResult(boolean result)
            throws NoSuchFieldException, IllegalAccessException {
        State.ApplyResult applyResult = new State.ApplyResult();
        Field resultField = State.ApplyResult.class.getSuperclass().getDeclaredField("result");
        resultField.setAccessible(true);
        resultField.set(applyResult, result);
        return applyResult;
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        this.mockSaltApi = context.mock(SaltApi.class);
    }

    /**
     * Test the handle method when all states are successfully applied
     */
    @Test
    public void handleSuccessWithMultipleStates() throws NoSuchFieldException, IllegalAccessException {
        Map<String, State.ApplyResult> applyResults = new HashMap<>();
        applyResults.put("state1", getStateApplyResult(true));
        applyResults.put("state2", getStateApplyResult(true));
        applyResults.put("state3", getStateApplyResult(true));

        ProxyConfigUpdateContext proxyConfigUpdateContext = getProxyConfigUpdateContext();

        context.checking(new Expectations() {{
            allowing(mockSaltApi).callSync(with(any(LocalCall.class)), with(any(String.class)));
            will(returnValue(Optional.of(Xor.right(applyResults))));
        }});

        handler.handle(proxyConfigUpdateContext);
        assertFalse(proxyConfigUpdateContext.getErrorReport().hasErrors());
    }

    /**
     * Test the handle method when one state fails to apply
     */
    @Test
    public void handleFailureWithMultipleStatesWhenOneFails() throws NoSuchFieldException, IllegalAccessException {
        Map<String, State.ApplyResult> applyResults = new HashMap<>();
        applyResults.put("state1", getStateApplyResult(true));
        applyResults.put("state2", getStateApplyResult(false));
        applyResults.put("state3", getStateApplyResult(true));

        ProxyConfigUpdateContext proxyConfigUpdateContext = getProxyConfigUpdateContext();

        context.checking(new Expectations() {{
            allowing(mockSaltApi).callSync(with(any(LocalCall.class)), with(any(String.class)));
            will(returnValue(Optional.of(Xor.right(applyResults))));
        }});

        handler.handle(proxyConfigUpdateContext);
        assertExpectedErrors(DEFAULT_EXPECTED_ERROR_MESSAGES, proxyConfigUpdateContext);
    }

    /**
     * Test the handle method when no states are returned
     */
    @Test
    public void handleFailureWhenNoStateReturns() {

        ProxyConfigUpdateContext proxyConfigUpdateContext = getProxyConfigUpdateContext();

        context.checking(new Expectations() {{
            allowing(mockSaltApi).callSync(with(any(LocalCall.class)), with(any(String.class)));
            will(returnValue(Optional.of(Xor.right(Collections.emptyMap()))));
        }});

        handler.handle(proxyConfigUpdateContext);
        assertExpectedErrors(DEFAULT_EXPECTED_ERROR_MESSAGES, proxyConfigUpdateContext);
    }

    @Test
    public void handleFailureWhenCallSyncFails() {
        final String[] expectedErrorMessages = {"dummy error occurred"};

        ProxyConfigUpdateContext proxyConfigUpdateContext = getProxyConfigUpdateContext();

        final String errorMessage = "dummy error occurred";
        context.checking(new Expectations() {{
            allowing(mockSaltApi).callSync(with(any(LocalCall.class)), with(any(String.class)));
            will(returnValue(Optional.of(Xor.left(errorMessage))));
        }});

        handler.handle(proxyConfigUpdateContext);
        assertExpectedErrors(expectedErrorMessages, proxyConfigUpdateContext);
    }

    @Test
    public void handleFailureWhenCallSyncReturnsEmpty() {
        ProxyConfigUpdateContext proxyConfigUpdateContext = getProxyConfigUpdateContext();

        context.checking(new Expectations() {{
            allowing(mockSaltApi).callSync(with(any(LocalCall.class)), with(any(String.class)));
            will(returnValue(Optional.empty()));
        }});

        handler.handle(proxyConfigUpdateContext);
        assertExpectedErrors(DEFAULT_EXPECTED_ERROR_MESSAGES, proxyConfigUpdateContext);
    }

    /**
     * Helper method to create a common ProxyConfigUpdateContext
     *
     * @return the ProxyConfigUpdateContext
     */
    private ProxyConfigUpdateContext getProxyConfigUpdateContext() {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);

        ProxyConfigUpdateContext proxyConfigUpdateContext =
                new ProxyConfigUpdateContext(null, null, mockSaltApi, user);
        proxyConfigUpdateContext.setProxyConfigFiles(new HashMap<>());
        proxyConfigUpdateContext.setProxyMinion(minion);
        return proxyConfigUpdateContext;
    }

}
