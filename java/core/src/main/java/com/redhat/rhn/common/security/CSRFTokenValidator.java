/*
 * Copyright (c) 2011 SUSE LLC
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
package com.redhat.rhn.common.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * This is a utility class containing static methods for handling creation and
 * validation of security tokens used for preventing from CSRF attacks.
 */
public final class CSRFTokenValidator {

    private static final Logger LOG = LogManager.getLogger(CSRFTokenValidator.class);
    private static String tokenKey = "csrf_token";
    private static final SecureRandom SECURE_RANDOM;

    static {
        SECURE_RANDOM = new SecureRandom();
        LOG.warn("CSRF token SecureRandom algorithm: {}", SECURE_RANDOM.getAlgorithm());
    }

    /* utility class, no public constructor  */
    private CSRFTokenValidator() {
    }

    /**
     * Create a new CSRF token using the platform default SecureRandom.
     *
     * @return token as a String
     */
    private static String createNewToken() {
        return String.valueOf(SECURE_RANDOM.nextLong());
    }

    /**
     * Return the CSRF token from the given session, create a new token if
     * there is currently none associated with this session.
     *
     * @param session HttpSession to retrieve the token from
     * @return token Security token retrieved from the session
     */
    public static String getToken(HttpSession session) {
        String tokenValue = (String) session.getAttribute(tokenKey);
        if (tokenValue == null) {
            // Create new token if necessary
            tokenValue = createNewToken();
            session.setAttribute(tokenKey, tokenValue);
        }
        return tokenValue;
    }

    /**
     * Validate a given request within its own session, throws a runtime
     * exception leading to internal server error in case of failure.
     *
     * @param request HTTPServletRequest to validate the token for
     * @throws CSRFTokenException In case the validation failed
     */
    public static void validate(HttpServletRequest request) throws CSRFTokenException {
        HttpSession session = request.getSession();

        if (session.getAttribute(tokenKey) == null) {
            throw new CSRFTokenException("Session does not contain a CSRF security token");
        }

        String header = request.getHeader("X-CSRF-Token");
        String parameter = request.getParameter(tokenKey);

        String token = null;
        if (parameter != null) {
            token = parameter;
        }
        else if (header != null) {
            token = header;
        }

        if (token == null) {
            throw new CSRFTokenException("Request does not contain a CSRF security token");
        }

        if (!session.getAttribute(tokenKey).equals(token)) {
            throw new CSRFTokenException("Validation of CSRF security token failed");
        }
    }
}
