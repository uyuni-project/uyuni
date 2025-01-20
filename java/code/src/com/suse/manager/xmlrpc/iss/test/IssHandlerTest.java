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

import com.redhat.rhn.domain.iss.IssRole;
import com.redhat.rhn.frontend.xmlrpc.InvalidTokenException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.frontend.xmlrpc.TokenCreationException;
import com.redhat.rhn.frontend.xmlrpc.TokenExchangeFailedException;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;

import com.suse.manager.hub.HubManager;
import com.suse.manager.model.hub.IssRole;
import com.suse.manager.webui.utils.token.TokenBuildingException;
import com.suse.manager.webui.utils.token.TokenException;
import com.suse.manager.webui.utils.token.TokenParsingException;
import com.suse.manager.xmlrpc.InvalidCertificateException;
import com.suse.manager.xmlrpc.iss.IssHandler;

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
public class IssHandlerTest extends BaseHandlerTestCase {

    @RegisterExtension
    protected final JUnit5Mockery context = new JUnit5Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    private HubManager hubManagerMock;

    private IssHandler issHandler;

    @BeforeEach
    public void setup() {
        context.setThreadingPolicy(new Synchroniser());
        context.setImposteriser((ByteBuddyClassImposteriser.INSTANCE));

        hubManagerMock = context.mock(HubManager.class);
        issHandler = new IssHandler(hubManagerMock);
    }

    @Test
    public void ensureOnlySatAdminCanAccessToTokenGeneration() throws Exception {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock).issueAccessToken(satAdmin, "uyuni-server.dev.local");
        expectations.will(returnValue("dummy-token"));
        context.checking(expectations);

        assertThrows(
            PermissionCheckFailureException.class,
            () -> issHandler.generateAccessToken(regular, "uyuni-server.dev.local")
        );

        assertThrows(
            PermissionCheckFailureException.class,
            () -> issHandler.generateAccessToken(admin, "uyuni-server.dev.local")
        );

