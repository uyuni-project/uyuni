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

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.manager.kickstart.KickstartUrlHelper;

import com.suse.manager.autoinstallation.builder.KernelOptionsBuilder;
import com.suse.manager.autoinstallation.builder.KernelOptionsBuilderException;
import com.suse.manager.autoinstallation.KernelOptionsList;

import org.apache.commons.lang3.StringUtils;
import org.cobbler.Profile;
import org.cobbler.SystemRecord;

/**
 * RHEL installer-specific implementation of the KernelOptionsBuilder.
 */
public class RhelKernelOptionsBuilder implements KernelOptionsBuilder {

    private String serverFqdn = ConfigDefaults.get().getJavaHostname();

    /**
     * Set public facing server FQDN. Default is JavaHostname config value
     * @param serverFqdnIn Server FQDN to be used in generated values
     */
    public void setServerFqdn(String serverFqdnIn) {
        this.serverFqdn = serverFqdnIn;
    }

    @Override
    public KernelOptionsList distroOptions(KickstartableTree ksTree) {
        if (ksTree == null) {
            throw new KernelOptionsBuilderException("Kickstartable tree cannot be null");
        }
        KernelOptionsList list = new KernelOptionsList();
        return list.addOption("inst.repo", "https://" + serverFqdn + "/ks/dist/" + ksTree.getLabel());
    }

    @Override
    public KernelOptionsList profileOptions(Profile profile) {
        if (profile == null || StringUtils.isBlank(profile.getName())) {
            throw new KernelOptionsBuilderException("Profile and/or profile name cannot be empty");
        }
        KernelOptionsList list = new KernelOptionsList();
        return list.addOption("inst.auto", KickstartUrlHelper.getCobblerProfileUrl(profile)).
                addOption("inst.auto_insecure");
    }

    @Override
    public KernelOptionsList systemOptions(SystemRecord system) {
        if (system == null || StringUtils.isBlank(system.getName())) {
            throw new KernelOptionsBuilderException("Profile and/or profile name cannot be empty");
        }
        KernelOptionsList list = new KernelOptionsList();
        String autoinst = "https://" + serverFqdn + "/cblr/svc/op/autoinstall/system/" + system.getName();
        if (system.getProfile().getDistro().getOsVersion().equals("rhel6")) {
            list.setFlagIfNotPresent("kssendmac").setOptionIfNotPresent("ks", autoinst);
        }
        else {
            list.setFlagIfNotPresent("inst.ks.sendmac").setOptionIfNotPresent("ks", autoinst);
        }
        return list;
    }

    @Override
    public KernelOptionsList networkBoot(KickstartableTree ksTree, SystemRecord system) {
        if (system == null) {
            throw new KernelOptionsBuilderException("System record cannot be null");
        }
        if (ksTree == null) {
            throw new KernelOptionsBuilderException("Kickstartable tree cannot be null");
        }
        Profile prof = system.getProfile();
        return distroOptions(ksTree).addOptions(profileOptions(prof)).
                addOption("info", "https://" + serverFqdn + "/cblr/svc/op/nopxe/system/" + system.getName());
    }

    @Override
    public KernelOptionsList localBoot(KickstartableTree ksTree, SystemRecord system) {
        if (system == null) {
            throw new KernelOptionsBuilderException("System record cannot be null");
        }
        Profile prof = system.getProfile();
        return distroOptions(ksTree).addOptions(profileOptions(prof));
    }
}
