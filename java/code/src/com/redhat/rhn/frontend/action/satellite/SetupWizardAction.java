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
package com.redhat.rhn.frontend.action.satellite;

import com.redhat.rhn.domain.iss.IssFactory;
import com.redhat.rhn.frontend.nav.NavCache;
import com.redhat.rhn.frontend.nav.NavNode;
import com.redhat.rhn.frontend.nav.NavTree;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.taskomatic.TaskoFactory;
import com.redhat.rhn.taskomatic.domain.TaskoRun;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is for now just a generic RhnAction used for all pages of the wizard.
 */
public class SetupWizardAction extends RhnAction {
    /** Tab menu XML. */
    private static final String NAVIGATION_XML_PATH = "/WEB-INF/nav/setup_wizard.xml";

    // page attributes
    private static final String NEXT_STEP_ATTRIBUTE = "nextStep";
    private static final String PREVIOUS_STEP_ATTRIBUTE = "previousStep";
    private static final String TOTAL_STEPS_ATTRIBUTE = "totalSteps";
    private static final String CURRENT_STEP_ATTRIBUTE = "currentStep";
    private static final String REFRESH_NEEDED = "refreshNeeded";
    private static final String ISS_MASTER = "issMaster";
    private static final String REFRESH_RUNNING = "refreshRunning";

    // Logger for this class
    private static Logger logger = Logger.getLogger(SetupWizardAction.class);

    /**
      * {@inheritDoc}
      * @throws Exception if parsing of navigation XML fails
      */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm formIn,
            HttpServletRequest request, HttpServletResponse response)
        throws Exception {

        setAttributes(mapping, request);
        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    /**
     * Sets common attributes needed by setup-tab-footer.jspf.
     * @param mapping the Action mapping object
     * @param request current request object
     * @throws Exception if parsing of navigation XML fails
     */
    private void setAttributes(ActionMapping mapping, HttpServletRequest request)
        throws Exception {
        String path = mapping.getPath();
        logger.debug("Current path: " + path);
        NavTree tree =
                NavCache.getTree(getServlet().getServletContext().getResource(
                        NAVIGATION_XML_PATH));
        List<NavNode> nodes = (List<NavNode>) tree.getNodes();
        int nodeIndex = getNodeIndex(path, nodes);
        int totalSteps = nodes.size();

        request.setAttribute(CURRENT_STEP_ATTRIBUTE, nodeIndex + 1);

        request.setAttribute(TOTAL_STEPS_ATTRIBUTE, totalSteps);

        request.setAttribute(PREVIOUS_STEP_ATTRIBUTE,
                nodeIndex == 0 ? null : nodes.get(nodeIndex - 1).getURLs().get(0));

        request.setAttribute(NEXT_STEP_ATTRIBUTE,
                nodeIndex == (totalSteps - 1) ? null : nodes.get(nodeIndex + 1).getURLs()
                        .get(0));

        request.setAttribute(ISS_MASTER, IssFactory.getCurrentMaster() == null);
        ContentSyncManager csm = new ContentSyncManager();
        request.setAttribute(REFRESH_NEEDED, csm.isRefreshNeeded(null));

        TaskoRun latestRun = TaskoFactory.getLatestRun("mgr-sync-refresh-bunch");
        request.setAttribute(REFRESH_RUNNING,
                latestRun != null && latestRun.getEndTime() == null);
    }

    /**
     * Looks for a URL in a list of navigation nodes, returns the index if any.
     * @param path the path
     * @param nodes the navigation nodes
     * @return the index
     * @throws IllegalArgumentException if path is not found
     */
    private int getNodeIndex(String path, List<NavNode> nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            NavNode node = nodes.get(i);
            List<String> urls = (List<String>) node.getURLs();
            for (String url : urls) {
                if (url.endsWith(path + ".do")) {
                    return i;
                }
            }
        }
        throw new IllegalArgumentException("SetupWizardAction: path " +
                path + " not found");
    }
}
