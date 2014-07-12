/**
 * Copyright (c) 2012--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.systems.audit;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.redhat.rhn.domain.audit.ScapFactory;
import com.redhat.rhn.domain.audit.XccdfTestResult;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.systems.sdc.SdcHelper;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.ListHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.Listable;
import com.redhat.rhn.manager.audit.ScapManager;
import com.redhat.rhn.manager.system.SystemManager;

/**
 * XccdfDetailsAction
 * @version $Rev$
 */

public class XccdfDetailsAction extends RhnAction implements Listable {

    /**
     * {@inheritDoc}
     */
    public ActionForward execute(ActionMapping mapping, ActionForm formIn,
            HttpServletRequest request,
            HttpServletResponse response) {
        RequestContext context = new RequestContext(request);
        User user = context.getCurrentUser();
        Long sid = context.getRequiredParam("sid");
        Server server = SystemManager.lookupByIdAndUser(sid, user);
        Long xid = context.getRequiredParam("xid");
        XccdfTestResult testResult = ScapFactory.lookupTestResultByIdAndSid(xid,
                server.getId());
        request.setAttribute("testResult", testResult);
        request.setAttribute("system", server);

        ListHelper helper = new ListHelper(this, request);
        helper.execute();

        request.setAttribute(ListTagHelper.PARENT_URL,
                request.getRequestURI() + "?sid=" + sid + "&xid=" + xid);
        SdcHelper.ssmCheck(request, sid, user);

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    /**
     * {@inheritDoc}
     */
    public List getResult(RequestContext context) {
        Long xid = context.getRequiredParam("xid");
        return ScapManager.ruleResultsPerScan(xid);
    }
}
