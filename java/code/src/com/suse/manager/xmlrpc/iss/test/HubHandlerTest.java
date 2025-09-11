/*
 * Copyright (c) 2024--2025 SUSE LLC
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
import com.redhat.rhn.frontend.xmlrpc.TokenExchangeFailedException;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;

import com.suse.manager.hub.HubManager;
import com.suse.manager.webui.utils.token.TokenBuildingException;
import com.suse.manager.webui.utils.token.TokenException;
import com.suse.manager.webui.utils.token.TokenParsingException;
import com.suse.manager.xmlrpc.InvalidCertificateException;
import com.suse.manager.xmlrpc.iss.HubHandler;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.security.cert.CertificateException;

@ExtendWith(JUnit5Mockery.class)
public class HubHandlerTest extends BaseHandlerTestCase {

    @RegisterExtension
    protected final JUnit5Mockery context = new JUnit5Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    private HubManager hubManagerMock;

    private HubHandler hubHandler;

    @BeforeEach
    public void setup() {
        context.setThreadingPolicy(new Synchroniser());
        context.setImposteriser((ByteBuddyClassImposteriser.INSTANCE));

        hubManagerMock = context.mock(HubManager.class);
        hubHandler = new HubHandler(hubManagerMock);
    }

    @Test
    public void ensureOnlySatAdminCanAccessToTokenGeneration() throws Exception {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock).issueAccessToken(satAdmin, "uyuni-server.dev.local");
        expectations.will(returnValue("dummy-token"));
        context.checking(expectations);

        assertThrows(
            PermissionCheckFailureException.class,
            () -> hubHandler.generateAccessToken(regular, "uyuni-server.dev.local")
        );

        assertThrows(
            PermissionCheckFailureException.class,
            () -> hubHandler.generateAccessToken(admin, "uyuni-server.dev.local")
        );

        assertDoesNotThrow(
            () -> hubHandler.generateAccessToken(satAdmin, "uyuni-server.dev.local")
        );
    }

    @Test
    public void throwsCorrectExceptionWhenIssuingFails() throws TokenException {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock).issueAccessToken(satAdmin, "uyuni-server.dev.local");
        expectations.will(throwException(new TokenBuildingException("unexpected error")));
        context.checking(expectations);

        assertThrows(TokenCreationException.class,
            () -> hubHandler.generateAccessToken(satAdmin, "uyuni-server.dev.local"));
    }

    @Test
    public void ensureOnlySatAdminCanAccessToTokenStorage() throws Exception {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock).storeAccessToken(satAdmin, "uyuni-server.dev.local", "dummy-token");
        context.checking(expectations);

        assertThrows(
            PermissionCheckFailureException.class,
            () -> hubHandler.storeAccessToken(regular, "uyuni-server.dev.local", "dummy-token")
        );

        assertThrows(
            PermissionCheckFailureException.class,
            () -> hubHandler.storeAccessToken(admin, "uyuni-server.dev.local", "dummy-token")
        );

        assertDoesNotThrow(
            () -> hubHandler.storeAccessToken(satAdmin, "uyuni-server.dev.local", "dummy-token")
        );
    }

    @Test
    public void throwsCorrectExceptionWhenStoringFails() throws TokenParsingException {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock).storeAccessToken(satAdmin, "uyuni-server.dev.local", "dummy-token");
        expectations.will(throwException(new TokenParsingException("Cannot parse")));
        context.checking(expectations);

        assertThrows(InvalidTokenException.class,
            () -> hubHandler.storeAccessToken(satAdmin, "uyuni-server.dev.local", "dummy-token"));
    }

    @Test
    public void ensureOnlySatAdminCanRegister() throws Exception {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock)
                .register(satAdmin, "remote-server.dev.local", "admin", "admin", null);
        context.checking(expectations);

        assertThrows(
            PermissionCheckFailureException.class,
            () -> hubHandler.registerPeripheral(regular, "remote-server.dev.local", "admin", "admin")
        );

        assertThrows(
            PermissionCheckFailureException.class,
            () -> hubHandler.registerPeripheral(admin, "remote-server.dev.local", "admin", "admin")
        );

        assertDoesNotThrow(
            () -> hubHandler.registerPeripheral(satAdmin, "remote-server.dev.local", "admin", "admin")
        );
    }

    @Test
    public void throwsCorrectExceptionsWhenRegisteringFails() throws Exception {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock)
            .register(satAdmin, "fails-certificate.dev.local", "admin", "admin", "dummy");
        expectations.will(throwException(new CertificateException("Unable to parse")));

        expectations.allowing(hubManagerMock)
            .register(satAdmin, "fails-parsing.dev.local", "admin", "admin", "dummy");
        expectations.will(throwException(new TokenParsingException("Unable to parse")));

        expectations.allowing(hubManagerMock)
            .register(satAdmin, "fails-building.dev.local", "admin", "admin", "dummy");
        expectations.will(throwException(new TokenBuildingException("Unable to build")));

        expectations.allowing(hubManagerMock)
            .register(satAdmin, "fails-connecting.dev.local", "admin", "admin", "dummy");
        expectations.will(throwException(new IOException("Unable to connect")));

        context.checking(expectations);

        assertThrows(
            InvalidCertificateException.class,
            () -> hubHandler.registerPeripheral(satAdmin, "fails-certificate.dev.local", "admin", "admin", "dummy")
        );

        assertThrows(
            TokenExchangeFailedException.class,
            () -> hubHandler.registerPeripheral(satAdmin, "fails-parsing.dev.local", "admin", "admin", "dummy")
        );

        assertThrows(
            TokenExchangeFailedException.class,
            () -> hubHandler.registerPeripheral(satAdmin, "fails-building.dev.local", "admin", "admin", "dummy")
        );

        assertThrows(
            TokenExchangeFailedException.class,
            () -> hubHandler.registerPeripheral(satAdmin, "fails-connecting.dev.local", "admin", "admin", "dummy")
        );
    }

    @Test
    public void ensureOnlySatAdminCanRegisterWithToken() throws Exception {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock)
            .register(satAdmin, "remote-server.dev.local", "token", null);
        context.checking(expectations);

        assertThrows(
            PermissionCheckFailureException.class,
            () -> hubHandler.registerPeripheralWithToken(regular, "remote-server.dev.local", "token")
        );

        assertThrows(
            PermissionCheckFailureException.class,
            () -> hubHandler.registerPeripheralWithToken(admin, "remote-server.dev.local", "token")
        );

        assertDoesNotThrow(
            () -> hubHandler.registerPeripheralWithToken(satAdmin, "remote-server.dev.local", "token")
        );
    }

    @Test
    public void throwsCorrectExceptionsWhenRegisteringWithTokenFails() throws Exception {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock)
            .register(satAdmin, "fails-certificate.dev.local", "token", "dummy");
        expectations.will(throwException(new CertificateException("Unable to parse")));

        expectations.allowing(hubManagerMock)
            .register(satAdmin, "fails-parsing.dev.local", "token", "dummy");
        expectations.will(throwException(new TokenParsingException("Unable to parse")));

        expectations.allowing(hubManagerMock)
            .register(satAdmin, "fails-building.dev.local", "token", "dummy");
        expectations.will(throwException(new TokenBuildingException("Unable to build")));

        expectations.allowing(hubManagerMock)
            .register(satAdmin, "fails-connecting.dev.local", "token", "dummy");
        expectations.will(throwException(new IOException("Unable to connect")));

        context.checking(expectations);

        assertThrows(
            InvalidCertificateException.class,
            () -> hubHandler.registerPeripheralWithToken(satAdmin, "fails-certificate.dev.local", "token", "dummy")
        );

        assertThrows(
            TokenExchangeFailedException.class,
            () -> hubHandler.registerPeripheralWithToken(satAdmin, "fails-parsing.dev.local", "token", "dummy")
        );

        assertThrows(
            TokenExchangeFailedException.class,
            () -> hubHandler.registerPeripheralWithToken(satAdmin, "fails-building.dev.local", "token", "dummy")
        );

        assertThrows(
            TokenExchangeFailedException.class,
            () -> hubHandler.registerPeripheralWithToken(satAdmin, "fails-connecting.dev.local", "token", "dummy")
        );
    }
}
