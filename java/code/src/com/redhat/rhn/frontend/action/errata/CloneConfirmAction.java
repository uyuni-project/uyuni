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
package com.redhat.rhn.frontend.action.errata;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.common.RhnSetAction;
import com.redhat.rhn.frontend.dto.ErrataOverview;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * CloneConfirmAction
 */
public class CloneConfirmAction extends RhnSetAction {

    /** This class reuses code in the channel assignment page */
    private ChannelAction channelAction = new ChannelAction();

    /** {@inheritDoc} */
    public ActionForward clone(ActionMapping mapping,
                                 ActionForm formIn,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        RequestContext rctx = new RequestContext(request);
        User user = rctx.getCurrentUser();

        //Make sure at least one channel is selected
        RhnSet channelSet = updateSet(request);
        if (channelSet.isEmpty()) {
            ActionErrors errors = new ActionErrors();
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errata.publish.nochannelsselected"));
            addErrors(request, errors);
            return mapping.findForward("failure");
        }

        DataResult<ErrataOverview> dr = ErrataManager.selectedForCloning(user, null);

        channelSet.getElements().forEach(c -> ErrataManager.cloneChannelErrata(dr, c.getElement(), user));

        RhnSet set = RhnSetDecl.ERRATA_CLONE.get(user);
        set.clear();
        RhnSetManager.store(set);

        return mapping.findForward("success");
    }

    protected void processMethodKeys(Map<String, String> map) {
        map.put("deleteconfirm.jsp.confirm", "clone");
    }

    @Override
    protected RhnSetDecl getSetDecl() {
        return channelAction.getSetDecl();
    }

    @Override
    protected DataResult getDataResult(User user, ActionForm formIn, HttpServletRequest request) {
        return channelAction.getDataResult(user, formIn, request);
    }

    @Override
    protected void processParamMap(ActionForm form, HttpServletRequest request, Map<String, Object> params) {
        channelAction.processParamMap(form, request, params);
    }
}
