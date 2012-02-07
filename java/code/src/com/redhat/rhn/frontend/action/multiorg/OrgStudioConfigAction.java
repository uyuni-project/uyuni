/**
 * Copyright (c) 2012 Novell
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
package com.redhat.rhn.frontend.action.multiorg;

import com.redhat.rhn.domain.org.Credentials;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.manager.org.CredentialsFactory;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Configuration of SUSE Studio details.
 */
public class OrgStudioConfigAction extends RhnAction {

    /** {@inheritDoc} */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm formIn,
            HttpServletRequest request, HttpServletResponse response)
    throws Exception {
        RequestContext ctx = new RequestContext(request);

        // Get credentials for this organization
        Org org = ctx.lookupAndBindOrg();
        Credentials creds = CredentialsFactory.lookupByOrg(org);
        if (creds == null) {
            creds = CredentialsFactory.createNewCredentials(org);
            creds.setOrg(org);
        }
        // Bind the credentials as well
        request.setAttribute("creds", creds);

        if (ctx.isSubmitted()) {
            // Store the credentials
            creds.setType(Credentials.TYPE_STUDIO);
            creds.setUsername(request.getParameter("studioUser"));
            creds.setPassword(request.getParameter("studioKey"));
            creds.setHostname(request.getParameter("studioHost"));
            CredentialsFactory.storeCredentials(creds);

            ActionMessages msg = new ActionMessages();
            msg.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("message.org_name_updated", org.getName()));
            getStrutsDelegate().saveMessages(request, msg);
            return getStrutsDelegate().forwardParam(mapping.findForward("success"),
                    RequestContext.ORG_ID,
                    org.getId().toString());
        }
        return mapping.findForward("default");
    }
}
