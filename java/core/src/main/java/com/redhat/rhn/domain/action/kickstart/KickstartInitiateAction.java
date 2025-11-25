/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2010 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.action.kickstart;


import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;

import com.suse.manager.webui.services.SaltParameters;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cobbler.CobblerConnection;
import org.cobbler.Distro;
import org.cobbler.Profile;
import org.cobbler.SystemRecord;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * KickstartInitiateAction
 */
@Entity
@DiscriminatorValue("19")
public class KickstartInitiateAction extends KickstartAction {
    private static final Logger LOG = LogManager.getLogger(KickstartInitiateAction.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {

        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        KickstartActionDetails ksActionDetails = getKickstartActionDetails();
        String cobblerSystem = ksActionDetails.getCobblerSystemName();
        String host = ksActionDetails.getKickstartHost();
        Map<String, String> bootParams = prepareCobblerBoot(host, cobblerSystem, true);
        Map<String, Object> pillar = new HashMap<>(bootParams);
        pillar.put("uyuni-reinstall-name", "reinstall-system");
        String kOpts = bootParams.get("kopts");

        if (kOpts.contains("autoupgrade=1") || kOpts.contains("uyuni_keep_saltkey=1")) {
            ksActionDetails.setUpgrade(true);
        }
        ret.put(State.apply(List.of(SaltParameters.KICKSTART_INITIATE), Optional.of(pillar)), minionSummaries);

        return ret;
    }

    private Map<String, String> prepareCobblerBoot(String kickstartHost,
                                                   String cobblerSystem,
                                                   boolean autoinstall) {
        CobblerConnection con = CobblerXMLRPCHelper.getAutomatedConnection();
        SystemRecord system = SystemRecord.lookupByName(con, cobblerSystem);
        Profile profile = system.getProfile();
        Distro dist = profile.getDistro();
        String kernel = dist.getKernel();
        String initrd = dist.getInitrd();

        List<String> nameParts = Arrays.asList(dist.getName().split(":"));
        String saltFSKernel = Paths.get(nameParts.get(1), nameParts.get(0), new File(kernel).getName()).toString();
        String saltFSInitrd = Paths.get(nameParts.get(1), nameParts.get(0), new File(initrd).getName()).toString();
        KickstartableTree tree = KickstartFactory.lookupKickstartTreeByLabel(nameParts.get(0),
                OrgFactory.lookupById(Long.valueOf(nameParts.get(1))));
        tree.createOrUpdateSaltFS();
        String kOpts = buildKernelOptions(system, kickstartHost, autoinstall);


        Map<String, String> pillar = new HashMap<>();
        pillar.put("kernel", saltFSKernel);
        pillar.put("initrd", saltFSInitrd);
        pillar.put("kopts", kOpts);

        return pillar;
    }



    private static String buildKernelOptions(SystemRecord sys, String host, boolean autoinstall) {
        String breed = sys.getProfile().getDistro().getBreed();
        Map<String, Object> kopts = sys.getResolvedKernelOptions();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Resolved kernel options for {}: {}", sys.getName(), convertOptionsMap(kopts));
        }
        if (breed.equals("suse")) {
            //SUSE is not using 'text'. Instead 'textmode' is used as kernel option.
            if (kopts.containsKey("textmode")) {
                kopts.remove("text");
            }
            else if (kopts.containsKey("text")) {
                kopts.remove("text");
                kopts.put("textmode", "1");
            }
        }
        // no additional initrd parameter allowed
        kopts.remove("initrd");
        String kernelOptions = convertOptionsMap(kopts);
        String autoinst = "http://" + host + "/cblr/svc/op/autoinstall/system/" + sys.getName();

        if (StringUtils.isBlank(breed) || breed.equals("redhat")) {
            if (sys.getProfile().getDistro().getOsVersion().equals("rhel6")) {
                kernelOptions += " kssendmac ks=" + autoinst;
            }
            else {
                kernelOptions += " inst.ks.sendmac ks=" + autoinst;
            }
        }
        else if (breed.equals("suse")) {
            kernelOptions += "autoyast=" + autoinst;
        }
        else if (breed.equals("debian") || breed.equals("ubuntu")) {
            kernelOptions += "auto-install/enable=true priority=critical netcfg/choose_interface=auto url=" + autoinst;
        }

        if (autoinstall) {
            String infoUrl = "http://" + host + "/cblr/svc/op/nopxe/system/" + sys.getName();
            kernelOptions += " info=" + infoUrl;
        }
        return kernelOptions;
    }



    private static String convertOptionsMap(Map<String, Object> map) {
        StringBuilder string = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            List<String> keyList;
            try {
                keyList = (List<String>)entry.getValue();
            }
            catch (ClassCastException e) {
                keyList = new ArrayList<>();
                keyList.add((String) entry.getValue());
            }
            string.append(key);
            if (keyList.isEmpty()) {
                string.append(" ");
            }
            else {
                for (String value : keyList) {
                    string.append("=").append(value).append(" ");
                }
            }
        }
        return string.toString();
    }

}
