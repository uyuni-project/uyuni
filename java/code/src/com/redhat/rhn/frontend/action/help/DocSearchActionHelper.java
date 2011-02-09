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
package com.redhat.rhn.frontend.action.help;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * DocSearchActionHelper
 * @version $Rev$
 */
public class DocSearchActionHelper {
    private static Logger log = Logger.getLogger(DocSearchActionHelper.class);

    /**
     * Redirect a search query to search.novell.com.
     * 
     * @param searchString
     */
    public static void redirectDocSearch(String searchString, HttpServletResponse response) {
        StringBuffer params = new StringBuffer();
        params.append("sortbyrelevence=true&noredirect=true&index=Documentation&x=0&y=0&query=");
        try {
            params.append(URLEncoder.encode(searchString, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.error("Unsupported Encoding.", e);
        }
        params.append("&filter=%2FProduct_and_Version%3DSUSE+Manager");
        try {
            response.sendRedirect("http://search.novell.com/qfsearch/SearchServlet?" + params);   
        } catch (IOException e) {
            log.error("Error while redirecting: ", e);
        } 
    }
}
