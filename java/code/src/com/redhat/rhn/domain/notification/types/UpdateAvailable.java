/*
 * Copyright (c) 2023 SUSE LLC
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
package com.redhat.rhn.domain.notification.types;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.notification.NotificationMessage;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Notification data for an update being available for the server.
 */
public class UpdateAvailable implements NotificationData {

    private static final LocalizationService LOCALIZATION_SERVICE = LocalizationService.getInstance();
    private static final Logger LOG = LogManager.getLogger(UpdateAvailable.class);
    private static final String UYUNI_PATCH_REPO = "systemsmanagement_Uyuni_Stable_Patches";
    private static final String UYUNI_UPDATE_REPO = "systemsmanagement_Uyuni_Stable";

    private final boolean mgr = !ConfigDefaults.get().isUyuni();
    private final String version = StringUtils.substringBeforeLast(ConfigDefaults.get().getProductVersion(), ".");
    private final Runtime runtime;

    /**
     * Constructor allowing to pass the runtime as argument.
     *
     * @param runtimeIn runtime object for command execution
     */
    public UpdateAvailable(Runtime runtimeIn) {
        this.runtime = runtimeIn;
    }

    /**
     * returns true if there are updates available.
     *
     * @return boolean
     **/
    public boolean updateAvailable() {
        boolean hasUpdates = false;
        String repo = mgr ? "SLE-Module-SUSE-Manager-Server-" + version + "-Updates" : UYUNI_PATCH_REPO;

        try {
            Process patchProc = runtime.exec(new String[]{"/bin/bash", "-c",
                    "LC_ALL=C zypper lp -r " + repo + " | grep 'applicable patch'"});
            patchProc.waitFor();
            // 0 here means there are patches
            hasUpdates = (0 == patchProc.exitValue());
            if (!hasUpdates && !mgr) {
                // Check for updates on uyuni when there are no patches
                Process updateProc = runtime.exec(new String[]{"/bin/bash", "-c",
                        "LC_ALL=C zypper lu -r " + UYUNI_UPDATE_REPO + " | grep 'Available Version'"});
                updateProc.waitFor();
                hasUpdates = (0 == updateProc.exitValue());
            }
        }
        catch (IOException e) {
            LOG.warn("Unable to check for updates", e);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("Wait for update check was interrupted", e);
        }
        return hasUpdates;
    }

    @Override
    public NotificationMessage.NotificationMessageSeverity getSeverity() {
        return NotificationMessage.NotificationMessageSeverity.WARNING;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.UpdateAvailable;
    }

    @Override
    public String getSummary() {
        return LOCALIZATION_SERVICE.getMessage("notification.updateavailable.summary");
    }

    @Override
    public String getDetails() {
        String url = mgr ?
                "https://www.suse.com/releasenotes/x86_64/SUSE-MANAGER/" + version + "/index.html" :
                "https://www.uyuni-project.org/pages/stable-version.html";
        return LOCALIZATION_SERVICE.getMessage("notification.updateavailable.detail", url);
    }
}
