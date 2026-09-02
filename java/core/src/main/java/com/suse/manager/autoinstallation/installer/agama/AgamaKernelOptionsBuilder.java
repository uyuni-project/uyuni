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

package com.suse.manager.autoinstallation.installer.agama;

import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.manager.kickstart.KickstartUrlHelper;

import com.suse.manager.autoinstallation.KernelOptionsList;
import com.suse.manager.autoinstallation.builder.AbstractKernelOptionsBuilder;
import com.suse.manager.autoinstallation.builder.KernelOptionsBuilderException;

import org.apache.commons.lang3.StringUtils;
import org.cobbler.Profile;
import org.cobbler.SystemRecord;

/**
 * SLES 16 / Agama installer-specific implementation of the KernelOptionsBuilder.
 */
public class AgamaKernelOptionsBuilder extends AbstractKernelOptionsBuilder {

    @Override
    protected String urlScheme() {
        return "https";
    }

    @Override
    protected String sslVerifySuffix() {
        return "?ssl_verify=no";
    }

    @Override
    public KernelOptionsList distroOptions(KickstartableTree ksTree) {
        if (ksTree == null) {
            throw new KernelOptionsBuilderException("Kickstartable tree cannot be null");
        }
        KernelOptionsList list = new KernelOptionsList();
        list.addMissingOptions(selfUpdateOption(ksTree));
        String rootUrl = "live:" + baseUrl() + "/ks/dist/" + ksTree.getLabel() +
                "/LiveOS/squashfs.img";
        list.setOptionIfNotPresent("root", rootUrl);
        list.setFlagIfNotPresent("rd.noverifyssl");
        return list;
    }

    @Override
    public KernelOptionsList selfUpdateOption(KickstartableTree ksTree) {
        if (ksTree == null) {
            return new KernelOptionsList();
        }
        KernelOptionsList list = new KernelOptionsList();
        installerUpdatesUrl(ksTree).ifPresent(url -> list.setOptionIfNotPresent("self_update", url));
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
        return new KernelOptionsList();
    }
}
