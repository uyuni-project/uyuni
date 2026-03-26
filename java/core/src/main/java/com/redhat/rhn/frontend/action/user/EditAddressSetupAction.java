/*
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
package com.redhat.rhn.frontend.action.user;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.user.Address;
import com.redhat.rhn.domain.user.AddressImpl;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.RhnValidationHelper;
import com.redhat.rhn.manager.user.UserManager;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * UserPreferencesAction, edit action for user detail page
 */
public class EditAddressSetupAction extends RhnAction {

    /** {@inheritDoc} */
    @Override
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm formIn,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        RequestContext requestContext = new RequestContext(request);

        DynaActionForm form = (DynaActionForm)formIn;
        Long uid = requestContext.getRequiredParam("uid");

        User user = UserManager.lookupUser(requestContext.getCurrentUser(), uid);
        request.setAttribute(RhnHelper.TARGET_USER, user);
        form.set("uid", user.getId());
        if (!RhnValidationHelper.getFailedValidation(request)) {
            Address address = user.getEnterpriseUser().getAddress();
            if (address == null) {
                address = new AddressImpl();
            }
            form.set("address1", address.getAddress1());
            form.set("address2", address.getAddress2());
            form.set("phone", address.getPhone());
            form.set("fax", address.getFax());
            form.set("city", address.getCity());
            form.set("state", address.getState());
            form.set("country", address.getCountry());
            form.set("zip", address.getZip());
        }
        form.set("typedisplay",
            LocalizationService.getInstance().
                getMessage("address type M"));
        // set the Country map
        request.setAttribute(
            "availableCountries", UserActionHelper.getCountries());

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

}
