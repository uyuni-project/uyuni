/**
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.ssm;

import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * IndexAction extends RhnAction
 * @version $Rev: 1 $
 */
public class MiscIndexAction extends RhnAction {

    /** {@inheritDoc} */
    public ActionForward execute(ActionMapping mapping,
                                  ActionForm formIn,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {

        RequestContext context = new RequestContext(request);
        DynaActionForm daForm = (DynaActionForm)formIn;

        if (context.isSubmitted()) {
            if (!(daForm.getString("notify").equals("no_change") &&
                    daForm.getString("summary").equals("no_change") && daForm.getString(
                    "update").equals("no_change"))) {
                request.setAttribute("notify", daForm.getString("notify"));
                request.setAttribute("summary", daForm.getString("summary"));
                request.setAttribute("update", daForm.getString("update"));
                request.setAttribute("no_execute", true);
                return mapping.findForward(RhnHelper.CONFIRM_FORWARD);
            }
        }

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

}
