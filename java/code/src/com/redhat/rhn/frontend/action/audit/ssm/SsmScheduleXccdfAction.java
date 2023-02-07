/*
 * Copyright (c) 2012 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.audit.ssm;

import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.RhnValidationHelper;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * SSM OpenSCAP XCCDF scanning.
 */
public class SsmScheduleXccdfAction extends BaseSsmScheduleXccdfAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm formIn,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        DynaActionForm form = (DynaActionForm) formIn;

        if (isSubmitted(form)) {
            ActionErrors errors = RhnValidationHelper.validateDynaActionForm(this, form);
            if (errors.isEmpty()) {
                return getStrutsDelegate().forwardParams(mapping.findForward("submit"),
                        request.getParameterMap());
            }
            getStrutsDelegate().saveMessages(request, errors);
        }
        setupDatePicker(request, form);
        setupListHelper(request);
        return getStrutsDelegate().forwardParams(
            mapping.findForward(RhnHelper.DEFAULT_FORWARD),
            request.getParameterMap());
    }
}
