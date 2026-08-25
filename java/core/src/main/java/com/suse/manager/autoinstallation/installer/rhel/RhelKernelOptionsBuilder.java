/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.autoinstallation.installer.rhel;

import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.manager.kickstart.KickstartUrlHelper;

import com.suse.manager.autoinstallation.KernelOptionsList;
import com.suse.manager.autoinstallation.builder.AbstractKernelOptionsBuilder;
import com.suse.manager.autoinstallation.builder.KernelOptionsBuilderException;

import org.apache.commons.lang3.StringUtils;
import org.cobbler.Profile;
import org.cobbler.SystemRecord;

/**
 * RHEL installer-specific implementation of the KernelOptionsBuilder.
 */
public class RhelKernelOptionsBuilder extends AbstractKernelOptionsBuilder {

    @Override
    protected String urlScheme() {
        return "http";
    }

    @Override
    public KernelOptionsList distroOptions(KickstartableTree ksTree) {
        if (ksTree == null) {
            throw new KernelOptionsBuilderException("Kickstartable tree cannot be null");
        }
        KernelOptionsList list = new KernelOptionsList();
        if (ksTree.getInstallType().isRhel8OrGreater()) {
            list.setOptionIfNotPresent("inst.repo", baseUrl() + "/ks/dist/" + ksTree.getLabel());
        }
        return list;
    }

    @Override
    public KernelOptionsList profileOptions(Profile profile) {
        if (profile == null || StringUtils.isBlank(profile.getName())) {
            throw new KernelOptionsBuilderException("Profile and/or profile name cannot be empty");
        }
        KernelOptionsList list = new KernelOptionsList();
        return list.setOptionIfNotPresent("inst.auto", KickstartUrlHelper.getCobblerProfileUrl(profile))
                .setFlagIfNotPresent("inst.auto_insecure");
    }

    @Override
    public KernelOptionsList systemOptions(SystemRecord system) {
        if (system == null || StringUtils.isBlank(system.getName())) {
            throw new KernelOptionsBuilderException("System name cannot be empty");
        }
        KernelOptionsList list = new KernelOptionsList();
        String autoinst = autoinstallSystemUrl(system);
        if ("rhel6".equals(system.getProfile().getDistro().getOsVersion())) {
            list.setFlagIfNotPresent("kssendmac").setOptionIfNotPresent("ks", autoinst);
        }
        else {
            list.setFlagIfNotPresent("inst.ks.sendmac").setOptionIfNotPresent("ks", autoinst);
        }
        return list;
    }
}
