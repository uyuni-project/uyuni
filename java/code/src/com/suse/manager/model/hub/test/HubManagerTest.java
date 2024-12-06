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

package com.suse.manager.model.hub.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.HubManager;
import com.suse.manager.model.hub.IssAccessToken;
import com.suse.manager.model.hub.TokenType;
import com.suse.manager.webui.utils.token.Token;
import com.suse.manager.webui.utils.token.TokenParser;
import com.suse.manager.webui.utils.token.TokenParsingException;

import org.jose4j.jwt.consumer.ErrorCodes;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class HubManagerTest extends BaseTestCaseWithUser {

    private HubFactory hubFactory;

    private HubManager hubManager;

    private String originalFqdn;

    @BeforeEach
    public void setup() {
        // Setting a fake hostname for the token validation
        originalFqdn = ConfigDefaults.get().getHostname();
        Config.get().setString(ConfigDefaults.SERVER_HOSTNAME, "uyuni-server.unit-test.local");

        hubFactory = new HubFactory();
        hubManager = new HubManager(hubFactory);
    }

    @AfterEach
    public void teardown() {
        Config.get().setString(ConfigDefaults.SERVER_HOSTNAME, originalFqdn);
    }

    @Test
    public void canIssueANewToken() throws Exception {
        Instant expectedExpiration = Instant.now().truncatedTo(ChronoUnit.SECONDS).plus(525_600L, ChronoUnit.MINUTES);
        String token = hubManager.issueAccessToken("uyuni-peripheral.test.local");

        // Ensure we get a token
        assertNotNull(token);

        // Ensure the token is correctly stored in the database
        IssAccessToken issAccessToken = hubFactory.lookupIssuedToken(token);
        assertNotNull(issAccessToken);
        assertEquals("uyuni-peripheral.test.local", issAccessToken.getServerFqdn());
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
        assertEquals("uyuni-peripheral.test.local", parsedJwtToken.getClaim("fqdn", String.class));
    }

    @Test
    public void canStoreThirdPartyToken() throws Exception {
        // This token has the following jwt payload:
        // {
        //     "jti" : "ftA6zJs1kBd7eGbGqeVKPQ",
        //     "exp" : 1764929426,
        //     "iat" : 1733393426,
        //     "nbf" : 1733393306,
        //     "fqdn" : "uyuni-server.unit-test.local"
        // }
        String token = """
            eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.ey\
            JqdGkiOiJmdEE2ekpzMWtCZDdlR2JHcWVWS1BRI\
            iwiZXhwIjoxNzY0OTI5NDI2LCJpYXQiOjE3MzMz\
            OTM0MjYsIm5iZiI6MTczMzM5MzMwNiwiZnFkbiI\
            6InV5dW5pLXNlcnZlci51bml0LXRlc3QubG9jYW\
            wifQ.2BQEEUynCwM_20GwOfQGrHmqxk_fP1PG0_\
            QQFmtngwg""";

        hubManager.storeAccessToken("external-server.dev.local", token);

        // Ensure the token is correctly stored in the database
        IssAccessToken issAccessToken = hubFactory.lookupAccessTokenFor("external-server.dev.local");
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
        //     "exp" : 1764929426,
        //     "iat" : 1733393426,
        //     "nbf" : 1733393306,
        //     "fqdn" : "different-server.unit-test.local"
        // }

        String token = """
            eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.ey\
            JqdGkiOiJmdEE2ekpzMWtCZDdlR2JHcWVWS1BRI\
            iwiZXhwIjoxNzY0OTI5NDI2LCJpYXQiOjE3MzMz\
            OTM0MjYsIm5iZiI6MTczMzM5MzMwNiwiZnFkbiI\
            6ImRpZmZlcmVudC1zZXJ2ZXIudW5pdC10ZXN0Lm\
            xvY2FsIn0.nD4DfRbKA-8aWwyLR6ZuFhprMbzWw\
            Z9YhKh14CEFVr4""";

        var exception = assertThrows(
            TokenParsingException.class,
            () -> hubManager.storeAccessToken("external-server.dev.local", token)
        );

        assertEquals(
            "FQDN do not match. Expected uyuni-server.unit-test.local got different-server.unit-test.local",
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
        //     "fqdn" : "uyuni-server.unit-test.local"
        // }

        String token = """
            eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.ey\
            JqdGkiOiJmdEE2ekpzMWtCZDdlR2JHcWVWS1BRI\
            iwiZXhwIjoxNjA3MjQ5NDI2LCJpYXQiOjE1NzU3\
            MTM0MjYsIm5iZiI6MTU3NTcxMzMwNiwiZnFkbiI\
            6InV5dW5pLXNlcnZlci51bml0LXRlc3QubG9jYW\
            wifQ.fbk7RIZ0F_AKGD9mBKBD8U3hz23iEVym-s\
            9BzRoCofc""";

        TokenParsingException exception = assertThrows(
            TokenParsingException.class,
            () -> hubManager.storeAccessToken("external-server.dev.local", token)
        );

        InvalidJwtException cause = assertInstanceOf(InvalidJwtException.class, exception.getCause());
        assertEquals(1, cause.getErrorDetails().size());
        assertEquals(ErrorCodes.EXPIRED, cause.getErrorDetails().get(0).getErrorCode());
    }
}
