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
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;

/**
 * Asynchronously render page content for managing mirror credentials.
 */
public class SetupWizardRenderer {

    // The logger for this class
    private static Logger logger = Logger.getLogger(SetupWizardRenderer.class);

    // URL of the page to render
    private static final String HTTP_PROXY_URL = "/WEB-INF/pages/admin/setup/http-proxy.jsp";
    private static final String MIRR_CREDS_URL = "/WEB-INF/pages/admin/setup/mirror-credentials.jsp";
    private static final String SUSE_PRODUCTS_URL = "/WEB-INF/pages/admin/setup/suse-products.jsp";
    private static final String SYNC_SCHEDULE_URL = "/WEB-INF/pages/admin/setup/sync-schedule.jsp";
    private static final String SYSTEM_GROUPS_URL = "/WEB-INF/pages/admin/setup/system-groups.jsp";
    private static final String ACTIVATION_KEYS_URL = "/WEB-INF/pages/admin/setup/activation-keys.jsp";
    private static final String BOOTSTRAP_URL = "/WEB-INF/pages/admin/setup/bootstrap.jsp";

    // Wizard steps mapping
    private HashMap<Long, String> wizardSteps = null;

    /**
     * Render the mirror credentials page.
     * @throws IOException
     * @throws ServletException
     */
    public String renderPage(long id) throws ServletException, IOException {
        // Setup wizard steps mapping if necessary
        if (wizardSteps == null) {
            setupWizardSteps();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("renderWizardPage(" + id + "): " + wizardSteps.get(id));
        }
        WebContext webContext = WebContextFactory.get();
        HttpServletRequest request = webContext.getHttpServletRequest();
        HttpServletResponse response = webContext.getHttpServletResponse();
        return RendererHelper.renderRequest(wizardSteps.get(id), request, response);
    }

    /**
     * Setup mapping between tab IDs and jsp URLs.
     */
    private void setupWizardSteps() {
        wizardSteps = new HashMap<Long, String>();
        wizardSteps.put(0L, HTTP_PROXY_URL);
        wizardSteps.put(1L, MIRR_CREDS_URL);
        wizardSteps.put(2L, SUSE_PRODUCTS_URL);
        wizardSteps.put(3L, SYNC_SCHEDULE_URL);
        wizardSteps.put(4L, SYSTEM_GROUPS_URL);
        wizardSteps.put(5L, ACTIVATION_KEYS_URL);
        wizardSteps.put(6L, BOOTSTRAP_URL);
    }
}
