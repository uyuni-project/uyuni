/**
 * Copyright (c) 2012 SUSE LLC
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
package com.redhat.rhn.frontend.action.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;

/**
 * Delete credentials for external systems or APIs.
 */
public class UserCredentialsDeleteAction extends RhnAction {

    private static final String ATTRIB_CREDS = "creds";

    /** {@inheritDoc} */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm formIn,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        RequestContext ctx = new RequestContext(request);

        // Lookup this user's credentials
        User user = ctx.getCurrentUser();
        Credentials creds = CredentialsFactory.lookupStudioCredentials(user);
        if (creds == null) {
            // Create new credentials if necessary
            creds = CredentialsFactory.createStudioCredentials(user);
        }
        request.setAttribute(ATTRIB_CREDS, creds);

        if (ctx.wasDispatched("credentials.jsp.delete.dispatch")) {
            // Delete credentials from DB
            CredentialsFactory.removeCredentials(creds);
            ActionMessages messages = new ActionMessages();
            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
                    "credentials.message.deleted"));
            getStrutsDelegate().saveMessages(request, messages);
            return mapping.findForward("success");
        }
        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }
}
