/**
 * Copyright (c) 2011 Novell
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.common.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * This is a utility class containing static methods for handling creation and
 * validation of security tokens used for preventing from CSRF attacks.
 */
public final class CSRFTokenValidator {

    private static String TOKEN_KEY = "csrf_token";
    private static String DEFAULT_ALGORITHM = "SHA1PRNG";

    /**
     * Create a new CSRF token using the given algorithm, throws a runtime
     * exception in case of an algorithm is used that is not available.
     *
     * @param alg
     * @return token as a String
     * @throws CSRFTokenException
     */
    private static String createNewToken(String alg) throws CSRFTokenException {
        String tokenValue = null;
        try {
            tokenValue = String.valueOf(SecureRandom.getInstance(alg).nextLong());
        } catch (NoSuchAlgorithmException e) {
            throw new CSRFTokenException(e.getMessage(), e);
        }
        return tokenValue;
    }

    /**
     * Return the CSRF token from the given session. Create a new token if
     * there is currently none associated with this session.
     *
     * @param session
     * @return token
     */
    public static String getToken(HttpSession session) {
        String tokenValue = (String) session.getAttribute(TOKEN_KEY);
        if (tokenValue == null) {
            // Create new token if necessary
            tokenValue = createNewToken(DEFAULT_ALGORITHM);
            session.setAttribute(TOKEN_KEY, tokenValue);
        }
        return tokenValue;
    }

    /**
     * Validate a given request within its own session, throws a runtime
     * exception leading to internal server error in case of failure.
     *
     * @param request
     * @param session
     * @throws CSRFTokenException
     */
    public static void validate(HttpServletRequest request) throws CSRFTokenException {
        HttpSession session = request.getSession();

        if (session.getAttribute(TOKEN_KEY) == null) {
            throw new CSRFTokenException("Session does not contain a CSRF security token");
        }

        if (request.getParameter(TOKEN_KEY) == null) {
            throw new CSRFTokenException("Request does not contain a CSRF security token");
        }

        if (!session.getAttribute(TOKEN_KEY).equals(request.getParameter(TOKEN_KEY))) {
            throw new CSRFTokenException("Validation of CSRF security token failed");
        }
    }
}
