/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.frontend.servlets;

import com.suse.manager.webui.utils.LoginHelper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * A CreateRedirectURI is a functor object that returns a redirect URI for a given
 * HttpServletRequest object. The URI consists of the original request URI along with
 * any query string parameters and values (from a GET request) or any form parameters and
 * values (from a POST request) added to the new query string.
 * <p>
 * If the length exceeds <code>MAX_URL_LENGTH</code>, then the redirect URI will default
 * to a value of {@link LoginHelper#DEFAULT_URL_BOUNCE};
 * <p>
 * <strong>Note:</strong> This implementation does not support multi-value parameters.
 * <p>
 * <strong>Note:</strong> This class may/should get generalized and wind up implementing a
 * "functor" interface or extending from some base class if other request-related functor
 * classes are added to the code base.
 *
 */
public class CreateRedirectURI {

    /**
     * Most browsers limit a URL length to 2048 bytes.
     */
    public static final long MAX_URL_LENGTH = 2048;

    /**
     * Execute this functor object and create a redirect URI with request params appended
     * to the query string.
     *
     * @param request The current request
     * @return A redirect URI with request params appended to the query string
     */
    public String execute(HttpServletRequest request) {
        StringBuilder redirectURI = new StringBuilder(request.getRequestURI()).append("?");
        String paramName = null;
        String paramValue = null;

        Enumeration paramNames = request.getParameterNames();

        while (paramNames.hasMoreElements()) {
            paramName = (String)paramNames.nextElement();
            paramValue = request.getParameter(paramName);

            paramName = encode(paramName);
            paramValue = encode(paramValue);

            redirectURI.append(paramName).append("=").append(paramValue).append("&");
        }

        if (redirectURI.length() > MAX_URL_LENGTH) {
            return LoginHelper.DEFAULT_URL_BOUNCE;
        }

        return redirectURI.toString();
    }

    private String encode(String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8);
    }
 }
