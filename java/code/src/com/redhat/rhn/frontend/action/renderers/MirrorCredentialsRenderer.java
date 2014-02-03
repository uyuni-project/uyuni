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

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.manager.setup.MirrorCredentials;
import com.redhat.rhn.manager.setup.SetupWizardManager;
import com.suse.manager.model.ncc.Subscription;

/**
 * Asynchronously render page content for managing mirror credentials.
 */
public class MirrorCredentialsRenderer {

    // The logger for this class
    private static Logger logger = Logger.getLogger(MirrorCredentialsRenderer.class);

    // Attribute keys
    private static final String ATTRIB_MIRRCREDS = "mirrorCredsList";
    private static final String ATTRIB_SUCCESS = "success";

    // URL of the page to render
    private static final String CREDENTIALS_URL = "/WEB-INF/pages/admin/setup/mirror-credentials-list.jsp";
    private static final String SUBSCRIPTIONS_URL = "/WEB-INF/pages/admin/setup/mirror-credentials-verify.jsp";

    /**
     * Add a new pair of credentials and re-render the whole list.
     * @param user username for new credentials
     * @param password password for new credentials
     * @throws IOException
     * @throws ServletException
     */
    public String addCredentials(String email, String user, String password) throws ServletException, IOException {
        // Find the current user
        WebContext webContext = WebContextFactory.get();
        HttpServletRequest request = webContext.getHttpServletRequest();
        RequestContext rhnContext = new RequestContext(request);
        User webUser = rhnContext.getCurrentUser();

        // Store the new mirror credentials
        logger.debug("Adding credentials: " + user + ":" + password);
        MirrorCredentials newCreds = new MirrorCredentials(email, user, password);
        SetupWizardManager.storeMirrorCredentials(newCreds, webUser);
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
        SetupWizardManager.deleteMirrorCredentials(id, webUser);
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
        return RendererHelper.renderRequest(CREDENTIALS_URL, request, response);
    }

    /**
     * Get subscriptions for credentials and asynchronously render the page fragment.
     * @throws IOException
     * @throws ServletException
     */
    public String renderSubscriptions(Long id) throws ServletException, IOException {
        logger.debug("renderSubscriptions()");
        WebContext webContext = WebContextFactory.get();
        HttpServletRequest request = webContext.getHttpServletRequest();

        // Load credentials for given ID and the subscriptions
        MirrorCredentials creds = SetupWizardManager.findMirrorCredentials(id);
        if (logger.isDebugEnabled()) {
            logger.debug("List subscriptions: " + creds.getUser());
        }
        List<Subscription> subs = SetupWizardManager.listSubscriptions(creds);
        request.setAttribute(ATTRIB_SUCCESS, subs != null);

        // Set the "parentUrl" for the form (in rl:listset)
        request.setAttribute(ListTagHelper.PARENT_URL, "");
        HttpServletResponse response = webContext.getHttpServletResponse();
        return RendererHelper.renderRequest(SUBSCRIPTIONS_URL, request, response);
    }
}
