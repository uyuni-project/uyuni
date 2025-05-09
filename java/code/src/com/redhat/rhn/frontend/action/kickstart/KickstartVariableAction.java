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
package com.redhat.rhn.frontend.action.kickstart;

import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.RhnValidationHelper;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.cobbler.CobblerObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * KickstartDetailsEdit extends RhnAction
 */
public abstract class KickstartVariableAction extends RhnAction {

    public static final String VARIABLES = "variables";


    /** {@inheritDoc} */
    @Override
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm formIn,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        RequestContext context = new RequestContext(request);

        String cobblerId = getCobblerId(context);

        if (isSubmitted((DynaActionForm) formIn)) {
            ValidatorError ve = processFormValues(request, (DynaActionForm) formIn,
                    cobblerId);
            if (ve != null) {
                ValidatorError[] verr = {ve};
                getStrutsDelegate().saveMessages(request,
                        RhnValidationHelper.validatorErrorToActionErrors(verr));
            }

        }

        setupFormValues(context, (DynaActionForm) formIn, cobblerId);
        request.setAttribute(getObjectString(), request.getParameter(getObjectString()));

        return getStrutsDelegate().forwardParams(
                mapping.findForward(RhnHelper.DEFAULT_FORWARD),
                request.getParameterMap());

    }

    /**
     * {@inheritDoc}
     */
    protected void setupFormValues(RequestContext ctx,
            DynaActionForm form, String cId) {
        CobblerObject cobj = getCobblerObject(cId, ctx.getCurrentUser());
        form.set(VARIABLES, StringUtil.convertMapToString(cobj.getKsMeta().get(), "\n"));
    }


    /**
     * {@inheritDoc}
     */
    protected ValidatorError processFormValues(HttpServletRequest request,
            DynaActionForm form,
            String cId) {

        RequestContext ctx = new RequestContext(request);

        try {

            CobblerObject cobj = getCobblerObject(cId, ctx.getCurrentUser());
            Map<String, Object> convertedMap = new HashMap<>(StringUtil.convertOptionsToMap(
                    (String) form.get(VARIABLES),
                    "kickstart.jsp.error.invalidvariable",
                    "\n"));
            cobj.setKsMeta(Optional.of(convertedMap));
            cobj.save();

            return null;
        }
        catch (ValidatorException ve) {
            return ve.getResult().getErrors().get(0);
        }
    }

    /**
     *
     * @param context the request context
     * @return the cobbler id
     */
    protected abstract String getCobblerId(RequestContext context);

    protected abstract String getObjectString();


    /**
     * Get the CobblerObject that we'll use to set the ksmeta data
     * @param cobblerId the cobbler Id
     * @param user the user requesting
     * @return the CobblerObject (either a profile or distro)
     */
    protected abstract CobblerObject getCobblerObject(String cobblerId, User user);


}
