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

package com.redhat.rhn.frontend.action.satellite;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.manager.setup.MirrorCredentials;
import com.redhat.rhn.manager.setup.SetupWizardManager;

/**
 * This is for now just a generic RhnAction used for all pages of the wizard.
 */
public class SetupWizardAction extends RhnAction {

    // Logger for this class
    private static Logger logger = Logger.getLogger(SetupWizardAction.class);

    // Attribute keys
    private final static String ATTRIB_MIRRCREDS_LIST = "mirrorCredsList";

    /** {@inheritDoc} */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm formIn,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        DynaActionForm form = (DynaActionForm) formIn;
        // RequestContext ctx = new RequestContext(request);

        String path = mapping.getPath();
        logger.debug("Current path: " + path);
        if (path.endsWith("MirrorCredentials")) {
            List<MirrorCredentials> creds = SetupWizardManager.getMirrorCredentials();
            logger.debug("Found " + creds.size() + " pairs of credentials");
            request.setAttribute(ListTagHelper.PARENT_URL, "");
            request.setAttribute(ATTRIB_MIRRCREDS_LIST, creds);
        }

        // Do nothing for now
        if (isSubmitted(form)) {
        }
        else {
        }

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }
}
