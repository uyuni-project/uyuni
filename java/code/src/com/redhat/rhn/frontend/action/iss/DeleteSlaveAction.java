/*
 * Copyright (c) 2013 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.iss;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.iss.IssFactory;
import com.redhat.rhn.domain.iss.IssSlave;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.manager.acl.AclManager;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * DeleteSlaveAction extends RhnAction
 *
 */
public class DeleteSlaveAction extends RhnAction {

    /** {@inheritDoc} */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm formIn,
                                 HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        if (!AclManager.hasAcl("user_role(satellite_admin)", request, null)) {
            LocalizationService ls = LocalizationService.getInstance();
            throw new PermissionException("Only satellite admins can modify allowed-slaves",
                    ls.getMessage("permission.jsp.title.iss.slave"),
                    ls.getMessage("permission.jsp.summary.general"));
        }

        DynaActionForm dynaForm = (DynaActionForm) formIn;
        Long sid = deleteSlave(mapping, dynaForm, request, response);

        return mapping.findForward("default");
    }

    private Long deleteSlave(ActionMapping mapping, DynaActionForm dynaForm,
            HttpServletRequest request, HttpServletResponse response) {

        RequestContext requestContext = new RequestContext(request);
        Long sid = requestContext.getParamAsLong(IssSlave.SID);

        boolean isNew = (IssSlave.NEW_SLAVE_ID == sid);

        IssSlave slave = null;
        if (!isNew) {
            slave = IssFactory.lookupSlaveById(sid);
            IssFactory.remove(slave);
            ActionMessages msg = new ActionMessages();
            msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
                    "message.iss_slave_removed", slave.getSlave()));
            getStrutsDelegate().saveMessages(request, msg);
        }
        return sid;
    }
}
