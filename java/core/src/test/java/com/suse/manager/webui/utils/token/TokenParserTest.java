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

package com.suse.manager.webui.utils.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

public class TokenParserTest {

    @Test
    public void testVerifyToken() throws TokenException {
        String token = new DefaultTokenBuilder()
            .usingServerSecret()
            .build()
            .getSerializedForm();

        TokenParser tokenParser = new TokenParser().usingServerSecret();
        assertTrue(tokenParser.verify(token));
    }

    @Test
    public void testWrongOriginToken() {
        String wrongOriginToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Ikpva" +
            "G4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        TokenParser tokenParser = new TokenParser().usingServerSecret();
        assertFalse(tokenParser.verify(wrongOriginToken));
    }

    @Test
    public void testParseClaimsWithoutVerifying() throws TokenParsingException {
        Token parsedToken = new TokenParser()
            .skippingExpirationVerification()
            .skippingNotBeforeVerification()
            .skippingSignatureVerification()
            .parse("""
                eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJUb2tlblBhcnNlclRlc3QiLCJqd\
                GkiOiI3ZjY5NWJiMi01YzhhLTRiMjktODIzOS1kYWY1Njg3ZTk0N2YiLCJuYW1lIjoiSm9obiBE\
                b2UiLCJpYXQiOjE1MTYyMzkwMjJ9.Pz_wi8p73rGlHjM8s68FTSKjc8OkGFvXeBC25KTS2e8""");

        assertEquals("TokenParserTest", parsedToken.getSubject());
        assertEquals("7f695bb2-5c8a-4b29-8239-daf5687e947f", parsedToken.getJwtId());
        assertEquals("John Doe", parsedToken.getClaim("name", String.class));
    }

    @Test
    public void testParseTokenWithUnrelatedSecret() {
        //the secret is NOT the one of the token
        String customRandomSecretAES256 = "8f3a7b2c9d4e1f6a5b8c7d2e9f0a3b6c4d8e2f9a0b5c8d7e1f4a9b2c5d8e3f7a";
        String customToken = """
                eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJfR0U0TzJSbnozTHBxSEVESzFYeG93IiwiZXhwIjoxNzU5NDk5MTA4LCJpY\
                XQiOjE3NTkzMTE5MDgsIm5iZiI6MTc1OTMxMTc4OCwib3JnIjowLCJvbmx5Q2hhbm5lbHMiOlsic2xlLW1vZHVsZS1\
                jb250YWluZXJzMTItcG9vbC14ODZfNjQtc3A1Il19.CzCF6nd3-S4R2aEqP_3alBxvbsZbRF7MeOf5haHNy1k""";

        TokenParser tokenParser = new TokenParser().withCustomSecret(customRandomSecretAES256);

        try {
            tokenParser.parse(customToken);
            fail("this should have thrown an exception");
        }
        catch (TokenParsingException ex) {
            int errorStringLen = ex.getMessage().replace("Unable to parse token claims", "").length();
            assertTrue(errorStringLen > 10); //some meaningful message
            // Error message is something like:
            // Unable to parse token claims: JWT rejected due to invalid signature. \
            // Additional details: [[9] Invalid JWS Signature: JsonWebSignature{"alg":"HS256"}->"...]
        }
    }
}
