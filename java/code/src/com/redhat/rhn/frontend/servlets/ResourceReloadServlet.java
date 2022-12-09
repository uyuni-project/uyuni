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

import com.redhat.rhn.common.localization.LocalizationService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.OutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A basic servlet class that reloads resources.  For now this just reloads the
 * LocalizationService resource files.
 *
 */
public class ResourceReloadServlet extends HttpServlet {
    private static final Logger LOG = LogManager.getLogger(ResourceReloadServlet.class);

    /**
     * executed when a get request happens
     *
     * @param request the request object
     * @param response the response object
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        reloadStringResources(response);
    }

    /**
     * executed when a post request happens
     *
     * @param request the request object
     * @param response the response object
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        reloadStringResources(response);
    }

    private void reloadStringResources(HttpServletResponse response) {

        response.setContentType("text/plain");
        boolean reloaded = LocalizationService.getInstance().reloadResourceFiles();
        try {
            OutputStream out = response.getOutputStream();
            String results = "Reloaded resource files: [" + reloaded + "]";
            response.setContentLength(results.length());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Reloaded result [{}]", results);
            }
            out.write(results.getBytes());
            out.flush();
        }
        // Lazy here since this is just a dev Servlet.
        catch (Exception e) {
            LOG.error("Exception trying to reload.", e);
        }
    }
}
