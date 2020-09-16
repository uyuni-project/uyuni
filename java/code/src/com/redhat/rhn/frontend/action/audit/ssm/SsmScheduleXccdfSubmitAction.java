/**
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

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.util.DatePicker;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.StrutsDelegate;

/**
 * SSM OpenSCAP XCCDF scanning.
 * @version $Rev$
 */
public class SsmScheduleXccdfSubmitAction extends BaseSsmScheduleXccdfAction {

    /**
     * {@inheritDoc}
     */
    public ActionForward execute(ActionMapping mapping,
            ActionForm formIn,
            HttpServletRequest request,
            HttpServletResponse response) {
        StrutsDelegate strutsDelegate = getStrutsDelegate();
        DynaActionForm form = (DynaActionForm) formIn;

        String params = (String) form.get(PARAMS);
        String path = (String) form.get(PATH);
        Date earliest = strutsDelegate.readScheduleDate(form, DATE, DatePicker.YEAR_RANGE_POSITIVE);

        request.setAttribute(LOCALIZED_DATE,
                LocalizationService.getInstance().formatDate(earliest));
        request.setAttribute(READONLY, TRUE);

        setupDatePicker(request, form);
        setupListHelper(request);

        return strutsDelegate.forwardParams(
                mapping.findForward(RhnHelper.DEFAULT_FORWARD),
                request.getParameterMap());
    }
}
