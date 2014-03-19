/**
 * Copyright (c) 2014 SUSE
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

package com.redhat.rhn.frontend.action.renderers;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.manager.setup.MirrorCredentials;
import com.redhat.rhn.manager.setup.SetupWizardManager;
import com.redhat.rhn.manager.setup.SubscriptionDto;

/**
 * Asynchronously render page content for managing mirror credentials.
 */
public class MirrorCredentialsRenderer {

    // The logger for this class
    private static Logger logger = Logger.getLogger(MirrorCredentialsRenderer.class);

    // Attribute keys
    private static final String ATTRIB_MIRRCREDS = "mirrorCredsList";
    private static final String ATTRIB_CREDS_ID = "credentialsId";
    private static final String ATTRIB_SUCCESS = "success";
    private static final String ATTRIB_SUBSCRIPTIONS = "subscriptions";

    // URL of the page to render
    private static final String CREDS_LIST_URL = "/WEB-INF/pages/admin/setup/mirror-credentials-list.jsp";
    private static final String CREDS_VERIFY_URL = "/WEB-INF/pages/admin/setup/mirror-credentials-verify.jsp";
    private static final String LIST_SUBSCRIPTIONS_URL = "/WEB-INF/pages/admin/setup/modal-subscriptions-body.jsp";

    /**
     * Add a new pair of credentials and re-render the whole list.
     * @param user username for new credentials
     * @param password password for new credentials
     * @throws IOException
     * @throws ServletException
     */
    public String saveCredentials(Long id, String email, String user, String password) throws ServletException, IOException {
        // Find the current user
        WebContext webContext = WebContextFactory.get();
        HttpServletRequest request = webContext.getHttpServletRequest();
        RequestContext rhnContext = new RequestContext(request);
        User webUser = rhnContext.getCurrentUser();

        MirrorCredentials creds;
        if (id != null) {
            // Save an existing pair of credentials
            creds = SetupWizardManager.findMirrorCredentials(id);
            creds.setEmail(email);
            // User and password are mandatory, but can be left unchanged
            if (user != null && !user.isEmpty()) {
                creds.setUser(user);
            }
            if (password != null && !password.isEmpty()) {
                creds.setPassword(password);
            }
        }
        else {
            // Add a new pair of credentials
            creds = new MirrorCredentials(email, user, password);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Saving credentials: " + user + ":" + password);
        }

        // TODO: Handle errors
        @SuppressWarnings("unused")
        ValidatorError[] errors = SetupWizardManager.storeMirrorCredentials(creds, webUser, request);
        return renderCredentials();
    }

    /**
     * Delete a pair of credentials given by ID.
     * @param id ID of the credentials to delete
     * @return rendered list of credentials
     * @throws ServletException
     * @throws IOException
     */
    public String deleteCredentials(long id) throws ServletException, IOException {
        // Find the current user
        WebContext webContext = WebContextFactory.get();
        HttpServletRequest request = webContext.getHttpServletRequest();
        RequestContext rhnContext = new RequestContext(request);
        User webUser = rhnContext.getCurrentUser();

        // Delete the credentials
        if (logger.isDebugEnabled()) {
            logger.debug("Deleting credentials: " + id);
        }
        SetupWizardManager.deleteMirrorCredentials(id, webUser, request);
        return renderCredentials();
    }

    /**
     * Make primary credentials for a given ID.
     * @param id ID of credentials to make the primary ones
     * @return rendered list of credentials
     * @throws ServletException
     * @throws IOException
     */
    public String makePrimaryCredentials(long id) throws ServletException, IOException {
        // Find the current user
        WebContext webContext = WebContextFactory.get();
        HttpServletRequest request = webContext.getHttpServletRequest();
        RequestContext rhnContext = new RequestContext(request);
        User webUser = rhnContext.getCurrentUser();

        // Make primary credentials
        if (logger.isDebugEnabled()) {
            logger.debug("Make primary credentials: " + id);
        }
        SetupWizardManager.makePrimaryCredentials(id, webUser, request);
        return renderCredentials();
    }

    /**
     * Render the list of mirror credentials.
     * @throws IOException
     * @throws ServletException
     */
    public String renderCredentials() throws ServletException, IOException {
        WebContext webContext = WebContextFactory.get();
        HttpServletRequest request = webContext.getHttpServletRequest();

        // Find mirror credentials
        List<MirrorCredentials> creds = SetupWizardManager.findMirrorCredentials();
        if (logger.isDebugEnabled()) {
            logger.debug("Found " + creds.size() + " pairs of credentials");
        }
        request.setAttribute(ATTRIB_MIRRCREDS, creds);

        // Set the "parentUrl" for the form (in rl:listset)
        request.setAttribute(ListTagHelper.PARENT_URL, "");
        HttpServletResponse response = webContext.getHttpServletResponse();
        return RendererHelper.renderRequest(CREDS_LIST_URL, request, response);
    }

    /**
     * Get subscriptions for credentials and asynchronously render the page fragment.
     * @throws IOException
     * @throws ServletException
     */
    public String verifyCredentials(Long id, boolean refresh) throws ServletException, IOException {
        WebContext webContext = WebContextFactory.get();
        HttpServletRequest request = webContext.getHttpServletRequest();

        // Load credentials for given ID and the subscriptions
        MirrorCredentials creds = SetupWizardManager.findMirrorCredentials(id);
        if (logger.isDebugEnabled()) {
            logger.debug("Verify credentials: " + creds.getUser());
        }

        // Download subscriptions or get from session cache
        List<SubscriptionDto> subs;

        // Download if forced refresh or status unknown
        if (refresh || SetupWizardManager.verificationStatusUnknown(creds, request)) {
            subs = SetupWizardManager.getSubscriptions(creds, request, true);
        }
        else {
            subs = SetupWizardManager.getSubscriptions(creds, request, false);
        }
        request.setAttribute(ATTRIB_SUCCESS, subs != null);
        request.setAttribute(ATTRIB_CREDS_ID, id);

        // Set the "parentUrl" for the form (in rl:listset)
        request.setAttribute(ListTagHelper.PARENT_URL, "");
        HttpServletResponse response = webContext.getHttpServletResponse();
        return RendererHelper.renderRequest(CREDS_VERIFY_URL, request, response);
    }

    /**
     * Get subscriptions for credentials and asynchronously render the page fragment.
     * @throws IOException
     * @throws ServletException
     */
    public String listSubscriptions(Long id) throws ServletException, IOException {
        WebContext webContext = WebContextFactory.get();
        HttpServletRequest request = webContext.getHttpServletRequest();

        // Load credentials for given ID and the subscriptions
        MirrorCredentials creds = SetupWizardManager.findMirrorCredentials(id);
        if (logger.isDebugEnabled()) {
            logger.debug("List subscriptions: " + creds.getUser());
        }

        // Download subscriptions only if verification status is unknown
        List<SubscriptionDto> subs = SetupWizardManager.getSubscriptions(creds, request, false);
        if (subs == null && SetupWizardManager.verificationStatusUnknown(creds, request)) {
            subs = SetupWizardManager.getSubscriptions(creds, request, true);
        }
        request.setAttribute(ATTRIB_SUBSCRIPTIONS, subs);

        HttpServletResponse response = webContext.getHttpServletResponse();
        return RendererHelper.renderRequest(LIST_SUBSCRIPTIONS_URL, request, response);
    }
}
