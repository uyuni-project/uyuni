/*
 * Copyright (c) 2022 SUSE LLC
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

package com.suse.manager.saltboot;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.OSImageStoreUtils;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.CustomDataValue;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;

import com.suse.manager.webui.utils.salt.custom.OSImageInspectSlsResult.BootImage;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cobbler.CobblerConnection;
import org.cobbler.Distro;
import org.cobbler.Network;
import org.cobbler.Profile;
import org.cobbler.SystemRecord;
import org.cobbler.XmlRpcException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SaltbootUtils {
    private static final Logger LOG = LogManager.getLogger(SaltbootUtils.class);
    private static final String DEFAULT_IMAGE = "DEFAULT_IMAGE";
    private SaltbootUtils() { }

    /**
     * Create saltboot distribution based on provided image and boot image info
     * For each distribution, new profile is created as well
     * <p>Distro name is: orgId-imageName-imageVersion</p>
     * @param imageInfo image info
     * @param bootImage boot image info relevant to provided image obtained from image inspect
     */
    public static void createSaltbootDistro(ImageInfo imageInfo, BootImage bootImage) {
        CobblerConnection con = CobblerXMLRPCHelper.getAutomatedConnection();
        String pathPrefix = OSImageStoreUtils.getOSImageStorePathForImage(imageInfo);
        pathPrefix += imageInfo.getName() + "-" + imageInfo.getVersion() + "-" + imageInfo.getRevisionNumber() + "/";
        String initrd = pathPrefix + bootImage.getInitrd().getFilename();
        String kernel = pathPrefix + bootImage.getKernel().getFilename();
        String nameVR = imageInfo.getOrg().getId() + "-" + imageInfo.getName() + "-" + imageInfo.getVersion() + "-" +
                imageInfo.getRevisionNumber();
        String nameV = imageInfo.getOrg().getId() + "-" + imageInfo.getName() + "-" + imageInfo.getVersion();
        String name = imageInfo.getOrg().getId() + "-" + imageInfo.getName();
        // Generic breed is required for cobbler not appending any autoyast or kickstart keywords
        Distro cd = new Distro.Builder<String>()
                .setName(nameVR)
                .setInitrd(initrd)
                .setKernel(kernel)
                .setKernelOptions(Optional.of("panic=60 splash=silent"))
                .setArch(imageInfo.getImageArch().getName()).setBreed("generic")
                .build(con);
        cd.setComment("Distro for image " + nameVR + " belonging to organization " + imageInfo.getOrg().getName());
        cd.save();

        // Each distro have its own private profile for individual system records
        // SystemRecords need to be decoupled from saltboot group default profiles
        updateDistroProfile(con, nameVR, cd, "Distro " + nameVR + " private profile");

        List<Distro> distros = Distro.list(con);
        SaltbootVersionCompare saltbootCompare = new SaltbootVersionCompare();
        String defaultImage = imageInfo.getOrg().getId() + "-" + DEFAULT_IMAGE;
        if (nameVR.equals(selectDistro(distros, imageInfo.getOrg().getId() + "-"))) {
            updateDistroProfile(con, defaultImage, cd, "Default image");
        }

        if (nameVR.equals(selectDistro(distros, name + "-"))) {
            updateDistroProfile(con, name, cd, "Default image for " + name);
        }

        if (nameVR.equals(selectDistro(distros, nameV + "-"))) {
            updateDistroProfile(con, nameV, cd, "Default image for " + nameV);
        }


        for (ServerGroup saltbootGroup : Pillar.getGroupsForCategory(FormulaFactory.SALTBOOT_PILLAR)) {
            String parentProfile;
            try {
                parentProfile = getParent(saltbootGroup);
            }
            catch (SaltbootException e) {
                LOG.warn("Can't get image for saltboot group {}-{}: {}",
                         saltbootGroup.getOrg().getId(), saltbootGroup.getName(), e.getMessage());
                continue;
            }

            if (parentProfile.equals(nameVR)) {
                updateGroupProfile(con, saltbootGroup, false);
            }
            if (parentProfile.equals(nameV) || parentProfile.equals(name) || parentProfile.equals(defaultImage)) {
                updateGroupProfile(con, saltbootGroup, true);
            }
        }
    }

    /**
     * Delete saltboot distribution
     * If distribution is not found, does nothing
     * @param info
     */
    public static void deleteSaltbootDistro(ImageInfo info) throws SaltbootException {
        CobblerConnection con = CobblerXMLRPCHelper.getAutomatedConnection();
        String nameVR = info.getOrg().getId() + "-" + info.getName() + "-" + info.getVersion() + "-" + info.getRevisionNumber();
        String nameV = info.getOrg().getId() + "-" + info.getName() + "-" + info.getVersion();
        String name = info.getOrg().getId() + "-" + info.getName();

        // First delete hidden distro profile
        deleteSaltbootProfile(nameVR);

        List<Distro> distros = Distro.list(con);
        List<Distro> remainingDistros = distros.stream().filter(d -> !nameVR.equals(d.getName())).collect(Collectors.toList());

        try {
            Distro d = Distro.lookupByName(con, selectDistro(remainingDistros, info.getOrg().getId() + "-"));
            updateDistroProfile(con, info.getOrg().getId() + "-" + DEFAULT_IMAGE, d, "Default image");

            d = Distro.lookupByName(con, selectDistro(remainingDistros, name + '-'));
            updateDistroProfile(con, name, d, "Default image for " + name);

            d = Distro.lookupByName(con, selectDistro(remainingDistros, nameV + '-'));
            updateDistroProfile(con, nameV, d, "Default image for " + nameV);
        }
        catch (SaltbootException e) {
                LOG.error("Can't update the default: {}", e.getMessage());
        }
        // then distro itself
        Distro d = Distro.lookupByName(con, nameVR);
        if (d != null) {
            d.remove();
        }
    }

    private static String selectDistro(List<Distro> distros, String filter) {
        SaltbootVersionCompare saltbootCompare = new SaltbootVersionCompare();
        return distros
               .stream()
               .map(d -> d.getName())
               .filter(s -> s.startsWith(filter))
               .min(saltbootCompare)
               .orElseThrow(() -> new SaltbootException("Specified image name is not found: " + filter));
    }

    private static void updateDistroProfile(CobblerConnection con, String name, Distro d, String comment) {
        Profile p = Profile.lookupByName(con, name);
        if (p == null) {
            p = Profile.create(con, name, d);
        }
        else {
            p.setDistro(d);
        }
        p.setEnableMenu(false);
        p.setKickstart("");
        p.setComment(comment);
        p.save();
    }

    private static void updateGroupProfile(CobblerConnection con, ServerGroup saltbootGroup, boolean onlyMissing) {
        String kernelOptions = getKernelOptions(saltbootGroup);
        Org org = saltbootGroup.getOrg();

        Profile gp = Profile.lookupByName(con, org.getId() + "-" + saltbootGroup.getName());
        if (gp == null) {
            gp = Profile.create(con, org.getId() + "-" + saltbootGroup.getName(), getParent(saltbootGroup));
        }
        else {
            if (onlyMissing) {
                return;
            }
            gp.setParent(getParent(saltbootGroup));
        }
        gp.<String>setKernelOptions(Optional.of(kernelOptions));
        gp.setComment("Saltboot group " + saltbootGroup.getName() +
              " of organization " + org.getName() + " default profile");
        gp.save();
    }

    private static String getKernelOptions(ServerGroup group) {
        Map<String, Object> formData = group.getPillarByCategory(FormulaFactory.SALTBOOT_PILLAR)
                .orElseThrow(() -> new SaltbootException("Missing saltboot group pillar"))
                .getPillar();
        Map<String, Object> saltboot = (Map<String, Object>) formData.get("saltboot");
        String kernelOptions = "MINION_ID_PREFIX=" + group.getName();
        kernelOptions += " MASTER=" + saltboot.get("download_server");
        if (Boolean.TRUE.equals(saltboot.get("disable_id_prefix"))) {
            kernelOptions += " DISABLE_ID_PREFIX=1";
        }
        if (Boolean.TRUE.equals(saltboot.get("disable_unique_suffix"))) {
            kernelOptions += " DISABLE_UNIQUE_SUFFIX=1";
        }
        if ("FQDN".equals(saltboot.get("minion_id_naming"))) {
            kernelOptions += " USE_FQDN_MINION_ID=1";
        }
        else if ("HWType".equals(saltboot.get("minion_id_naming"))) {
            kernelOptions += " DISABLE_HOSTNAME_ID=1";
        }
        if (StringUtils.isNotEmpty((String) saltboot.get("default_kernel_parameters"))) {
            kernelOptions += " " + saltboot.get("default_kernel_parameters");
        }
        return kernelOptions;
    }
    private static String getParent(ServerGroup group) {
        Org org = group.getOrg();
        Map<String, Object> formData = group.getPillarByCategory(FormulaFactory.SALTBOOT_PILLAR)
                .orElseThrow(() -> new SaltbootException("Missing saltboot group pillar"))
                .getPillar();
        Map<String, Object> saltboot = (Map<String, Object>) formData.get("saltboot");
        String bootImage = (String)saltboot.get("default_boot_image");
        String bootImageVersion = (String)saltboot.get("default_boot_image_version");
        String parent;
        if (bootImage == null || bootImage.isEmpty()) {
            parent = org.getId() + "-" + DEFAULT_IMAGE;
        }
        else if (bootImageVersion == null || bootImageVersion.isEmpty()) {
            parent = org.getId() + "-" + bootImage;
        }
        else {
            parent = org.getId() + "-" + bootImage + "-" + bootImageVersion;
        }
        return parent;
    }

    /**
     * Create saltboot profile
     * Saltboot profile is tied with particular saltboot group and contains default boot instructions for new terminals
     * @param saltbootGroup The group
     * @throws SaltbootException
     */
    public static void createSaltbootProfile(ServerGroup saltbootGroup) throws SaltbootException {
        try {
            CobblerConnection con = CobblerXMLRPCHelper.getAutomatedConnection();
            updateGroupProfile(con, saltbootGroup, false);
        }
        catch (XmlRpcException e) {
            throw new SaltbootException(e);
        }
    }

    /**
     * Delete saltboot profile
     * If profile is not found, does nothing
     * @param profileName
     * @param org
     */
    public static void deleteSaltbootProfile(String profileName, Org org) {
        deleteSaltbootProfile(org.getId() + "-" + profileName);
    }

    /**
     * Delete saltboot profile
     * If profile is not found, does nothing
     * @param profileName
     * @param org
     */
    public static void deleteSaltbootProfile(String profileName) {
        CobblerConnection con = CobblerXMLRPCHelper.getAutomatedConnection();
        Profile p = Profile.lookupByName(con, profileName);

        if (p != null) {
            List<SystemRecord> systems = SystemRecord.listByAssociatedProfile(con, p.getName());
            if (!systems.isEmpty()) {
                throw new SaltbootException("Unable to delete distro, systems are still registered to it");
            }
            if (!p.remove()) {
                throw new SaltbootException("Unable to delete image saltboot distribution for image " + profileName);
            }
        }
    }

    /**
     * Create saltboot system record
     * System record is tied with one particular terminal and contains boot instructions for this terminal
     * @param minionId
     * @param bootImage Image name including version and revision, used for image profile lookup
     * @param saltbootGroup
     * @param hwAddresses
     * @param kernelParams
     * @throws SaltbootException
     */
    public static void createSaltbootSystem(String minionId, String bootImage, String saltbootGroup,
                                            List<String> hwAddresses, String kernelParams) throws SaltbootException {
        CobblerConnection con = CobblerXMLRPCHelper.getAutomatedConnection();
        MinionServer minion = MinionServerFactory.findByMinionId(minionId).orElseThrow(
                () -> new SaltbootException("Unable to find minion entry for minion id " + minionId));
        Org org = minion.getOrg();

        Profile profile = Profile.lookupByName(con, org.getId() + "-" + bootImage);
        if (profile == null) {
            LOG.warn("Unable to find Cobbler profile for specified boot image '{}'", bootImage);
            return;
        }

        Profile group = Profile.lookupByName(con, org.getId() + "-" + saltbootGroup);
        if (group == null) {
            LOG.warn("Unable to find Cobbler profile for saltboot group '{}'", saltbootGroup);
            return;
        }

        // We need to append associated saltboot group settings, particularly MASTER
        kernelParams = kernelParams + group.getKernelOptions().map(opt -> group.convertOptionsMap(opt)).orElse("");

        LOG.debug("Creating saltboot system entry, params: {}", kernelParams);

        String name = org.getId() + "-" + minionId;
        SystemRecord system = SystemRecord.lookupByName(con, name);
        if (system == null) {
            system = SystemRecord.create(con, name, profile);
        }
        else {
            system.setProfile(profile);
        }
        system.<String>setKernelOptions(Optional.of(kernelParams));
        List<Network> networks = hwAddresses.stream().map(hw -> {
            Network k = new Network(con, hw);
            k.setMacAddress(hw);
            return k;
        }).collect(Collectors.toList());
        system.setNetworkInterfaces(networks);
        system.enableNetboot(true);
        system.save();

        minion.setCobblerId(system.getId());
    }

    /**
     * Delete saltboot system record
     * If not found, does nothing
     *
     * @param minionId
     */
    public static void deleteSaltbootSystem(String minionId) {
        CobblerConnection con = CobblerXMLRPCHelper.getAutomatedConnection();
        Org org = MinionServerFactory.findByMinionId(minionId).orElseThrow(
                () -> new SaltbootException("Unable to find minion entry for minion id " + minionId)).getOrg();
        SystemRecord sr = SystemRecord.lookupByName(con, org.getId() + "-" + minionId);
        if (sr != null) {
            sr.remove();
        }
    }

    /**
     * Remove saltboot:force_redeploy and saltboot:force_repartition flags
     * These flags can be both as a saltboot:force* pillars ( from saltboot formula)
     * or custom info saltboot_force_* keys
     *
     * Consumer of these flags is saltboot state
     * @param minionId
     */
    public static void resetSaltbootRedeployFlags(String minionId) {
        MinionServerFactory.findByMinionId(minionId).ifPresentOrElse(
            minion -> {
                // Look for custom_info or formula_saltboot category.
                // If flag is set somewhere else, then we can't reset it
                removeSaltbootRedeployPillar(minion);
                removeSaltbootRedeployCustomInfo(minion);
            },
            () -> LOG.error("Trying to reset saltboot flag for nonexisting minion {}", minionId));
    }

    /**
     * Remove saltboot:force_redeploy and saltboot:force_repartition from saltboot pillar data
     * @param minion
     */
    private static void removeSaltbootRedeployPillar(MinionServer minion) {
        minion.getPillarByCategory("tuning-saltboot").ifPresent(
            pillar -> {
                Map<String, Object> pillarData = pillar.getPillar();
                Map<String, String> saltboot = (Map<String, String>)pillarData.get("saltboot");

                // Check if saltboot data are present at all, remove pillar if there is nothing else
                if (saltboot == null) {
                    if (pillarData.isEmpty()) {
                        minion.getPillars().remove(pillar);
                        HibernateFactory.getSession().remove(pillar);
                    }
                    return;
                }
                boolean changed = false;
                if (saltboot.remove("force_redeploy") != null) {
                    changed = true;
                }
                if (saltboot.remove("force_repartition") != null) {
                    changed = true;
                }
                if (changed) {
                    LOG.debug("saltboot redeploy flags removed");
                    if (saltboot.isEmpty() && pillarData.size() == 1) {
                        // Remove pillar completely if we cleared saltboot data and it was the only entry
                        minion.getPillars().remove(pillar);
                        HibernateFactory.getSession().remove(pillar);
                    }
                    else {
                        pillar.setPillar(pillarData);
                    }
                }
            }
        );
    }

    /**
     * Remove saltboot_force_redeploy and saltboot_force_repartition custom info values from the minion
     * @param minion
     */
    private static void removeSaltbootRedeployCustomInfo(MinionServer minion) {
        CustomDataKey saltbootRedeploy = OrgFactory.lookupKeyByLabelAndOrg("saltboot_force_redeploy", minion.getOrg());
        CustomDataValue redeploy = minion.getCustomDataValue(saltbootRedeploy);
        if (redeploy != null) {
            ServerFactory.removeCustomDataValue(minion, saltbootRedeploy);
        }
        CustomDataKey saltbootRepart = OrgFactory.lookupKeyByLabelAndOrg("saltboot_force_repartition", minion.getOrg());
        CustomDataValue repart = minion.getCustomDataValue(saltbootRepart);
        if (repart != null) {
            ServerFactory.removeCustomDataValue(minion, saltbootRepart);
        }
        if (redeploy != null || repart != null) {
            LOG.debug("saltboot custom info redeploy flags removed");
        }
    }
}
