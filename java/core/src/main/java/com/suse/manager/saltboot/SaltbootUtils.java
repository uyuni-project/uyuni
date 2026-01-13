/*
 * Copyright (c) 2025 SUSE LLC
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

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.image.ImageFile;
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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cobbler.CobblerConnection;
import org.cobbler.CobblerObject;
import org.cobbler.Distro;
import org.cobbler.Network;
import org.cobbler.Profile;
import org.cobbler.SystemRecord;
import org.cobbler.XmlRpcException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class SaltbootUtils {
    private static final Logger LOG = LogManager.getLogger(SaltbootUtils.class);
    public static final String DEFAULT_BOOT_IMAGE = "DEFAULT_IMAGE";
    private SaltbootUtils() { }

    private static String makeCobblerFilterNameV(ImageInfo imageInfo) {
        String sep = ConfigDefaults.get().getCobblerNameSeparator();
        String label = imageInfo.getName() + "-" + imageInfo.getVersion();
        label = label.replace(' ', '_').replaceAll("[^a-zA-Z0-9_.-]", "");
        String orgName = imageInfo.getOrg().getName().replaceAll("[^a-zA-Z0-9_-]", "");
        String suffix = sep + "S" + sep + imageInfo.getOrg().getId() + sep + orgName;
        return "^(" + Pattern.quote(label) + "-(\\d+))" + Pattern.quote(suffix) + "$";
    }

    private static String makeCobblerFilterName(ImageInfo imageInfo) {
        String sep = ConfigDefaults.get().getCobblerNameSeparator();
        String label = imageInfo.getName();
        label = label.replace(' ', '_').replaceAll("[^a-zA-Z0-9_.-]", "");
        String orgName = imageInfo.getOrg().getName().replaceAll("[^a-zA-Z0-9_-]", "");
        String suffix = sep + "S" + sep + imageInfo.getOrg().getId() + sep + orgName;
        return "^(" + Pattern.quote(label) + "-\\d+\\.\\d+\\.\\d+-\\d+)" + Pattern.quote(suffix) + "$";
    }

    private static String makeCobblerFilterDefault(Org org) {
        String sep = ConfigDefaults.get().getCobblerNameSeparator();
        String orgName = org.getName().replaceAll("[^a-zA-Z0-9_-]", "");
        String suffix = sep + "S" + sep + org.getId() + sep + orgName;
        return "^(.*-\\d+\\.\\d+\\.\\d+-\\d+)" + Pattern.quote(suffix) + "$";
    }

    private static String makeCobblerFilterSystem(Org org) {
        String sep = ConfigDefaults.get().getCobblerNameSeparator();
        String orgName = org.getName().replaceAll("[^a-zA-Z0-9_-]", "");
        String suffix = sep + "S" + sep + org.getId() + sep + orgName;
        return "^[a-zA-Z0-9_.-]*" + Pattern.quote(suffix) + "$";
    }

    /**
     * Makes a simple saltboot profile or distro object name that fit our cobbler naming convention
     * See also @code com.redhat.rhn.manager.kickstart.cobbler.CobblerCommand.makeCobblerName
     *
     * Created name follows pattern:
     *      label:S:orgId:orgName
     * where spaces are replaced by _ and not allowed characters are removed.
     * S flag between label and orgId indicates a Saltboot entry.
     *
     * @param org the org to appropriately add the org info
     * @param label the distro or profile label
     * @return the cobbler name.
     */
    public static String makeCobblerName(Org org, String label) {
        String sep = ConfigDefaults.get().getCobblerNameSeparator();
        label = label.replace(' ', '_').replaceAll("[^a-zA-Z0-9_.-]", "");

        String orgName = org.getName().replaceAll("[^a-zA-Z0-9_-]", "");
        // mark the saltboot entries with 'S' so the namespaces do not conflict
        String format = "%s" + sep + "S" + sep + "%s" + sep + "%s";
        return String.format(format, label, org.getId(), orgName);
    }

    private static String makeCobblerName(Org org, String name, String version, String release) {
        if (name == null || name.isEmpty()) {
            return makeCobblerName(org, DEFAULT_BOOT_IMAGE);
        }
        else if (version == null || version.isEmpty()) {
            return makeCobblerName(org, name);
        }
        else if (release == null || release.isEmpty()) {
            return makeCobblerName(org, name + "-" + version);
        }
        else {
            return makeCobblerName(org, name + "-" + version + "-" + release);
        }
    }

    private static String makeCobblerName(Org org, String name, String version) {
        return makeCobblerName(org, name, version, "");
    }

    /**
     * Makes a simple saltboot profile or distro object name that fit our cobbler naming convention
     * See also @code com.redhat.rhn.manager.kickstart.cobbler.CobblerCommand.makeCobblerName
     *
     * Created name follows pattern:
     *      label:S:orgId:orgName
     * where spaces are replaced by _ and not allowed characters are removed.
     * S flag between label and orgId indicates a Saltboot entry.
     *
     * @param imageInfo Image details
     * @return the cobbler name.
     */
    public static String makeCobblerNameVR(ImageInfo imageInfo) {
        return makeCobblerName(imageInfo.getOrg(), imageInfo.getName(), imageInfo.getVersion(),
                String.valueOf(imageInfo.getRevisionNumber()));
    }

    private static String makeCobblerNameV(ImageInfo imageInfo) {
        return makeCobblerName(imageInfo.getOrg(), imageInfo.getName(), imageInfo.getVersion());
    }

    private static String makeCobblerName(ImageInfo imageInfo) {
        return makeCobblerName(imageInfo.getOrg(), imageInfo.getName());
    }

    private static String makeCobblerNameDefault(Org org) {
        return makeCobblerName(org, DEFAULT_BOOT_IMAGE);
    }

    private static Map<String, String> splitStringIgnoreQuotes(String input) {
        Map<String, String> result = new HashMap<>();
        // This regex matches tokens separated by whitespace, respecting quotes.
        // One match for single options, up to two for the key=value options.
        String regex = "(?:[^\\s\"']+|\"[^\"]+\"|'[^']+'){1,2}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String tmp = matcher.group();
            // Now we should have individual options, let's get key=value
            String[] keyvalues = tmp.split("=", 2);
            if (keyvalues.length != 2) {
                result.put(keyvalues[0], "");
            }
            else {
                result.put(keyvalues[0], keyvalues[1]);
            }
        }
        return result;
    }


    /**
     * Create saltboot distribution based on provided image and boot image info
     * For each distribution, new profile is created as well
     * <p>Distro name is: imageName-imageVersion:S:orgId:orgName</p>
     * @param imageInfo image info
     */
    public static void createSaltbootDistro(ImageInfo imageInfo) {
        CobblerConnection con = CobblerXMLRPCHelper.getUncachedAutomatedConnection();
        List<Distro> distros = Distro.list(con);
        try {
            con.transactionBegin();
            createSaltbootDistro(imageInfo, distros, con);
            con.transactionCommit();
        }
        catch (Exception e) {
            con.transactionAbort();
            throw e;
        }
    }

    /**
     * Create saltboot distribution based on provided image and boot image info
     * For each distribution, new profile is created as well
     * <p>Distro name is: imageName-imageVersion:S:orgId:orgName</p>
     * @param imageInfo image info
     * @param distros list of existing distributions
     * @param con Cobbler connection
     */
    public static void createSaltbootDistro(ImageInfo imageInfo, List<Distro> distros, CobblerConnection con) {
        String nameVR = makeCobblerNameVR(imageInfo);
        String nameV = makeCobblerNameV(imageInfo);
        String name = makeCobblerName(imageInfo);

        // Return early when distribution already exists
        if (distros.stream().anyMatch(d -> nameVR.equals(d.getName()))) {
            LOG.debug("Saltboot distribution {} already exists", nameVR);
            return;
        }

        final String pathPrefix = OSImageStoreUtils.getOSImageStorePathForImage(imageInfo) +
                imageInfo.getName() + "-" + imageInfo.getVersion() + "-" + imageInfo.getRevisionNumber() + "/";

        Map<String, String> imageFilePaths = imageInfo.getImageFiles().stream()
                .filter(f -> "kernel".equals(f.getType()) || "initrd".equals(f.getType()))
                .collect(Collectors.toMap(ImageFile::getType, f -> pathPrefix + f.getFile()));

        String kernel = imageFilePaths.get("kernel");
        String initrd = imageFilePaths.get("initrd");

        if (initrd == null || kernel == null) {
            throw new SaltbootException("Missing initrd or kernel files from the image");
        }

        // First create actual distro object
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

        // Make a copy of passed distros, as that list can be immutable
        List<Distro> distributions = new ArrayList<>(distros);
        distributions.add(cd);


        // Update DEFAULT_BOOT_IMAGE profile to point to this one
        // As generic default boot image use the latest built image, which is usually this one
        // Reason is that we want latest patches to be generally available
        String defaultImage = makeCobblerNameDefault(imageInfo.getOrg());
        updateDistroProfile(con, defaultImage, cd, "Default image");

        // Update profile when just image name is used
        selectDistro(distributions, makeCobblerFilterName(imageInfo)).ifPresent(n -> {
            if (nameVR.equals(n)) {
                updateDistroProfile(con, name, cd, "Default image for " + name);
            }
        });

        // Update profile when image name-version is used
        selectDistro(distributions, makeCobblerFilterNameV(imageInfo)).ifPresent(n -> {
            if (nameVR.equals(n)) {
                updateDistroProfile(con, nameV, cd, "Default image for " + nameV);
            }
        });
    }

    /**
     * Delete saltboot distribution
     * If distribution is not found, does nothing
     * @param info ImageInfo
     */
    public static void deleteSaltbootDistro(ImageInfo info) throws SaltbootException {
        CobblerConnection con = CobblerXMLRPCHelper.getUncachedAutomatedConnection();
        deleteSaltbootDistro(info, con);
    }

    /**
     * Delete saltboot distribution
     * If distribution is not found, does nothing
     * @param info ImageInfo
     * @param con CobblerConnection
     */
    public static void deleteSaltbootDistro(ImageInfo info, CobblerConnection con) throws SaltbootException {
        Long orgId = info.getOrg().getId();
        String nameVR = makeCobblerNameVR(info);
        String nameV = makeCobblerNameV(info);
        String name = makeCobblerName(info);

        Distro distroToDelete = Distro.lookupByName(con, nameVR);
        if (distroToDelete == null) {
            return;
        }

        List<Distro> distros = Distro.list(con);

        con.transactionBegin();
        try {
            // First delete hidden distro profile
            deleteSaltbootProfile(nameVR, con);

            List<Distro> remainingDistros = distros.stream().filter(
                d -> !nameVR.equals(d.getName())).collect(Collectors.toList());

            selectDistro(remainingDistros, makeCobblerFilterDefault(info.getOrg()))
                 .map(n -> Distro.lookupByName(con, n))
                 .ifPresentOrElse(
                     d -> updateDistroProfile(con, makeCobblerNameDefault(info.getOrg()), d, "Default image"),
                     () -> LOG.error("Can't update the profile for {}", orgId + "-" + DEFAULT_BOOT_IMAGE));

            selectDistro(remainingDistros, makeCobblerFilterName(info))
                 .map(n -> Distro.lookupByName(con, n))
                 .ifPresentOrElse(
                     d -> updateDistroProfile(con, name, d, "Default image for " + name),
                     () -> LOG.error("Can't update the profile for {}", name));

            selectDistro(remainingDistros, makeCobblerFilterName(info))
                 .map(n -> Distro.lookupByName(con, n))
                 .ifPresentOrElse(
                     d -> updateDistroProfile(con, nameV, d, "Default image for " + nameV),
                     () -> LOG.error("Can't update the profile for {}", nameV));

            // then distro itself
            distroToDelete.remove();
            con.transactionCommit();
        }
        catch (Exception e) {
            con.transactionAbort();
            throw e;
        }
    }

    private static Optional<String> selectDistro(List<Distro> distros, String filter) {
        Pattern pattern = Pattern.compile(filter);
        SaltbootVersionCompare saltbootCompare = new SaltbootVersionCompare(pattern);
        return distros
               .stream()
               .map(CobblerObject::getName)
               .filter(s -> pattern.matcher(s).matches())
               .min(saltbootCompare);
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

    static void updateGroupProfile(CobblerConnection con,
                                           ServerGroup saltbootGroup,
                                           String parentProfile,
                                           boolean onlyMissing) {
        // Validate parentProfile exists
        Profile profile = Profile.lookupByName(con, parentProfile);
        if (profile == null) {
            LOG.debug("Cannot find profile using supplied name, trying rename");
            parentProfile = makeCobblerName(saltbootGroup.getOrg(), parentProfile);
            profile = Profile.lookupByName(con, parentProfile);
        }
        if (profile == null) {
            throw new SaltbootException("Unable to find parent profile " + parentProfile);
        }

        Map<String, String> kernelOptions = getKernelOptions(saltbootGroup);
        Org org = saltbootGroup.getOrg();
        String name = makeCobblerName(org, saltbootGroup.getName());

        Profile gp = Profile.lookupByName(con, name);
        if (gp == null) {
            gp = Profile.create(con, name, parentProfile);
        }
        else {
            if (onlyMissing) {
                return;
            }
            gp.setParent(parentProfile);
        }
        gp.<Map<String, String>>setKernelOptions(Optional.of(kernelOptions));
        gp.setComment("Saltboot group " + saltbootGroup.getName() +
              " of organization " + org.getName() + " default profile");
        gp.save();
    }

    private static Map<String, String> getKernelOptions(ServerGroup group) {
        Map<String, Object> formData = group.getPillarByCategory(FormulaFactory.SALTBOOT_PILLAR)
                .orElseThrow(() -> new SaltbootException("Missing saltboot group pillar"))
                .getPillar();
        Map<String, Object> saltboot = (Map<String, Object>) formData.get("saltboot");
        Map<String, String> kernelOptions = new HashMap<>();
        kernelOptions.put("MINION_ID_PREFIX", group.getName());
        kernelOptions.put("MASTER", (String)saltboot.get("download_server"));
        if (Boolean.TRUE.equals(saltboot.get("disable_id_prefix"))) {
            kernelOptions.put("DISABLE_ID_PREFIX", "1");
        }
        if (Boolean.TRUE.equals(saltboot.get("disable_unique_suffix"))) {
            kernelOptions.put("DISABLE_UNIQUE_SUFFIX", "1");
        }
        if ("FQDN".equals(saltboot.get("minion_id_naming"))) {
            kernelOptions.put("USE_FQDN_MINION_ID", "1");
        }
        else if ("HWType".equals(saltboot.get("minion_id_naming"))) {
            kernelOptions.put("DISABLE_HOSTNAME_ID", "1");
        }
        else if ("MAC".equals(saltboot.get("minion_id_naming"))) {
            kernelOptions.put("USE_MAC_MINION_ID", "1");
        }
        String defaultBranchOptions = (String) saltboot.get("default_kernel_parameters");
        if (defaultBranchOptions != null && !defaultBranchOptions.isEmpty()) {
            kernelOptions.putAll(splitStringIgnoreQuotes(defaultBranchOptions));
        }
        return kernelOptions;
    }

    static Optional<String> getGroupImageName(ServerGroup group) {
        Org org = group.getOrg();

        Optional<Map<String, Object>> formDataOpt = group.getPillarByCategory(FormulaFactory.SALTBOOT_PILLAR)
                .map(Pillar::getPillar);
        if (formDataOpt.isEmpty()) {
            return Optional.empty();
        }
        Map<String, Object> saltboot = (Map<String, Object>) formDataOpt.get().get("saltboot");
        String bootImage = (String)saltboot.get("default_boot_image");
        String bootImageVersion = (String)saltboot.get("default_boot_image_version");
        if (bootImage == null || bootImage.isEmpty()) {
            LOG.debug("Using default image for group {}, org {}", group.getName(), org.getName());
            bootImage = DEFAULT_BOOT_IMAGE;
        }

        String parent = makeCobblerName(org, bootImage, bootImageVersion);
        return Optional.of(parent);
    }

    /**
     * Create a Saltboot cobbler profile
     * Saltboot profile is tied with particular saltboot group and contains default boot instructions for new terminals
     * @param saltbootGroup The group for the branch.
     * @throws SaltbootException Throws SaltbootException describing the failure.
     */
    public static void createSaltbootProfile(ServerGroup saltbootGroup) throws SaltbootException {
        CobblerConnection con = CobblerXMLRPCHelper.getAutomatedConnection();
        createSaltbootProfile(saltbootGroup, con);
    }

    /**
     * Create a Saltboot cobbler profile
     * Saltboot profile is tied with particular saltboot group and contains default boot instructions for new terminals
     * @param saltbootGroup The group for the branch.
     * @param con CobblerConnection
     * @throws SaltbootException Throws SaltbootException describing the failure.
     */
    public static void createSaltbootProfile(ServerGroup saltbootGroup, CobblerConnection con)
            throws SaltbootException {
        String groupImageName = getGroupImageName(saltbootGroup).orElseThrow(
                () -> new SaltbootException("Cannot get an image for a saltboot group " + saltbootGroup.getName() +
                        " under organization " + saltbootGroup.getOrg().getName()));
        createSaltbootProfile(saltbootGroup, groupImageName, false, con);
    }

    /**
     * Create a Saltboot cobbler profile
     * Saltboot profile is tied with particular saltboot group and contains default boot instructions for new terminals
     * @param branchGroup The group for the branch.
     * @param image The image name, possibly including version.
     * @param onlyWhenMissing If true, existing groups will be skipped.
     * @param con CobblerConnection
     * @throws SaltbootException Throws SaltbootException describing the failure.
     */
    public static void createSaltbootProfile(ServerGroup branchGroup, String image, Boolean onlyWhenMissing,
        CobblerConnection con) throws SaltbootException {
        try {
            updateGroupProfile(con, branchGroup, image, onlyWhenMissing);
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
        deleteSaltbootProfile(makeCobblerName(org, profileName));
    }

    /**
     * Delete saltboot profile
     * If profile is not found, does nothing
     * @param profileName
     */
    public static void deleteSaltbootProfile(String profileName) {
        CobblerConnection con = CobblerXMLRPCHelper.getAutomatedConnection();
        deleteSaltbootProfile(profileName, con);
    }

    /**
     * Delete saltboot profile
     * If profile is not found, does nothing
     * @param profileName
     * @param con Use this Cobbler connection
     */
    public static void deleteSaltbootProfile(String profileName, CobblerConnection con) {
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

    private static Optional<String> getConflictingSystem(Org org, Throwable e) {
        while (e != null) {
            String msg = e.getMessage();
            // See https://github.com/openSUSE/cobbler/blob/uyuni/master/cobbler/items/system.py#L313
            Pattern pattern = Pattern.compile(
                "MAC address duplicate found.*Object with the conflict has the name \"([^\"]*)\"");
            Matcher match = pattern.matcher(msg);
            if (match.find()) {
                String name = match.group(1);
                Pattern pattern2 = Pattern.compile(makeCobblerFilterSystem(org));
                if (pattern2.matcher(name).matches()) {
                    return Optional.of(name);
                }
            }
            e = e.getCause();
        }
        return Optional.empty();
    }

    /**
     * Create saltboot system record
     * Tied with one particular terminal and contains boot instructions for this terminal
     * @param minion MinionServer
     * @param bootImage Image name including version and revision, used for image profile lookup
     * @param saltbootGroup Name of the saltboot group this system belongs to
     * @param hwAddresses List of Strings with hardware addresses
     * @param kernelParams String with kernel parameters for the system
     * @throws SaltbootException
     */
    public static void createSaltbootSystem(MinionServer minion, String bootImage, String saltbootGroup,
                                            List<String> hwAddresses, String kernelParams) throws SaltbootException {
        CobblerConnection con = CobblerXMLRPCHelper.getAutomatedConnection();
        createSaltbootSystem(minion, bootImage, saltbootGroup, hwAddresses, kernelParams, con);
    }
    /**
     * Create saltboot system record
     * Tied with one particular terminal and contains boot instructions for this terminal
     * @param minion MinionServer
     * @param bootImage Image name including version and revision, used for image profile lookup
     * @param saltbootGroup Name of the saltboot group this system belongs to
     * @param hwAddresses List of Strings with hardware addresses
     * @param kernelParams String with kernel parameters for the system
     * @param con CobblerConnection
     * @throws SaltbootException
     */
    public static void createSaltbootSystem(MinionServer minion, String bootImage, String saltbootGroup,
                                            List<String> hwAddresses, String kernelParams, CobblerConnection con)
            throws SaltbootException {
        String minionId = minion.getMinionId();
        Org org = minion.getOrg();

        Profile profile = Profile.lookupByName(con, makeCobblerName(org, bootImage));
        if (profile == null) {
            throw new SaltbootException("Unable to find profile for boot image " + bootImage);
        }

        Profile group = Profile.lookupByName(con, makeCobblerName(org, saltbootGroup));
        if (group == null) {
            throw new SaltbootException("Unable to find profile for saltboot group " + saltbootGroup);
        }

        // We need to append associated saltboot group settings, particularly MASTER
        Map<String, Object> kernelOptions = group.getKernelOptions().orElse(new HashMap<>());
        if (kernelParams != null && !kernelParams.isEmpty()) {
            kernelOptions.putAll(splitStringIgnoreQuotes(kernelParams));
        }

        String name = makeCobblerName(org, minionId);
        LOG.debug("Creating saltboot system entry {}", name);
        SystemRecord system = SystemRecord.lookupByName(con, name);
        if (system == null) {
            system = SystemRecord.create(con, name, profile);
        }
        else {
            system.setProfile(profile);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Using kernel options {}", system.convertOptionsMap(kernelOptions));
        }

        minion.setCobblerId(system.getId());
        system.<Map<String, Object>>setKernelOptions(Optional.of(kernelOptions));
        List<Network> networks = hwAddresses.stream().map(hw -> {
            Network k = new Network(con, hw);
            k.setMacAddress(hw);
            return k;
        }).collect(Collectors.toList());
        try {
            system.setNetworkInterfaces(networks);
        }
        catch (XmlRpcException e) {
            Optional<String> c = getConflictingSystem(org, e);
            if (c.isPresent()) {
                    LOG.info("Deleting conflicting saltboot profile {}", c.get());
                    SystemRecord csr = SystemRecord.lookupByName(con, c.get());
                    if (csr != null) {
                        csr.remove();
                    }
                    system.setNetworkInterfaces(networks);
            }
            else {
                throw e;
            }
        }
        system.enableNetboot(true);
        system.save();
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

    /**
     * Tries to find an image saltboot profile by progressively simplifying the image name.
     * The profile name is a string in the format "name-version-revision".
     * This method searches for the full name, then "name-version", and finally "name".
     *
     * @param imageName The full name of the image (from the probable_boot_image field).
     * @param org The organization.
     * @return An {@link Optional} containing the found profile name, or empty if none is found.
     */
    public static Optional<String> findImageSaltbootProfile(String imageName, Org org) {
        if (StringUtils.isEmpty(imageName)) {
            return Optional.empty();
        }

        CobblerConnection con = CobblerXMLRPCHelper.getUncachedAutomatedConnection();
        String currentName = imageName;

        do {
            // First, try the old name orgid-name
            Profile profile = Profile.lookupByName(con, org.getId() + "-" + currentName);
            if (profile != null) {
                return Optional.of(currentName);
            }

            // If not found, try the cobbler-mangled name.
            String cobblerName = makeCobblerName(org, currentName);
            profile = Profile.lookupByName(con, cobblerName);
            if (profile != null) {
                return Optional.of(cobblerName);
            }

            // If still not found, shorten the name for the next iteration.
            int lastHyphen = currentName.lastIndexOf('-');
            if (lastHyphen > 0) {
                currentName = currentName.substring(0, lastHyphen);
            }
            else {
                currentName = null;
            }
        } while (currentName != null);

        return Optional.empty();
    }
}
