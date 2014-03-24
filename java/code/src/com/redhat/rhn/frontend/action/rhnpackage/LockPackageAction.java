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
import com.redhat.rhn.common.util.DatePicker;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.systems.sdc.SdcHelper;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.SessionSetHelper;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.frontend.taglibs.list.TagHelper;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.log4j.Logger;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author bo
 */
public class LockPackageAction extends BaseSystemPackagesAction {
    private static final String LIST_NAME = "packageList";

    /** Logger instance */
    private static Logger log = Logger.getLogger(LockPackageAction.class);

    @Override
    protected DataResult getDataResult(Server server) {
        DataResult result = PackageManager.systemTotalPackages(server.getId(), null);
        return result;
    }


    @Override
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm actionForm,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        RequestContext context = new RequestContext(request);
        DynaActionForm form = (DynaActionForm) actionForm;
        Long sid = context.getRequiredParam("sid");
        User user = context.getCurrentUser();
        Server server = SystemManager.lookupByIdAndUser(sid, user);
        Set<String> selectedPkgs = SessionSetHelper.lookupAndBind(request, this.getDecl(sid));
        ActionMessages infoMessages = new ActionMessages();
        ActionErrors errorMessages = new ActionErrors();
        Set<Package> pkgsAlreadyLocked = new HashSet<Package>();

        if (!context.isSubmitted()) {
            selectedPkgs.clear();
        }

        DataResult<PackageListItem> lockedPackagesResult = PackageManager.systemLockedPackages(sid, null);
        for (PackageListItem pkg : lockedPackagesResult) {
            if (!context.isSubmitted()) {
                // pre-select locked
                selectedPkgs.add(pkg.getIdCombo() + "~*~" + pkg.getNvrea());
            }

            Package lockedPkg = PackageManager.lookupByIdAndUser(pkg.getPackageId(), user);
            if (lockedPkg != null) {
                pkgsAlreadyLocked.add(lockedPkg);
            }
        }

        SessionSetHelper helper = new SessionSetHelper(request);
        if (isSubmitted(form)) {
            // if its one of the Dispatch actions handle it..
            helper.updateSet(selectedPkgs, LIST_NAME);
            Date scheduleDate = this.getStrutsDelegate().readDatePicker(
                    form, "date", DatePicker.YEAR_RANGE_POSITIVE);
            if (!selectedPkgs.isEmpty()) {
                if (context.wasDispatched("pkg.lock.requestlock")) {
                    this.lockSelectedPackages(pkgsAlreadyLocked,
                                              scheduleDate,
                                              server,
                                              request);
                    this.getStrutsDelegate().addInfo("pkg.lock.message.locksuccess",
                                                     infoMessages);
                }
                else if (context.wasDispatched("pkg.lock.requestunlock")) {
                    try {
                        this.unlockSelectedPackages(pkgsAlreadyLocked,
                                                    scheduleDate,
                                                    server,
                                                    request);
                        this.getStrutsDelegate().addInfo("pkg.lock.message.unlocksuccess",
                                                         infoMessages);
                    }
                    catch (Exception ex) {
                        log.error(ex);
                        this.getStrutsDelegate().addError(errorMessages,
                                                          "pkg.lock.message.genericerror",
                                                          ex.getLocalizedMessage());
                    }
                }
                else if (request.getParameter("dispatch").equals(
                        LocalizationService.getInstance()
                        .getMessage("pkg.lock.showlockedonly"))) {
                    // set to the session some flag that would:
                    // 1. filter out only locked packages
                    // 2. disable the "request lock" button
                    // 3. switch to "show all packages" button, which do the otherwise above
                }
            }
            else {
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
        request.setAttribute(Globals.MESSAGE_KEY, infoMessages);
        request.setAttribute(Globals.ERROR_KEY, errorMessages);

        SdcHelper.ssmCheck(request, server.getId(), user);
        ListTagHelper.bindSetDeclTo(LIST_NAME, getDecl(sid), request);
        TagHelper.bindElaboratorTo(LIST_NAME, dataSet.getElaborator(), request);

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    /**
     * Unlock the packages.
     *
     * @param pkgsAlreadyLocked
     * @param scheduleDate
     * @param server
     * @param request
     */
    private void unlockSelectedPackages(Set<Package> pkgsAlreadyLocked,
                                        Date scheduleDate,
                                        Server server,
                                        HttpServletRequest request)
            throws Exception {
        RequestContext context = new RequestContext(request);
        Long sid = context.getRequiredParam("sid");
        User user = context.getCurrentUser();
        List<Package> pkgsToUnlock = new ArrayList<Package>();

        for (String label : ListTagHelper.getSelected(LIST_NAME, request)) {
            Package pkg = this.findPackage(sid, label, user);
            if (pkg != null) {
                pkgsToUnlock.add(pkg);
            }
        }

        PackageManager.setPendingStatusOnLockedPackages(pkgsToUnlock,
                                                        PackageManager.PKG_PENDING_UNLOCK);
        ActionManager.schedulePackageLock(user, server, pkgsAlreadyLocked, scheduleDate);
    }

    /**
     * Lock the packages, adding to the existing list of locked packages,
     * yet re-issuing the action.
     *
     * @param pkgsAlreadyLocked
     * @param scheduleDate
     * @param server
     * @param request
     */
    private void lockSelectedPackages(Set<Package> pkgsAlreadyLocked,
                                      Date scheduleDate,
                                      Server server,
                                      HttpServletRequest request) {
        RequestContext context = new RequestContext(request);
        Long sid = context.getRequiredParam("sid");
        User user = context.getCurrentUser();
        Set<Package> pkgsToLock = new HashSet<Package>();

        // Lock all selected packages, if they are not already in the list
        for (String label : ListTagHelper.getSelected(LIST_NAME, request)) {
            Package pkg = this.findPackage(sid, label, user);

            if (pkg == null || pkgsAlreadyLocked.contains(pkg)) {
                continue;
            }

            pkg.setLockPending(Boolean.TRUE);
            pkgsToLock.add(pkg);
        }

        if (!pkgsToLock.isEmpty()) {
            // mark the selected packages as 'pending'
            PackageManager.lockPackages(sid, pkgsToLock);

            // Ensure pending locks and already locked items are sent to the client
            pkgsToLock.addAll(pkgsAlreadyLocked);
            ActionManager.schedulePackageLock(user, server, pkgsToLock, scheduleDate);
        }
        else {
            log.info("No packages to lock");
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
