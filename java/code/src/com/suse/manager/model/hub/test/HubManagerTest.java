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

package com.suse.manager.model.hub.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.iss.IssRole;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

import com.suse.manager.iss.IssClientFactory;
import com.suse.manager.iss.IssExternalClient;
import com.suse.manager.iss.IssInternalClient;
import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.HubManager;
import com.suse.manager.model.hub.IssAccessToken;
import com.suse.manager.model.hub.IssHub;
import com.suse.manager.model.hub.IssPeripheral;
import com.suse.manager.model.hub.TokenType;
import com.suse.manager.webui.utils.token.Token;
import com.suse.manager.webui.utils.token.TokenBuildingException;
import com.suse.manager.webui.utils.token.TokenParser;
import com.suse.manager.webui.utils.token.TokenParsingException;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jose4j.jwt.consumer.ErrorCodes;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class HubManagerTest extends JMockBaseTestCaseWithUser {

    private static final String LOCAL_SERVER_FQDN = "local-server.unit-test.local";

    private static final String REMOTE_SERVER_FQDN = "remote-server.unit-test.local";

    private HubFactory hubFactory;

    private HubManager hubManager;

    private IssClientFactory clientFactoryMock;

    private String originalServerSecret;

    private String originalFqdn;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);

        // Setting a fake hostname for the token validation
        originalFqdn = ConfigDefaults.get().getHostname();
        originalServerSecret = Config.get().getString("server.secret_key");

        Config.get().setString(ConfigDefaults.SERVER_HOSTNAME, LOCAL_SERVER_FQDN);
        Config.get().setString("server.secret_key", // my-super-secret-key-for-testing
            "6D792D73757065722D7365637265742D6B65792D666F722D74657374696E670D0A");

        hubFactory = new HubFactory();
        clientFactoryMock = mock(IssClientFactory.class);

        hubManager = new HubManager(hubFactory, clientFactoryMock);
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        Config.get().setString(ConfigDefaults.SERVER_HOSTNAME, originalFqdn);
        Config.get().setString("server.secret_key", originalServerSecret);
    }

    @Test
    public void canIssueANewToken() throws Exception {
        Instant expectedExpiration = Instant.now().truncatedTo(ChronoUnit.SECONDS).plus(525_600L, ChronoUnit.MINUTES);
        String token = hubManager.issueAccessToken(REMOTE_SERVER_FQDN);

        // Ensure we get a token
        assertNotNull(token);

        // Ensure the token is correctly stored in the database
        IssAccessToken issAccessToken = hubFactory.lookupIssuedToken(token);
        assertNotNull(issAccessToken);
        assertEquals(REMOTE_SERVER_FQDN, issAccessToken.getServerFqdn());
        assertEquals(TokenType.ISSUED, issAccessToken.getType());
        assertFalse(issAccessToken.isExpired());
        assertTrue(issAccessToken.isValid());
        assertEquals(
            expectedExpiration,
            issAccessToken.getExpirationDate().toInstant().truncatedTo(ChronoUnit.SECONDS)
        );

        // Ensure we can decode the token correctly
        Token parsedJwtToken = new TokenParser()
            .usingServerSecret()
            .parse(token);

        assertEquals(expectedExpiration, parsedJwtToken.getExpirationTime());
        assertEquals(REMOTE_SERVER_FQDN, parsedJwtToken.getClaim("fqdn", String.class));
    }

    @Test
    public void canStoreThirdPartyToken() throws Exception {
        // This token has the following jwt payload:
        // {
        //     "jti" : "ftA6zJs1kBd7eGbGqeVKPQ",
        //     "exp" : 3313569600,
        //     "iat" : 1733393426,
        //     "nbf" : 1733393306,
        //     "fqdn" : "local-server.unit-test.local" (aka LOCAL_SERVER_FQDN)
        // }
        String token = """
            eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.ey\
            JqdGkiOiJmdEE2ekpzMWtCZDdlR2JHcWVWS1BRI\
            iwiZXhwIjozMzEzNTY5NjAwLCJpYXQiOjE3MzMz\
            OTM0MjYsIm5iZiI6MTczMzM5MzMwNiwiZnFkbiI\
            6ImxvY2FsLXNlcnZlci51bml0LXRlc3QubG9jYW\
            wifQ.6M9MOQvsiFr4EeyeAo36_2jUbV1Ju9ceCD\
            kI-mykFms""";

        hubManager.storeAccessToken(REMOTE_SERVER_FQDN, token);

        // Ensure the token is correctly stored in the database
        IssAccessToken issAccessToken = hubFactory.lookupAccessTokenFor(REMOTE_SERVER_FQDN);
        assertNotNull(issAccessToken);
        assertEquals(token, issAccessToken.getToken());
        assertEquals(TokenType.CONSUMED, issAccessToken.getType());
        assertNotNull(issAccessToken.getExpirationDate());
    }

    @Test
    public void rejectsTokenIfFqdnIsNotMatching() {
        // This token has the following jwt payload:
        // {
        //     "jti" : "ftA6zJs1kBd7eGbGqeVKPQ",
        //     "exp" : 3313569600,
        //     "iat" : 1733393426,
        //     "nbf" : 1733393306,
        //     "fqdn" : "different-server.unit-test.local"
        // }

        String token = """
            eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.ey\
            JqdGkiOiJmdEE2ekpzMWtCZDdlR2JHcWVWS1BRI\
            iwiZXhwIjozMzEzNTY5NjAwLCJpYXQiOjE3MzMz\
            OTM0MjYsIm5iZiI6MTczMzM5MzMwNiwiZnFkbiI\
            6ImRpZmZlcmVudC1zZXJ2ZXIudW5pdC10ZXN0Lm\
            xvY2FsIn0.GaW8A54CrfUI43MiCDw_7TPCUeL8l\
            TU76L5Yn9bXHGs""";

        var exception = assertThrows(
            TokenParsingException.class,
            () -> hubManager.storeAccessToken(REMOTE_SERVER_FQDN, token)
        );

        assertEquals(
            "FQDN do not match. Expected " + LOCAL_SERVER_FQDN + " got different-server.unit-test.local",
            exception.getMessage()
        );
    }

    @Test
    public void rejectsTokenIfAlreadyExpired() {
        // This token has the following jwt payload:
        // {
        //     "jti" : "ftA6zJs1kBd7eGbGqeVKPQ",
        //     "exp": 1607249426,
        //     "iat": 1575713426,
        //     "nbf": 1575713306,
        //     "fqdn" : "local-server.unit-test.local" (aka LOCAL_SERVER_FQDN)
        // }

        String token = """
            eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.ey\
            JqdGkiOiJmdEE2ekpzMWtCZDdlR2JHcWVWS1BRI\
            iwiZXhwIjoxNjA3MjQ5NDI2LCJpYXQiOjE1NzU3\
            MTM0MjYsIm5iZiI6MTU3NTcxMzMwNiwiZnFkbiI\
            6ImxvY2FsLXNlcnZlci51bml0LXRlc3QubG9jYW\
            wifQ.CWuFZX8KKFQ3Le7Qn0kGE_16ZCNs7QrneL\
            ajQABZuAw""";

        TokenParsingException exception = assertThrows(
            TokenParsingException.class,
            () -> hubManager.storeAccessToken("external-server.dev.local", token)
        );

        InvalidJwtException cause = assertInstanceOf(InvalidJwtException.class, exception.getCause());
        assertEquals(1, cause.getErrorDetails().size());
        assertEquals(ErrorCodes.EXPIRED, cause.getErrorDetails().get(0).getErrorCode());
    }

    @Test
    public void canSaveHubAndPeripheralServers() {
        hubManager.saveNewServer(IssRole.HUB, "dummy.hub.fqdn", "dummy-certificate-data");

        Optional<IssHub> issHub = hubFactory.lookupIssHubByFqdn("dummy.hub.fqdn");
        assertTrue(issHub.isPresent());
        assertEquals("dummy-certificate-data", issHub.get().getRootCa());

        hubManager.saveNewServer(IssRole.PERIPHERAL, "dummy.peripheral.fqdn", null);
        Optional<IssPeripheral> issPeripheral = hubFactory.lookupIssPeripheralByFqdn("dummy.peripheral.fqdn");
        assertTrue(issPeripheral.isPresent());
        assertNull(issPeripheral.get().getRootCa());
    }

    @Test
    public void canRegisterPeripheralWithUserNameAndPassword()
        throws TokenBuildingException, CertificateException, IOException, TokenParsingException {
        IssExternalClient externalClient = mock(IssExternalClient.class);
        IssInternalClient internalClient = mock(IssInternalClient.class);

        // The token generated by REMOTE_SERVER_FQDN for LOCAL_SERVER_FQDN
        String remoteTokenForLocal = """
            eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.ey\
            JqdGkiOiJmdEE2ekpzMWtCZDdlR2JHcWVWS1BRI\
            iwiZXhwIjozMzEzNTY5NjAwLCJpYXQiOjE3MzMz\
            OTM0MjYsIm5iZiI6MTczMzM5MzMwNiwiZnFkbiI\
            6ImxvY2FsLXNlcnZlci51bml0LXRlc3QubG9jYW\
            wifQ.XldOvQJypIkb4E1am0JlBxsHYrx_7J77s1\
            vTrvoNlEU""";

        context().checking(new Expectations() {{
            allowing(clientFactoryMock).newExternalClient(REMOTE_SERVER_FQDN, "admin", "admin", null);
            will(returnValue(externalClient));

            allowing(externalClient).generateAccessToken(LOCAL_SERVER_FQDN);
            will(returnValue(remoteTokenForLocal));

            allowing(externalClient).close();

            allowing(clientFactoryMock).newInternalClient(REMOTE_SERVER_FQDN, remoteTokenForLocal, null);
            will(returnValue(internalClient));

            allowing(internalClient).register(
                with(equal(IssRole.HUB)),
                with(any(String.class)),
                with(aNull(String.class))
            );
        }});

        // Register the remote server as PERIPHERAL for this local server
        hubManager.register(REMOTE_SERVER_FQDN, IssRole.PERIPHERAL, "admin", "admin", null);

        // Verify the remote server is saved as peripheral
        Optional<IssPeripheral> issPeripheral = hubFactory.lookupIssPeripheralByFqdn(REMOTE_SERVER_FQDN);
        assertTrue(issPeripheral.isPresent());

        // Verify we have both tokens for the remote server
        var consumed = hubFactory.lookupAccessTokenByFqdnAndType(REMOTE_SERVER_FQDN, TokenType.CONSUMED);
        assertNotNull(consumed);
        assertEquals(remoteTokenForLocal, consumed.getToken());

        var issued = hubFactory.lookupAccessTokenByFqdnAndType(REMOTE_SERVER_FQDN, TokenType.ISSUED);
        assertNotNull(issued);
    }

    @Test
    public void canRegisterHubWithUserNameAndPassword()
        throws TokenBuildingException, CertificateException, IOException, TokenParsingException {
        IssExternalClient externalClient = mock(IssExternalClient.class);
        IssInternalClient internalClient = mock(IssInternalClient.class);

        // The token generated by REMOTE_SERVER_FQDN for LOCAL_SERVER_FQDN
        String remoteTokenForLocal = """
            eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.ey\
            JqdGkiOiJmdEE2ekpzMWtCZDdlR2JHcWVWS1BRI\
            iwiZXhwIjozMzEzNTY5NjAwLCJpYXQiOjE3MzMz\
            OTM0MjYsIm5iZiI6MTczMzM5MzMwNiwiZnFkbiI\
            6ImxvY2FsLXNlcnZlci51bml0LXRlc3QubG9jYW\
            wifQ.XldOvQJypIkb4E1am0JlBxsHYrx_7J77s1\
            vTrvoNlEU""";

        context().checking(new Expectations() {{
            allowing(clientFactoryMock).newExternalClient(REMOTE_SERVER_FQDN, "admin", "admin", null);
            will(returnValue(externalClient));

            allowing(externalClient).generateAccessToken(LOCAL_SERVER_FQDN);
            will(returnValue(remoteTokenForLocal));

            allowing(externalClient).close();

            allowing(clientFactoryMock).newInternalClient(REMOTE_SERVER_FQDN, remoteTokenForLocal, null);
            will(returnValue(internalClient));

            allowing(internalClient).register(
                with(equal(IssRole.PERIPHERAL)),
                with(any(String.class)),
                with(aNull(String.class))
            );
        }});

        // Register the remote server as HUB for this local server
        hubManager.register(REMOTE_SERVER_FQDN, IssRole.HUB, "admin", "admin", null);

        // Verify the remote server is saved as hub
        Optional<IssHub> issHub = hubFactory.lookupIssHubByFqdn(REMOTE_SERVER_FQDN);
        assertTrue(issHub.isPresent());

        // Verify we have both tokens for the remote server
        var consumed = hubFactory.lookupAccessTokenByFqdnAndType(REMOTE_SERVER_FQDN, TokenType.CONSUMED);
        assertNotNull(consumed);
        assertEquals(remoteTokenForLocal, consumed.getToken());

        var issued = hubFactory.lookupAccessTokenByFqdnAndType(REMOTE_SERVER_FQDN, TokenType.ISSUED);
        assertNotNull(issued);
    }
}
