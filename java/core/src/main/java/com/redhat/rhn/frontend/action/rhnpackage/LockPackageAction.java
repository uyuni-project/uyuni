/*
 * Copyright (c) 2013 SUSE LLC
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
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
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
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * LockPackageAction
 */
public class LockPackageAction extends BaseSystemPackagesAction {
    private static final String LIST_NAME = "packageList";

    /** Logger instance */
    private static final Logger LOG = LogManager.getLogger(LockPackageAction.class);

    @Override
    protected DataResult<PackageListItem> getDataResult(Server server) {
        Optional<MinionServer> minion = MinionServerFactory.lookupById(server.getId());
        // Check if this server is a minion
        boolean isMinion = minion.isPresent();
        LOG.debug("{}is a minion system? {}", server.getId(), isMinion);
        // Check if this is a SUSE system (for minions only)
        boolean isSUSEMinion = isMinion && minion.get().isOsFamilySuse();
        LOG.debug("{}is a SUSE system? {}", server.getId(), isSUSEMinion);

        if (isSUSEMinion || !isMinion) {
            return PackageManager.systemTotalPackages(server.getId(), null);
        }
        return PackageManager.nonSUSEsystemLockingPackages(server.getId(), null);
    }

    /**
     * {@inheritDoc}
     */
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
        Set<String> pkgsToSelect = SessionSetHelper.lookupAndBind(
                request, this.getDecl(sid));
        ActionMessages infoMessages = new ActionMessages();
        ActionErrors errorMessages = new ActionErrors();
        Set<Package> pkgsAlreadyLocked = new HashSet<>();

        if (!context.isSubmitted()) {
            pkgsToSelect.clear();
        }

        DataResult<PackageListItem> lockedPackagesResult =
                PackageManager.systemLockedPackages(sid, null);

        if (!context.isSubmitted()) {
            for (PackageListItem pkg : lockedPackagesResult) {
                // pre-select locked
                pkgsToSelect.add(pkg.getIdCombo() + "~*~" + pkg.getNvrea());
            }
        }

        List<Package> pkgsFindAlreadyLocked = PackageManager.lookupByIdAndUser(
                lockedPackagesResult.stream().map(PackageListItem::getPackageId)
                        .collect(Collectors.toList()), user);
        pkgsAlreadyLocked.addAll(pkgsFindAlreadyLocked);

        SessionSetHelper helper = new SessionSetHelper(request);
        if (isSubmitted(form) && request.getParameter("dispatch") != null) {
            // if its one of the Dispatch actions handle it...
            helper.updateSet(pkgsToSelect, LIST_NAME);
            Date scheduleDate = this.getStrutsDelegate().readScheduleDate(
                    form, "date", DatePicker.YEAR_RANGE_POSITIVE);
            if (!pkgsToSelect.isEmpty()) {
                try {
                    if (context.wasDispatched("pkg.lock.requestlock")) {
                        boolean success = this.lockSelectedPackages(pkgsAlreadyLocked,
                                                  scheduleDate,
                                                  server,
                                                  request);
                        if (success) {
                            this.getStrutsDelegate().addInfo("pkg.lock.message.locksuccess",
                                    infoMessages);
                        }
                        else {
                            this.getStrutsDelegate().addError("pkg.lock.message.lockerror",
                                    errorMessages);
                        }
                    }
                    else if (context.wasDispatched("pkg.lock.requestunlock")) {
                        this.unlockSelectedPackages(pkgsAlreadyLocked,
                                                    scheduleDate,
                                                    server,
                                                    request);
                        this.getStrutsDelegate().addInfo("pkg.lock.message.unlocksuccess",
                                                         infoMessages);
                    }
                }
                catch (TaskomaticApiException e) {
                    LOG.error("Could not schedule package lock action:", e);
                    this.getStrutsDelegate().addError(errorMessages, "taskscheduler.down");
                }
            }
            else {
                RhnHelper.handleEmptySelection(request);
                this.getStrutsDelegate().addError(errorMessages, "emptyselectionerror");
            }
        }

        DataResult<PackageListItem> dataSet = getDataResult(server);

