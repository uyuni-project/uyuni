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
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.util.DatePicker;
import com.redhat.rhn.domain.action.Action;
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
import com.redhat.rhn.frontend.dto.ScheduledAction;
import com.redhat.rhn.manager.action.ActionManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

/**
 *
 * @author bo
 */
public class LockPackageAction extends BaseSystemPackagesAction {
    private static final String LIST_NAME = "packageList";

    @Override
    protected DataResult getDataResult(Server server) {
        return PackageManager.systemAvailablePackages(server.getId(), null);
    }
    

    @Override
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm actionForm,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        RequestContext context = new RequestContext(request);
        DynaActionForm form = (DynaActionForm) actionForm;
        Long sid = context.getRequiredParam("sid");
        User user = context.getLoggedInUser();
        Server server = SystemManager.lookupByIdAndUser(sid, user);
        Set selectedPkgs = SessionSetHelper.lookupAndBind(request, this.getDecl(sid));

        // Clear the set on first visit
        DataResult lockedPackagesResult = PackageManager.systemLockedPackages(sid, null);
        if (!context.isSubmitted()) {
            selectedPkgs.clear();
            for (Iterator it = lockedPackagesResult.iterator(); it.hasNext();) {
                PackageListItem pkg = (PackageListItem) it.next();
                selectedPkgs.add(pkg.getIdCombo() + "~*~" + pkg.getNvrea()); // pre-select locked
            }
        }

        SessionSetHelper helper = new SessionSetHelper(request);
        if (request.getParameter("dispatch") != null) {
            // if its one of the Dispatch actions handle it..
            helper.updateSet(selectedPkgs, LIST_NAME);
            Date scheduleDate = this.getStrutsDelegate().readDatePicker(
                    form, "date", DatePicker.YEAR_RANGE_POSITIVE);
            if (!selectedPkgs.isEmpty()) {
                List<Package> container = new ArrayList<Package>() {
                    @Override
                    public boolean add(Package pkg) {
                        return pkg == null ? false : super.add(pkg);
                    }
                };

                if (request.getParameter("dispatch").equals(LocalizationService.getInstance().getMessage("pkg.lock.requestlock"))) {
                    this.lockSelectedPackages(lockedPackagesResult, selectedPkgs, container,
                                              scheduleDate, server, request);
                }
                else if (request.getParameter("dispatch").equals(LocalizationService.getInstance().getMessage("pkg.lock.requestunlock"))) {
                    this.unlockSelectedPackages(selectedPkgs);
                }
                else if (request.getParameter("dispatch").equals(LocalizationService.getInstance().getMessage("pkg.lock.showlockedonly"))) {
                    // set to the session some flag that would:
                    // 1. filter out only locked packages
                    // 2. disable the "request lock" button
                    // 3. switch to "show all packages" button, which do the otherwise above
                }
            } else {            
                RhnHelper.handleEmptySelection(request);
            }
        }

        DataResult dataSet = getDataResult(server);

        // Update selection
        if (ListTagHelper.getListAction(LIST_NAME, request) != null) {
            helper.execute(selectedPkgs, LIST_NAME, dataSet);
        }

        // Previous selection
        if (!selectedPkgs.isEmpty()) {
            helper.syncSelections(selectedPkgs, dataSet);
            ListTagHelper.setSelectedAmount(LIST_NAME, selectedPkgs.size(), request);
        }

        request.setAttribute("system", server);
        request.setAttribute(ListTagHelper.PARENT_URL, 
                             request.getRequestURI() + "?sid=" + server.getId());
        request.setAttribute("date", this.getStrutsDelegate().prepopulateDatePicker(
                request, form, "date", DatePicker.YEAR_RANGE_POSITIVE));
        request.setAttribute("dataset", dataSet);
        SdcHelper.ssmCheck(request, server.getId(), user);
        ListTagHelper.bindSetDeclTo(LIST_NAME, getDecl(sid), request);
        TagHelper.bindElaboratorTo(LIST_NAME, dataSet.getElaborator(), request);

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }


    private void unlockSelectedPackages(Set selected) {
        System.err.println("Unlock is not yet implemented");
    }


    private void lockSelectedPackages(DataResult lockedPackages,
                                      Set selected,
                                      List<Package> container,
                                      Date scheduleDate,
                                      Server server,
                                      HttpServletRequest request) {
        RequestContext context = new RequestContext(request);
        Long sid = context.getRequiredParam("sid");
        User user = context.getLoggedInUser();

        // Unlock all packages temporarily
        for (Iterator it = lockedPackages.iterator(); it.hasNext();) {
            container.add(PackageManager.lookupByIdAndUser(
                    ((PackageListItem) it.next()).getId(), user));
        }
        
        PackageManager.unlockPackages(sid, container);
        container.clear();
        
        // Lock all selected packages
        for (String label : ListTagHelper.getSelected(LIST_NAME, request)) {
            container.add(this.findPackage(sid, label, user));
        }
        
        if (!container.isEmpty()) {
            // Wipe all previously scheduled actions
            for (Iterator it = ActionManager.allActions(user, null).iterator();
                 it.hasNext();) {
                Action action = ActionManager.lookupAction(user,
                                    ((ScheduledAction) it.next()).getId());
                // Hard-coded ID from DB: this is *the* design!
                if (action != null && action.getActionType().getId() == 502) {
                    System.err.println("Canceled action: " + action.getName());
                    ActionManager.cancelAction(user, action);
                }
            }

            // Lock packages
            PackageManager.lockPackages(sid, container);
            ActionManager.schedulePackageLock(user, server, container, scheduleDate);
        } else {
            System.err.println("No packages has been locked.");
        }
    }

    /**
     * Find the package.
     * 
     * @param sid System ID
     * @param combo Package combo (Separated by "|" name ID, evr ID and arch ID).
     * @param user User.
     * @return Returns Package or null.
     */
    private Package findPackage(Long sid, String combo, User user) {
        PackageListItem pkgInfo = PackageListItem.parse(combo.split("\\~\\*\\~")[0]);
        Package pkg = PackageManager.guestimatePackageBySystem(sid, pkgInfo.getIdOne(),
                        pkgInfo.getIdTwo(),
                        pkgInfo.getIdThree() != null ? pkgInfo.getIdThree() : 0,
                        user.getOrg());
        if (pkg != null) {
            pkg.setLockPending(Boolean.TRUE);
        }

        return pkg;
    }
}
