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
package com.redhat.rhn.frontend.action.systems;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.rhnset.RhnSetElement;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.SystemGroupOverview;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.RhnListSetHelper;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.frontend.taglibs.list.TagHelper;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * SystemGroupListSetupAction
 * @version $Rev$
 */
public class SystemGroupListSetupAction extends RhnAction {
    private static final Logger LOG = Logger.getLogger(SystemGroupListSetupAction.class);

    /** {@inheritDoc} */
    public ActionForward execute(ActionMapping mapping,
            ActionForm formIn,
            HttpServletRequest request,
            HttpServletResponse response) {

        RequestContext requestContext = new RequestContext(request);
        User user =  requestContext.getCurrentUser();


        DataResult<SystemGroupOverview> result = SystemManager.groupList(user, null);
        request.setAttribute(ListTagHelper.PARENT_URL, request.getRequestURI());
        request.setAttribute(RequestContext.PAGE_LIST, result);
        ListTagHelper.bindSetDeclTo("groupList", getSetDecl(), request);
        TagHelper.bindElaboratorTo("groupList", result.getElaborator(), request);

        RhnSet set =  getSetDecl().get(user);
        if (!requestContext.isSubmitted()) {
            set.clear();
            RhnSetManager.store(set);
        }

        RhnListSetHelper helper = new RhnListSetHelper(request);
        if (ListTagHelper.getListAction("groupList", request) != null) {
            helper.execute(set, "groupList", result);
        }
        else {

            if (request.getParameter("union") != null) {
                helper.updateSet(set, "groupList");
                return union(mapping, formIn, request, response, set);
            }
            else if (request.getParameter("intersection") != null) {
                helper.updateSet(set, "groupList");
                return intersection(mapping, formIn, request, response, set);
            }
        }

        if (!set.isEmpty()) {
            helper.syncSelections(set, result);
            ListTagHelper.setSelectedAmount("result", set.size(), request);
        }

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    protected RhnSetDecl getSetDecl() {
        return RhnSetDecl.SYSTEM_GROUPS;
    }

    /**
     * Sends the user to the SSM with a system set representing the intersection
     * of their chosen group set
     * @param mapping ActionMapping
     * @param formIn ActionForm
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param groupSet the set of groups to intersect
     * @return the ActionForward that uses the intersection of the
     *         chosen groups in the SSM
     */
    public ActionForward intersection(ActionMapping mapping,
            ActionForm formIn,
            HttpServletRequest request,
            HttpServletResponse response, RhnSet groupSet) {

        User user = new RequestContext(request).getCurrentUser();
        RhnSet systemSet = RhnSetDecl.SYSTEMS.create(user);

        if (groupSet.isEmpty()) {
            ActionMessages msg = new ActionMessages();
            msg.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("systemgroups.none"));
            getStrutsDelegate().saveMessages(request, msg);
            return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
        }

       // Iterator groups = groupSet.getElements().iterator();
        List<Long> firstList = new ArrayList<Long>();
        List<Long> secondList = new ArrayList<Long>();

        //for the first group, add all the systems to firstList
        Long sgid = groupSet.getElementValues().iterator().next();
        groupSet.removeElement(sgid);

        for (SystemOverview system : SystemManager.systemsInGroup(sgid, null)) {
            Long id = system.getId();
            firstList.add(id);
        }

        //for every subsequent group, remove systems that aren't in the intersection
        for (Long groupId : groupSet.getElementValues()) { //for every group

          //for every system in each group
            for (SystemOverview sys : SystemManager.systemsInGroup(groupId, null)) {
                Long id = sys.getId();
                secondList.add(id);
            }

            firstList = listIntersection(firstList, secondList);
            secondList = new ArrayList<Long>();
        }

        //add all the systems to the set
        for (Long i : firstList) {
            systemSet.addElement(i);
        }
        RhnSetManager.store(systemSet);

        /*
         * Until SSM stuff is done in java, we have to redirect because struts
         * doesn't easily go outside of the /rhn context
         * TODO: make this an ActionForward
         */
        try {
            response.sendRedirect("/rhn/systems/ssm/ListSystems.do");
        }
        catch (IOException exc) {
            // This really shouldn't happen, but just in case, log and
            // return.
            LOG.error("IOException when trying to redirect to " +
                    "/rhn/systems/ssm/ListSystems.do", exc);
        }

        return null;
    }


    private List<Long> listIntersection(List<Long> one, List<Long> two) {

        List<Long> retval = new ArrayList<Long>();
        for (Long i : one) {
            if (two.contains(i)) {
                retval.add(i);
            }
        }

        return retval;
    }

    /**
     * Sends the user to the SSM with a system set representing the union
     * of their chosen group set
     * @param mapping ActionMapping
     * @param formIn ActionForm
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param groupSet the set of groups to union
     * @return the ActionForward that uses the union of the
     *         chosen groups in the SSM
     */
    public ActionForward union(ActionMapping mapping,
            ActionForm formIn,
            HttpServletRequest request,
            HttpServletResponse response, RhnSet groupSet) {
        User user = new RequestContext(request).getCurrentUser();
        RhnSet systemSet = RhnSetDecl.SYSTEMS.create(user);

        if (groupSet.isEmpty()) {
            ActionMessages msg = new ActionMessages();
            msg.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("systemgroups.none"));
            getStrutsDelegate().saveMessages(request, msg);
            return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
        }

        Iterator<RhnSetElement> groups = groupSet.getElements().iterator();
        while (groups.hasNext()) { //for every group
            Long sgid = groups.next().getElement();
            Iterator<SystemOverview> systems =
                    SystemManager.systemsInGroup(sgid, null).iterator();

            while (systems.hasNext()) { //for every system in a group
                Long id = systems.next().getId();
                if (!systemSet.contains(id)) {
                    systemSet.addElement(id);
                }
            }
        }

        RhnSetManager.store(systemSet);

        /*
         * Until SSM stuff is done in java, we have to redirect because struts
         * doesn't easily go outside of the /rhn context
         * TODO: make this an ActionForward
         */
        try {
            response.sendRedirect("/rhn/systems/ssm/ListSystems.do");
        }
        catch (IOException exc) {
            // This really shouldn't happen, but just in case, log and
            // return.
            LOG.error("IOException when trying to redirect to " +
                    "/rhn/systems/ssm/ListSystems.do", exc);
        }

        return null;
    }
}