        assertDoesNotThrow(
            () -> issHandler.generateAccessToken(satAdmin, "uyuni-server.dev.local")
        );
    }

    @Test
    public void throwsCorrectExceptionWhenIssuingFails() throws TokenException {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock).issueAccessToken(satAdmin, "uyuni-server.dev.local");
        expectations.will(throwException(new TokenBuildingException("unexpected error")));
        context.checking(expectations);

        assertThrows(TokenCreationException.class,
            () -> issHandler.generateAccessToken(satAdmin, "uyuni-server.dev.local"));
    }

    @Test
    public void ensureOnlySatAdminCanAccessToTokenStorage() throws Exception {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock).storeAccessToken(satAdmin, "uyuni-server.dev.local", "dummy-token");
        context.checking(expectations);

        assertThrows(
            PermissionCheckFailureException.class,
            () -> issHandler.storeAccessToken(regular, "uyuni-server.dev.local", "dummy-token")
        );

        assertThrows(
            PermissionCheckFailureException.class,
            () -> issHandler.storeAccessToken(admin, "uyuni-server.dev.local", "dummy-token")
        );

        assertDoesNotThrow(
            () -> issHandler.storeAccessToken(satAdmin, "uyuni-server.dev.local", "dummy-token")
        );
    }

    @Test
    public void throwsCorrectExceptionWhenStoringFails() throws TokenParsingException {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock).storeAccessToken(satAdmin, "uyuni-server.dev.local", "dummy-token");
        expectations.will(throwException(new TokenParsingException("Cannot parse")));
        context.checking(expectations);

        assertThrows(InvalidTokenException.class,
            () -> issHandler.storeAccessToken(satAdmin, "uyuni-server.dev.local", "dummy-token"));
    }

    @Test
    public void ensureOnlySatAdminCanRegister() throws Exception {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock)
                .register(satAdmin, "remote-server.dev.local", IssRole.PERIPHERAL, "admin", "admin", null);
        context.checking(expectations);

        assertThrows(
            PermissionCheckFailureException.class,
            () -> issHandler.register(regular, "remote-server.dev.local", "PERIPHERAL", "admin", "admin", null)
        );

        assertThrows(
            PermissionCheckFailureException.class,
            () -> issHandler.register(admin, "remote-server.dev.local", "PERIPHERAL", "admin", "admin", null)
        );

        assertDoesNotThrow(
            () -> issHandler.register(satAdmin, "remote-server.dev.local", "PERIPHERAL", "admin", "admin", null)
        );
    }

    @Test
    public void throwsCorrectExceptionsWhenRegisteringFails() throws Exception {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock)
            .register(satAdmin, "fails-certificate.dev.local", IssRole.PERIPHERAL, "admin", "admin", "dummy");
        expectations.will(throwException(new CertificateException("Unable to parse")));

        expectations.allowing(hubManagerMock)
            .register(satAdmin, "fails-parsing.dev.local", IssRole.PERIPHERAL, "admin", "admin", "dummy");
        expectations.will(throwException(new TokenParsingException("Unable to parse")));

        expectations.allowing(hubManagerMock)
            .register(satAdmin, "fails-building.dev.local", IssRole.PERIPHERAL, "admin", "admin", "dummy");
        expectations.will(throwException(new TokenBuildingException("Unable to build")));

        expectations.allowing(hubManagerMock)
            .register(satAdmin, "fails-connecting.dev.local", IssRole.PERIPHERAL, "admin", "admin", "dummy");
        expectations.will(throwException(new IOException("Unable to connect")));

        context.checking(expectations);

        assertThrows(
            InvalidCertificateException.class,
            () -> issHandler.register(satAdmin, "fails-certificate.dev.local", "PERIPHERAL", "admin", "admin", "dummy")
        );

        assertThrows(
            TokenExchangeFailedException.class,
            () -> issHandler.register(satAdmin, "fails-parsing.dev.local", "PERIPHERAL", "admin", "admin", "dummy")
        );

        assertThrows(
            TokenExchangeFailedException.class,
            () -> issHandler.register(satAdmin, "fails-building.dev.local", "PERIPHERAL", "admin", "admin", "dummy")
        );

        assertThrows(
            TokenExchangeFailedException.class,
            () -> issHandler.register(satAdmin, "fails-connecting.dev.local", "PERIPHERAL", "admin", "admin", "dummy")
        );
    }

    @Test
    public void ensureOnlySatAdminCanRegisterWithToken() throws Exception {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock)
            .register(satAdmin, "remote-server.dev.local", IssRole.PERIPHERAL, "token", null);
        context.checking(expectations);

        assertThrows(
            PermissionCheckFailureException.class,
            () -> issHandler.registerWithToken(regular, "remote-server.dev.local", "PERIPHERAL", "token", null)
        );

        assertThrows(
            PermissionCheckFailureException.class,
            () -> issHandler.registerWithToken(admin, "remote-server.dev.local", "PERIPHERAL", "token", null)
        );

        assertDoesNotThrow(
            () -> issHandler.registerWithToken(satAdmin, "remote-server.dev.local", "PERIPHERAL", "token", null)
        );
    }

    @Test
    public void throwsCorrectExceptionsWhenRegisteringWithTokenFails() throws Exception {
        Expectations expectations = new Expectations();
        expectations.allowing(hubManagerMock)
            .register(satAdmin, "fails-certificate.dev.local", IssRole.PERIPHERAL, "token", "dummy");
        expectations.will(throwException(new CertificateException("Unable to parse")));

        expectations.allowing(hubManagerMock)
            .register(satAdmin, "fails-parsing.dev.local", IssRole.PERIPHERAL, "token", "dummy");
        expectations.will(throwException(new TokenParsingException("Unable to parse")));

        expectations.allowing(hubManagerMock)
            .register(satAdmin, "fails-building.dev.local", IssRole.PERIPHERAL, "token", "dummy");
        expectations.will(throwException(new TokenBuildingException("Unable to build")));

        expectations.allowing(hubManagerMock)
            .register(satAdmin, "fails-connecting.dev.local", IssRole.PERIPHERAL, "token", "dummy");
        expectations.will(throwException(new IOException("Unable to connect")));

        context.checking(expectations);

        assertThrows(
            InvalidCertificateException.class,
            () -> issHandler.registerWithToken(satAdmin, "fails-certificate.dev.local", "PERIPHERAL", "token", "dummy")
        );

        assertThrows(
            TokenExchangeFailedException.class,
            () -> issHandler.registerWithToken(satAdmin, "fails-parsing.dev.local", "PERIPHERAL", "token", "dummy")
        );

        assertThrows(
            TokenExchangeFailedException.class,
            () -> issHandler.registerWithToken(satAdmin, "fails-building.dev.local", "PERIPHERAL", "token", "dummy")
        );

        assertThrows(
            TokenExchangeFailedException.class,
            () -> issHandler.registerWithToken(satAdmin, "fails-connecting.dev.local", "PERIPHERAL", "token", "dummy")
        );
    }
}
