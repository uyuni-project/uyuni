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
package com.redhat.rhn.frontend.action.systems.sdc;

import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.CustomDataValue;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ListCustomDataAction handles the interaction of the sdc/ListCustomData page.
 */
public class ListCustomDataAction extends RhnAction {
    public static final String SID = "sid";

    /** {@inheritDoc} */
    @Override
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm formIn,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        RequestContext ctx = new RequestContext(request);
        User user =  ctx.getCurrentUser();
        Map<String, Object> params = makeParamMap(request);
        String fwd = RhnHelper.DEFAULT_FORWARD;

        Long sid = ctx.getRequiredParam(RequestContext.SID);
        Server server = SystemManager.lookupByIdAndUser(sid, user);
        Set<CustomDataValue> customDataValues = server.getCustomDataValues();

        if (customDataValues.isEmpty()) {
            request.setAttribute("listEmpty", "1");
        }

        List<String> keyList = customDataValues.stream()
                .map(val -> val.getKey().getLabel())
                .sorted()
                .toList();

        List<Map<String, Object>> pageList = new ArrayList<>();
        for (String keyLabel : keyList) {
            Map<String, Object> returnMap = new HashMap<>();

            CustomDataKey key = OrgFactory.lookupKeyByLabelAndOrg(keyLabel, user.getOrg());
            CustomDataValue val = server.getCustomDataValue(key);
            returnMap.put("cikid", val.getKey().getId());
            returnMap.put("label", val.getKey().getLabel());
            returnMap.put("description", val.getKey().getDescription());

            if (val.getValue() != null) {
                returnMap.put("value", val.getValue());
            }
            else {
                returnMap.put("value", "");
            }
            pageList.add(returnMap);
        }

        request.setAttribute(ListTagHelper.PARENT_URL, request.getRequestURI());
        request.setAttribute(RequestContext.PAGE_LIST, pageList);
        request.setAttribute("system", server);

        return getStrutsDelegate().forwardParams(
                mapping.findForward(fwd), params);
    }
}
