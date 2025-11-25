/*
 * Copyright (c) 2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.manager.webui.utils.token.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.suse.manager.webui.utils.token.DefaultTokenBuilder;
import com.suse.manager.webui.utils.token.Token;
import com.suse.manager.webui.utils.token.TokenException;
import com.suse.manager.webui.utils.token.TokenParser;
import com.suse.manager.webui.utils.token.TokenParsingException;

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
}
