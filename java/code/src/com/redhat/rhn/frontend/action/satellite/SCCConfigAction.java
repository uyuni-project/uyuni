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
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.MgrSyncUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.directwebremoting.WebContextFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Random;

/**
 * SCCConfigAction - Struts action to handle migration to the
 * SUSE Customer Center
 */
public class SCCConfigAction extends RhnAction {

    private static Logger log = Logger.getLogger(SCCConfigAction.class);
    private static final String LOCAL_MIRROR_USED = "local.mirror.used";

    /**
     * DWR ajax end-point for this action
     */
    public static class AjaxEndPoint {

        public static String sayHello() {
            try {
                Thread.sleep(4000);
                Random random = new Random();
                if (random.nextInt(10) == 0) {
                    throw new RuntimeException("Test error");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.debug("I am on DWR");
            return "Hello!";
        }

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

        public static void performMigration() throws ContentSyncException {
            User user = new RequestContext(WebContextFactory.get().getHttpServletRequest())
                    .getCurrentUser();
            ensureSatAdmin(user);
            ContentSyncManager manager = new ContentSyncManager();
            manager.performMigration(user);
        }

        public static void synchronizeChannels() throws ContentSyncException {
            ensureSatAdmin();
            ContentSyncManager csm = new ContentSyncManager();
            csm.updateChannels(csm.getRepositories(), null);
        }

        public static void synchronizeChannelFamilies() throws ContentSyncException {
            ensureSatAdmin();
            ContentSyncManager csm = new ContentSyncManager();
            csm.updateChannelFamilies(csm.readChannelFamilies());
        }

        public static void synchronizeProducts() throws ContentSyncException {
            ensureSatAdmin();
            ContentSyncManager csm = new ContentSyncManager();
            csm.updateSUSEProducts(csm.getProducts());
        }

        public static void synchronizeProductChannels() throws ContentSyncException {
            ensureSatAdmin();
            ContentSyncManager csm = new ContentSyncManager();
            csm.updateSUSEProductChannels(csm.getAvailableChannels(csm.readChannels()));
        }

        public static void synchronizeSubscriptions() throws ContentSyncException {
            ensureSatAdmin();
            ContentSyncManager csm = new ContentSyncManager();
            csm.updateSubscriptions(csm.getSubscriptions());
        }

        public static void synchronizeUpgradePaths() throws ContentSyncException {
            ensureSatAdmin();
            ContentSyncManager csm = new ContentSyncManager();
            csm.updateUpgradePaths();
        }
    }

    /**
     * Logger for this class
     */
    private static Logger logger = Logger.getLogger(SCCConfigAction.class);

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
        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    private boolean localMirrorUsed() {
        return Config.get().getString(ContentSyncManager.MIRROR_CFG_KEY) != null ||
            Config.get().getString(ContentSyncManager.RESOURCE_PATH) != null;
    }

}

