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
package com.redhat.rhn.frontend.action.systems.monitoring;

import com.redhat.rhn.domain.monitoring.Probe;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.manager.monitoring.ModifyProbeCommand;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Base class for actions that edit individual probes
 *
 * @version $Rev: 53910 $
 */
public abstract class BaseProbeEditAction extends BaseProbeAction {


    /** {@inheritDoc} */
    public final ActionForward execute(ActionMapping mapping, ActionForm formIn,
            HttpServletRequest req, HttpServletResponse resp) {
        DynaActionForm form = (DynaActionForm) formIn;

        RequestContext rctx = new RequestContext(req);
        Probe probe = rctx.lookupProbe();
        User user = rctx.getCurrentUser();

        if (isSubmitted(form)) {
            ModifyProbeCommand cmd = new ModifyProbeCommand(user, probe);
            if (editProbe(cmd, form, req)) {
                createSuccessMessage(req, "probeedit.probesaved", probe.getDescription());
                Map<String, Object> params = new HashMap<String, Object>();
                addSuccessParams(rctx, params, cmd.getProbe());
                return getStrutsDelegate().forwardParams(mapping.findForward("success"),
                        params);
            }
        }
        else {
            // Initialize the form
            form.set(NOTIFICATION, probe.isNotifyCritical());
            form.set(DESCR, probe.getDescription());
            form.set(NOTIFICATION_INTERVAL_MIN, probe.getNotificationIntervalMinutes());
            if (probe.getContactGroup() != null) {
                form.set(CONTACT_GROUP_ID, probe.getContactGroup().getId());
            }
            form.set(CHECK_INTERVAL_MIN, probe.getCheckIntervalMinutes());
        }
        req.setAttribute("probe", probe);
        addAttributes(rctx);
        setIntervals(req);
        setContactGroups(req, user.getOrg());
        setParamValueList(req, probe, probe.getCommand(), isSubmitted(form));
        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    protected abstract void addSuccessParams(RequestContext rctx, Map params,
            Probe probe);

    /**
     * @param rctx
     */
    protected abstract void addAttributes(RequestContext rctx);

}
