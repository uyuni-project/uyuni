/*
 * Copyright (c) 2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.xmlrpc.iss.test;

import static org.jmock.AbstractExpectations.returnValue;
import static org.jmock.AbstractExpectations.throwException;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.redhat.rhn.frontend.xmlrpc.InvalidTokenException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.frontend.xmlrpc.TokenCreationException;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;

import com.suse.manager.model.hub.HubManager;
import com.suse.manager.webui.utils.token.TokenException;
import com.suse.manager.webui.utils.token.TokenParsingException;
import com.suse.manager.xmlrpc.iss.SyncHandler;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

@ExtendWith(JUnit5Mockery.class)
public class SyncHandlerTest extends BaseHandlerTestCase {

    @RegisterExtension
    protected final JUnit5Mockery context = new JUnit5Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    private HubManager hubManagerMock;

    private SyncHandler syncHandler;

    @BeforeEach
    public void setup() {
        context.setThreadingPolicy(new Synchroniser());
        context.setImposteriser((ByteBuddyClassImposteriser.INSTANCE));

        hubManagerMock = context.mock(HubManager.class);
        syncHandler = new SyncHandler(hubManagerMock);
    }

    @Test
    public void ensureOnlySatAdminCanAccessToTokenGeneration() throws Exception {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock).issueAccessToken("uyuni-server.dev.local");
        expectations.will(returnValue("dummy-token"));
        context.checking(expectations);

        assertThrows(
            PermissionCheckFailureException.class,
            () -> syncHandler.generateAccessToken(regular, "uyuni-server.dev.local")
        );

        assertThrows(
            PermissionCheckFailureException.class,
            () -> syncHandler.generateAccessToken(admin, "uyuni-server.dev.local")
        );

        assertDoesNotThrow(
            () -> syncHandler.generateAccessToken(satAdmin, "uyuni-server.dev.local")
        );
    }

    @Test
    public void throwsCorrectExceptionWhenIssuingFails() throws TokenException {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock).issueAccessToken("uyuni-server.dev.local");
        expectations.will(throwException(new TokenException("unexpected error")));
        context.checking(expectations);

        assertThrows(TokenCreationException.class,
            () -> syncHandler.generateAccessToken(satAdmin, "uyuni-server.dev.local"));
    }

    @Test
    public void ensureOnlySatAdminCanAccessToTokenStorage() throws Exception {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock).storeAccessToken("uyuni-server.dev.local", "dummy-token");
        context.checking(expectations);

        assertThrows(
            PermissionCheckFailureException.class,
            () -> syncHandler.storeAccessToken(regular, "uyuni-server.dev.local", "dummy-token")
        );

        assertThrows(
            PermissionCheckFailureException.class,
            () -> syncHandler.storeAccessToken(admin, "uyuni-server.dev.local", "dummy-token")
        );

        assertDoesNotThrow(
            () -> syncHandler.storeAccessToken(satAdmin, "uyuni-server.dev.local", "dummy-token")
        );
    }

    @Test
    public void throwsCorrectExceptionWhenStoringFails() throws TokenParsingException {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock).storeAccessToken("uyuni-server.dev.local", "dummy-token");
        expectations.will(throwException(new TokenParsingException("Cannot parse")));
        context.checking(expectations);

        assertThrows(InvalidTokenException.class,
            () -> syncHandler.storeAccessToken(satAdmin, "uyuni-server.dev.local", "dummy-token"));
    }
}
