/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 */
package com.redhat.rhn.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.redhat.rhn.testing.RhnMockHttpServletRequest;
import com.redhat.rhn.testing.RhnMockHttpSession;

import org.junit.jupiter.api.Test;

public class CSRFTokenValidatorTest {

    @Test
    public void testGetTokenCreatesToken() {
        RhnMockHttpSession session = new RhnMockHttpSession();
        String token = CSRFTokenValidator.getToken(session);
        assertNotNull(token, "Token should not be null");
        assertNotEquals("", token, "Token should not be empty");
    }

    @Test
    public void testGetTokenReturnsSameTokenForSession() {
        RhnMockHttpSession session = new RhnMockHttpSession();
        String token1 = CSRFTokenValidator.getToken(session);
        String token2 = CSRFTokenValidator.getToken(session);
        assertEquals(token1, token2, "Same session should return the same token");
    }

    @Test
    public void testGetTokenReturnsDifferentTokensForDifferentSessions() {
        String token1 = CSRFTokenValidator.getToken(new RhnMockHttpSession());
        String token2 = CSRFTokenValidator.getToken(new RhnMockHttpSession());
        assertNotEquals(token1, token2, "Different sessions should get different tokens");
    }

    @Test
    public void testValidateSucceedsWithHeader() throws CSRFTokenException {
        RhnMockHttpSession session = new RhnMockHttpSession();
        String token = CSRFTokenValidator.getToken(session);

        RhnMockHttpServletRequest request = new RhnMockHttpServletRequest();
        request.setSession(session);
        request.setHeader("X-CSRF-Token", token);

        CSRFTokenValidator.validate(request);
    }

    @Test
    public void testValidateSucceedsWithParameter() throws CSRFTokenException {
        RhnMockHttpSession session = new RhnMockHttpSession();
        String token = CSRFTokenValidator.getToken(session);

        RhnMockHttpServletRequest request = new RhnMockHttpServletRequest();
        request.setSession(session);
        request.addParameter("csrf_token", token);

        CSRFTokenValidator.validate(request);
    }

    @Test
    public void testValidateFailsWithNoToken() {
        RhnMockHttpSession session = new RhnMockHttpSession();
        CSRFTokenValidator.getToken(session);

        RhnMockHttpServletRequest request = new RhnMockHttpServletRequest();
        request.setSession(session);

        assertThrows(CSRFTokenException.class, () -> CSRFTokenValidator.validate(request));
    }

    @Test
    public void testValidateFailsWithWrongToken() {
        RhnMockHttpSession session = new RhnMockHttpSession();
        CSRFTokenValidator.getToken(session);

        RhnMockHttpServletRequest request = new RhnMockHttpServletRequest();
        request.setSession(session);
        request.setHeader("X-CSRF-Token", "wrong-token");

        assertThrows(CSRFTokenException.class, () -> CSRFTokenValidator.validate(request));
    }

    @Test
    public void testValidateFailsWithNoSessionToken() {
        RhnMockHttpServletRequest request = new RhnMockHttpServletRequest();
        request.setSession(new RhnMockHttpSession());
        request.setHeader("X-CSRF-Token", "some-token");

        assertThrows(CSRFTokenException.class, () -> CSRFTokenValidator.validate(request));
    }
}
