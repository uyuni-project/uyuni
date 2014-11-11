/**
 * Copyright (c) 2014 SUSE LLC
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
package com.redhat.rhn.frontend.action.satellite;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.domain.iss.IssFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.MgrSyncUtils;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.io.DwrConvertedException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * SCCConfigAction - Struts action to handle migration to the
 * SUSE Customer Center
 */
public class SCCConfigAction extends RhnAction {

    private static final String LOCAL_MIRROR_USED = "localMirrorUsed";
    public static final String ISS_MASTER = "issMaster";

    /**
     * DWR ajax end-point for this action
     */
    public static class AjaxEndPoint {

        private static void ensureSatAdmin(User user) {
            if (!user.hasRole(RoleFactory.SAT_ADMIN)) {
                throw new IllegalArgumentException(
                        "Must be SAT_ADMIN to perform a migration");
            }
        }

        private static void ensureSatAdmin() {
            User user = new RequestContext(WebContextFactory.get().getHttpServletRequest())
                    .getCurrentUser();
            ensureSatAdmin(user);
        }

        public static void performMigration() throws DwrConvertedException {
            User user = new RequestContext(WebContextFactory.get().getHttpServletRequest())
                    .getCurrentUser();
            ensureSatAdmin(user);
            ContentSyncManager manager = new ContentSyncManager();
            try {
                manager.performMigration(user);
            }
            catch (Exception e) {
                throw new DwrConvertedException(e.getLocalizedMessage());
            }
        }

        public static void synchronizeChannels() throws DwrConvertedException {
            ensureSatAdmin();
            ContentSyncManager csm = new ContentSyncManager();
            try {
                csm.updateChannels(null);
            }
            catch (Exception e) {
                throw new DwrConvertedException(e.getLocalizedMessage());
            }
        }

        public static void synchronizeChannelFamilies() throws DwrConvertedException {
            ensureSatAdmin();
            ContentSyncManager csm = new ContentSyncManager();
            try {
                csm.updateChannelFamilies(csm.readChannelFamilies());
            }
            catch (Exception e) {
                throw new DwrConvertedException(e.getLocalizedMessage());
            }
        }

        public static void synchronizeProducts() throws DwrConvertedException {
            ensureSatAdmin();
            ContentSyncManager csm = new ContentSyncManager();
            try {
                csm.updateSUSEProducts(csm.getProducts());
            }
            catch (Exception e) {
                throw new DwrConvertedException(e.getLocalizedMessage());
            }
        }

        public static void synchronizeProductChannels() throws DwrConvertedException {
            ensureSatAdmin();
            ContentSyncManager csm = new ContentSyncManager();
            try {
                csm.updateSUSEProductChannels(csm.getAvailableChannels(csm.readChannels()));
            }
            catch (Exception e) {
                throw new DwrConvertedException(e.getLocalizedMessage());
            }
        }

        public static void synchronizeSubscriptions() throws DwrConvertedException {
            ensureSatAdmin();
            ContentSyncManager csm = new ContentSyncManager();
            try {

                csm.updateSubscriptions(csm.getSubscriptions());
            }
            catch (Exception e) {
                throw new DwrConvertedException(e.getLocalizedMessage());
            }
        }

        public static void synchronizeUpgradePaths() throws DwrConvertedException {
            ensureSatAdmin();
            ContentSyncManager csm = new ContentSyncManager();
            try {
                csm.updateUpgradePaths();
            }
            catch (Exception e) {
                throw new DwrConvertedException(e.getLocalizedMessage());
            }
        }
    }

    /** {@inheritDoc} */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm formIn,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        if (!MgrSyncUtils.isSCCTheDefault()) {
            return mapping.findForward("notyetavailable");
        }

        if (MgrSyncUtils.isMigratedToSCC()) {
            return mapping.findForward("alreadymigrated");
        }

        request.setAttribute(LOCAL_MIRROR_USED, localMirrorUsed());

        // Perform a refresh only if this is an ISS master
        request.setAttribute(ISS_MASTER, IssFactory.getCurrentMaster() == null);

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    private boolean localMirrorUsed() {
        return Config.get().getString(ContentSyncManager.MIRROR_CFG_KEY) != null ||
            Config.get().getString(ContentSyncManager.RESOURCE_PATH) != null;
    }

}

