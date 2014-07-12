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

package com.redhat.rhn.frontend.action.groups;

import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.ListSessionSetHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.Listable;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author paji
 * @version $Rev$
 */
public abstract class BaseListAction extends RhnAction implements Listable {

    protected void setup(HttpServletRequest request) {
        RequestContext context = new RequestContext(request);
        context.lookupAndBindServerGroup();
    }

    /** {@inheritDoc} */
    public String getDataSetName() {
        return RequestContext.PAGE_LIST;
    }

    /** {@inheritDoc} */
    public String getListName() {
        return "systemList";
    }

    protected Map getParamsMap(HttpServletRequest request) {
        RequestContext context = new RequestContext(request);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(RequestContext.SERVER_GROUP_ID,
                    context.getRequiredParam(RequestContext.SERVER_GROUP_ID));
        return params;
    }

    /** {@inheritDoc} */
    @Override
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm formIn,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        setup(request);
        ListSessionSetHelper helper = new ListSessionSetHelper(this,
                                        request, getParamsMap(request));
        processHelper(helper);
        helper.execute();
        if (helper.isDispatched()) {
            ActionForward forward =
                handleDispatch(helper, mapping, formIn, request, response);
            processPostSubmit(helper);
            return forward;
        }
        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    protected abstract ActionForward handleDispatch(
            ListSessionSetHelper helper,
            ActionMapping mapping,
            ActionForm formIn, HttpServletRequest request,
            HttpServletResponse response);

    protected void processHelper(ListSessionSetHelper helper) {
        helper.setDataSetName(getDataSetName());
        helper.setListName(getListName());
    }

    protected void processPostSubmit(ListSessionSetHelper helper) {
        helper.destroy();
    }
}
