/*
 * Copyright (c) 2014 SUSE LLC
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
package com.redhat.rhn.frontend.action.renderers.setupwizard;

import com.redhat.rhn.frontend.action.renderers.RendererHelper;
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.setup.MirrorCredentialsDto;
import com.redhat.rhn.manager.setup.MirrorCredentialsManager;
import com.redhat.rhn.manager.setup.MirrorCredentialsNotUniqueException;
import com.redhat.rhn.manager.setup.SubscriptionDto;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Asynchronously render page content for managing mirror credentials.
 */
public class MirrorCredentialsRenderer {

    // The logger for this class
    private static Logger logger = LogManager.getLogger(MirrorCredentialsRenderer.class);

    // Attribute keys
    private static final String ATTRIB_MIRRCREDS = "credentials";
    private static final String ATTRIB_CREDS_ID = "credentialsId";
    private static final String ATTRIB_SUCCESS = "success";
    private static final String ATTRIB_SUBSCRIPTIONS = "subscriptions";

    // Save credentials return codes
    private static final String CRED_OK = "ok";
    private static final String CRED_ERROR_DUPLICATE = "mirror-credentials-error-duplicate";

    // URL of the page to render
    private static final String CREDS_LIST_URL =
            "/WEB-INF/pages/admin/setup/mirror-credentials-list.jsp";
    private static final String CREDS_VERIFY_URL =
            "/WEB-INF/pages/admin/setup/mirror-credentials-verify.jsp";
    private static final String LIST_SUBSCRIPTIONS_URL =
            "/WEB-INF/pages/admin/setup/modal-subscriptions-body.jsp";

    /**
     * Add or edit pair of credentials and re-render the whole list.
     * @param request the request
     * @param id ID of the credential to edit or null for new mirror credential
     * @param user username for new credentials
     * @param password password for new credentials
     * @return the rendered fragment
     * @throws ServletException in case of rendering errors
     * @throws IOException in case something really bad happens
     * @throws ContentSyncException in case of problems storing the credentials
     */
    public String saveCredentials(HttpServletRequest request, Long id, String user, String password)
        throws ServletException, IOException, ContentSyncException {

        MirrorCredentialsDto creds;
        MirrorCredentialsManager credsManager = new MirrorCredentialsManager();
        if (id != null) {
            // Save an existing pair of credentials
            creds = credsManager.findMirrorCredentials(id);

            // User and password are mandatory, but can be left unchanged
            if (!StringUtils.isBlank(user)) {
                creds.setUser(user);
            }
            if (!StringUtils.isBlank(password)) {
                creds.setPassword(password);
            }
        }
        else {
            // Add a new pair of credentials
            creds = new MirrorCredentialsDto(user, password);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Saving credentials: {}:{}", user, password);
        }
        try {
            credsManager.storeMirrorCredentials(creds, request);
        }
        catch (MirrorCredentialsNotUniqueException e) {
            if (logger.isDebugEnabled()) {
                logger.info("Mirror credentials not saved: Username already exists");
            }
            return CRED_ERROR_DUPLICATE;
        }
        return CRED_OK;
    }

    /**
     * Delete a pair of credentials given by ID.
     * @param request the request
     * @param response the response
     * @param id ID of the credentials to delete
     * @return the rendered fragment
     * @throws ServletException in case of rendering errors
     * @throws IOException in case something really bad happens
     * @throws ContentSyncException in case of problems making new primary creds
     */
    public String deleteCredentials(HttpServletRequest request, HttpServletResponse response, long id)
            throws ServletException, IOException,
            ContentSyncException {
        // Delete the credentials
        if (logger.isDebugEnabled()) {
            logger.debug("Deleting credentials: {}", id);
        }
        MirrorCredentialsManager credsManager = new MirrorCredentialsManager();
        credsManager.deleteMirrorCredentials(id, request);
        return renderCredentials(request, response);
    }

    /**
     * Make primary credentials for a given ID.
     * @param request the request
     * @param response the response
     * @param id ID of credentials to make the primary ones
     * @return the rendered fragment
     * @throws ServletException in case of rendering errors
     * @throws IOException in case something really bad happens
     * @throws ContentSyncException in case the credentials cannot be found
     */
    public String makePrimaryCredentials(HttpServletRequest request, HttpServletResponse response, long id)
            throws ServletException, IOException,
            ContentSyncException {
        // Make primary credentials
        if (logger.isDebugEnabled()) {
            logger.debug("Make primary credentials: {}", id);
        }
        MirrorCredentialsManager credsManager = new MirrorCredentialsManager();
        credsManager.makePrimaryCredentials(id);
        return renderCredentials(request, response);
    }

    /**
     * Render the mirror credentials set of panels.
     * @param request the request
     * @param response the response
     * @return the rendered fragment
     * @throws ServletException in case of rendering errors
     * @throws IOException in case something really bad happens
     */
    public String renderCredentials(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Find mirror credentials
        MirrorCredentialsManager credsManager = new MirrorCredentialsManager();
        List<MirrorCredentialsDto> creds = credsManager.findMirrorCredentials();
        if (logger.isDebugEnabled()) {
            logger.debug("Found {} pairs of credentials", creds.size());
        }
        request.setAttribute(ATTRIB_MIRRCREDS, creds);
        return RendererHelper.renderRequest(CREDS_LIST_URL, request, response);
    }

    /**
     * Get subscriptions for credentials and asynchronously render the page fragment.
     * @param id ID of the credentials to verify
     * @param refresh force a cache refresh
     * @param request the request
     * @param response the response
     * @return the rendered fragment
     * @throws ServletException in case of rendering errors
     * @throws IOException in case something really bad happens
     */
    public String verifyCredentials(HttpServletRequest request, HttpServletResponse response, Long id, boolean refresh)
        throws ServletException, IOException {
        // Load credentials for given ID and the subscriptions
        MirrorCredentialsManager credsManager = new MirrorCredentialsManager();
        MirrorCredentialsDto creds = credsManager.findMirrorCredentials(id);
        if (logger.isDebugEnabled()) {
            logger.debug("Verify credentials: {}", creds.getUser());
        }

        // Download subscriptions or get from session cache
        List<SubscriptionDto> subs;

        // Download if forced refresh or status unknown
        subs = credsManager.getSubscriptions(creds, request, refresh);

        request.setAttribute(ATTRIB_SUCCESS, subs != null);
        request.setAttribute(ATTRIB_CREDS_ID, id);
        return RendererHelper.renderRequest(CREDS_VERIFY_URL, request, response);
    }

    /**
     * Get subscriptions for credentials and asynchronously render the page fragment.
     * @param id ID of the credentials to use for listing
     * @param request the request
     * @param response the response
     * @return the rendered fragment
     * @throws ServletException in case of rendering errors
     * @throws IOException in case something really bad happens
     */
    public String listSubscriptions(HttpServletRequest request, HttpServletResponse response, Long id)
            throws ServletException, IOException {
        // Load credentials for given ID and the subscriptions
        MirrorCredentialsManager credsManager = new MirrorCredentialsManager();
        MirrorCredentialsDto creds = credsManager.findMirrorCredentials(id);
        if (logger.isDebugEnabled()) {
            logger.debug("List subscriptions: {}", creds.getUser());
        }
        List<SubscriptionDto> subs = credsManager.getSubscriptions(creds, request, false);
        request.setAttribute(ATTRIB_SUBSCRIPTIONS, subs);
        return RendererHelper.renderRequest(LIST_SUBSCRIPTIONS_URL, request, response);
    }
}
