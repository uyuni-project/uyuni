/**
 * Copyright (c) 2014 Red Hat, Inc.
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

import com.redhat.rhn.domain.org.usergroup.UserExtGroup;
import com.redhat.rhn.domain.org.usergroup.UserGroupFactory;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * ExtGroupDeleteAction
 * @version $Rev$
 */
public class ExtGroupDeleteAction extends RhnAction {

    /** {@inheritDoc} */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm formIn,
            HttpServletRequest request, HttpServletResponse response) {

        RequestContext ctx = new RequestContext(request);
        Long gid = ctx.getRequiredParamAsLong("gid");
        UserExtGroup extGroup = UserGroupFactory.lookupExtGroupById(gid);
        ctx.copyParamToAttributes("gid");
        request.setAttribute("group", extGroup);

        if (ctx.isSubmitted()) {
            String label = extGroup.getLabel();
            UserGroupFactory.delete(extGroup);
            createSuccessMessage(request, "message.extgroup.deleted", label);
            return mapping.findForward("success");
        }

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

}
