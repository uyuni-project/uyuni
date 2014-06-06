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
package com.redhat.rhn.frontend.action.systems.sdc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.StrutsDelegate;
import com.redhat.rhn.frontend.taglibs.list.helper.ListHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.Listable;
import com.redhat.rhn.manager.system.SystemManager;

/**
 * SingleSnapshotTagsAction
 * @version $Rev$
 */
public class SingleSnapshotTagsAction extends RhnAction implements Listable {

    /**
     * {@inheritDoc}
     */
    public ActionForward execute(ActionMapping mapping, ActionForm formIn,
            HttpServletRequest request,
            HttpServletResponse response) {
        RequestContext context = new RequestContext(request);
        Long sid = context.getRequiredParam("sid");
        Server server = context.lookupAndBindServer();
        Long ssId = context.getRequiredParam("ss_id");

        ListHelper helper = new ListHelper(this, request);
        helper.execute();
        Map params = makeParamMap(request);
        params.put(RequestContext.SID, sid);
        params.put("ss_id", ssId);

        DateFormat df = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
        params.put("ss_name",
                df.format(ServerFactory.lookupSnapshotById(ssId.intValue()).getCreated()));

        return StrutsDelegate.getInstance().forwardParams(
                mapping.findForward("default"), params);
    }

    /** {@inheritDoc} */
    public List getResult(RequestContext context) {
        Long sid = context.getRequiredParam("sid");
        Long ssId = context.getRequiredParam("ss_id");
        return SystemManager.snapshotTagsForSystemAndSnapshot(sid, ssId, null);
    }

}
