/**
 * Copyright (c) 2013 SUSE
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

package com.redhat.rhn.frontend.action.rhnpackage;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.systems.sdc.SdcHelper;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.SessionSetHelper;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.frontend.taglibs.list.TagHelper;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.domain.rhnpackage.Package;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 *
 * @author bo
 */
public class LockPackageAction extends BaseSystemPackagesAction {
    private static final String LIST_NAME = "packageList";

    @Override
    protected DataResult getDataResult(Server server) {
        DataResult result = PackageManager.systemAvailablePackages(server.getId(), null);
        System.err.println("Available packages: " + result.size());
        return result;
    }
    

    @Override
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm formIn,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        RequestContext context = new RequestContext(request);
        Long sid = context.getRequiredParam("sid");
        User user = context.getLoggedInUser();
        Server server = SystemManager.lookupByIdAndUser(sid, user);
        Set sessionSet = SessionSetHelper.lookupAndBind(request, this.getDecl(sid));

        // Clear the set on first visit
        if (!context.isSubmitted()) {
            sessionSet.clear();
        }

        SessionSetHelper helper = new SessionSetHelper(request);
        if (request.getParameter("dispatch") != null) {
            // if its one of the Dispatch actions handle it..
            helper.updateSet(sessionSet, LIST_NAME);

            //List<PackageListItem> items = new LinkedList<PackageListItem>();
            if (!sessionSet.isEmpty()) {
                String[] selected = ListTagHelper.getSelected(LIST_NAME, request);
                for (int i = 0; i < selected.length; i++) {
                    Package pkg = this.findPackage(sid, selected[i], user);                    
                    if (pkg != null) {
                        System.err.println("Package: " + pkg.getNameEvra() + ", ID: " + pkg.getId());
                    } else {
                        System.err.println("Package not found from the combo: " + selected[i]);
                    }

                }
            } else {            
                RhnHelper.handleEmptySelection(request);
            }
        }

        DataResult dataSet = getDataResult(server);
        
        // Update selection
        if (ListTagHelper.getListAction(LIST_NAME, request) != null) {
            helper.execute(sessionSet, LIST_NAME, dataSet);
        }

        // Previous selection
        if (!sessionSet.isEmpty()) {
            helper.syncSelections(sessionSet, dataSet);
            ListTagHelper.setSelectedAmount(LIST_NAME, sessionSet.size(), request);
        }

        request.setAttribute("system", server);
        request.setAttribute(ListTagHelper.PARENT_URL, 
                             request.getRequestURI() + "?sid=" + server.getId());

        request.setAttribute("dataset", dataSet);
        SdcHelper.ssmCheck(request, server.getId(), user);
        ListTagHelper.bindSetDeclTo(LIST_NAME, getDecl(sid), request);
        TagHelper.bindElaboratorTo(LIST_NAME, dataSet.getElaborator(), request);

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }


    /**
     * Find the package.
     * 
     * @param sid
     * @param combo
     * @param user
     * @return 
     */
    private Package findPackage(Long sid, String combo, User user) {
        PackageListItem pkgInfo = PackageListItem.parse(combo.split("\\~\\*\\~")[0]);
        return PackageManager.guestimatePackageBySystem(sid, pkgInfo.getIdOne(), 
            pkgInfo.getIdTwo(),
            pkgInfo.getIdThree() != null ? pkgInfo.getIdThree() : 0, 
            user.getOrg());
    }
}