        // Update selection
        if (ListTagHelper.getListAction(LIST_NAME, request) != null) {
            helper.execute(pkgsToSelect, LIST_NAME, dataSet);
        }

        // Previous selection
        if (!pkgsToSelect.isEmpty()) {
            helper.syncSelections(pkgsToSelect, dataSet);
            ListTagHelper.setSelectedAmount(LIST_NAME, pkgsToSelect.size(), request);
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
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    private void unlockSelectedPackages(Set<Package> pkgsAlreadyLocked, Date scheduleDate,
            Server server, HttpServletRequest request)
        throws TaskomaticApiException {
        RequestContext context = new RequestContext(request);
        User user = context.getCurrentUser();
        Set<Package> pkgsToUnlock = new HashSet<>();
        String[] selectedPkgs = ListTagHelper.getSelected(LIST_NAME, request);

        if (selectedPkgs != null) {
            for (String label : selectedPkgs) {
                Package pkg = this.findPackage(label, user);
                if (pkg != null) {
                    pkgsToUnlock.add(pkg);
                }
            }
        }

        PackageManager.setPendingStatusOnLockedPackages(pkgsToUnlock,
                                                        PackageManager.PKG_PENDING_UNLOCK);
        ActionManager.schedulePackageLock(user, pkgsAlreadyLocked, scheduleDate, server);
    }

    /**
     * Lock the packages, adding to the existing list of locked packages,
     * yet re-issuing the action.
     *
     * @param pkgsAlreadyLocked
     * @param scheduleDate
     * @param server
     * @param request
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     * @return whether the lock has been scheduled successfully
     */
    private boolean lockSelectedPackages(Set<Package> pkgsAlreadyLocked, Date scheduleDate,
            Server server, HttpServletRequest request) throws TaskomaticApiException {
        RequestContext context = new RequestContext(request);
        Long sid = context.getRequiredParam("sid");
        User user = context.getCurrentUser();
        Set<Package> pkgsToLock = new HashSet<>();
        String[] selectedPkgs = ListTagHelper.getSelected(LIST_NAME, request);

        // Lock all selected packages, if they are not already in the list
        if (selectedPkgs != null) {
            Set<Long> alreadyLockedNameIds = pkgsAlreadyLocked.stream().map(p ->
                    p.getPackageName().getId()).collect(Collectors.toSet());

            for (String label : selectedPkgs) {
                Package pkg = this.findPackage(label, user);

                if (pkg == null || pkgsAlreadyLocked.contains(pkg)) {
                    continue;
                }
                // Prevent multiple versions of the same package from being locked
                else if (alreadyLockedNameIds.contains(pkg.getPackageName().getId()) ||
                        pkgsToLock.stream().anyMatch(p ->
                                Objects.equals(p.getPackageName().getId(), pkg.getPackageName().getId())
                        )) {
                    return false;
                }

                pkg.setLockPending(Boolean.TRUE);
                pkgsToLock.add(pkg);
            }
        }

        if (!pkgsToLock.isEmpty()) {
            // mark the selected packages as 'pending'
            PackageManager.lockPackages(sid, pkgsToLock);

            // Ensure pending locks and already locked items are sent to the client
            pkgsToLock.addAll(pkgsAlreadyLocked);
            ActionManager.schedulePackageLock(user, pkgsToLock, scheduleDate, server);
        }
        else {
            LockPackageAction.LOG.info("No packages to lock");
        }
        return true;
    }

    /**
     * Find the package.
     *
     * @param combo Package combo (Separated by "|" name ID, evr ID and arch ID).
     * @param user User.
     * @return Returns Package or null.
     */
    private Package findPackage(String combo, User user) {
        PackageListItem pkgInfo = PackageListItem.parse(combo.split("\\~\\*\\~")[0]);
        Package pkg = PackageFactory.lookupByNevraIds(user.getOrg(), pkgInfo.getIdOne(),
                pkgInfo.getIdTwo(), pkgInfo.getIdThree()).get(0);
        if (pkg != null) {
            pkg.setLockPending(Boolean.TRUE);
        }

        return pkg;
    }
}
