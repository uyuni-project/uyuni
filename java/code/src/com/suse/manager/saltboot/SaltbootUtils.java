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

import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.OSImageStoreUtils;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.suse.manager.webui.utils.salt.custom.OSImageInspectSlsResult.BootImage;

import org.cobbler.CobblerConnection;
import org.cobbler.Distro;
import org.cobbler.Profile;
import org.cobbler.SystemRecord;
import org.cobbler.Network;

import java.util.List;
import java.util.stream.Collectors;

public class SaltbootUtils {
    private SaltbootUtils() { }

    public static void createSaltbootDistro(ImageInfo imageInfo, BootImage bootImage) {
        CobblerConnection con = CobblerXMLRPCHelper.getAutomatedConnection();
        String pathPrefix = OSImageStoreUtils.getOSImageStorePathForImage(imageInfo);
        String initrd = pathPrefix + "/" + bootImage.getInitrd().getFilename();
        String kernel = pathPrefix + "/" + bootImage.getKernel().getFilename();
        String name = imageInfo.getName() + "-" + imageInfo.getVersion() + "-" + imageInfo.getRevisionNumber();
        // Generic breed is required for cobbler not appending any autoyast or kickstart keywords
        Distro cd = new Distro.Builder().setName(name)
                .setInitrd(initrd).setKernel(kernel)
                .setKernelOptions("panic=60 splash=silent")
                .setArch(imageInfo.getImageArch().getName()).setBreed("generic").build(con);

        // Each distro have its own private profile for individual system records
        // SystemRecords need to be decoupled from saltboot group default profiles
        Profile profile = Profile.create(con, name, cd);
        profile.setEnableMenu(false);
        profile.setComment("Distro " + name + " private profile");
        profile.save();

        // Check if cobbler default distro is present and update if so. Otherwise, create it
        Distro defaultDistro = Distro.lookupByName(con,"default-latest");
        if (defaultDistro == null) {
            new Distro.Builder().setName("default-latest")
              .setInitrd(initrd).setKernel(kernel)
              .setKernelOptions("panic=60 splash=silent")
              .setArch(imageInfo.getImageArch().getName()).setBreed("generic").build(con);
        }
        else {
            defaultDistro.setInitrd(initrd);
            defaultDistro.setKernel(kernel);
            defaultDistro.save();
        }
    }

    public static void deleteSaltbootDistro(ImageInfo info) {
        CobblerConnection con = CobblerXMLRPCHelper.getAutomatedConnection();
        Distro d = Distro.lookupByName( con,info.getName() + "-" + info.getVersion() + "-" + info.getRevisionNumber());
        if (d != null) {
            d.remove();
        }
    }

    public static void createSaltbootProfile(String saltbootGroup, String kernelOptions,
                                     String bootImage, String bootImageVersion) throws SaltbootException{
        CobblerConnection con = CobblerXMLRPCHelper.getAutomatedConnection();

        Distro d = Distro.lookupByName(con,bootImage + "-" + bootImageVersion);
        if (d == null) {
            throw new SaltbootException("Unable to find Cobbler distribution for specified image and version");
        }
        Profile gp = Profile.lookupByName(con, saltbootGroup);
        if (gp == null) {
            gp = Profile.create(con, saltbootGroup, d);
        }
        gp.setKernelOptions(kernelOptions);
        gp.setComment("Saltboot group " + saltbootGroup + " default profile");
        gp.save();
    }

    public static void deleteSaltbootProfile(String saltbootGroup) {
        CobblerConnection con = CobblerXMLRPCHelper.getAutomatedConnection();
        Profile p = Profile.lookupByName(con, saltbootGroup);
        if (p != null) {
            p.remove();
        }
    }

    public static void createSaltbootSystem(String minionId, String bootImage,
                                            List<String> hwAddresses, String kernelParams) throws SaltbootException {
        CobblerConnection con = CobblerXMLRPCHelper.getAutomatedConnection();

        Profile profile = Profile.lookupByName(con, bootImage);
        if (profile == null) {
            throw new SaltbootException("Unable to find Cobbler profile for specified boot image " + bootImage);
        }

        SystemRecord system = SystemRecord.lookupByName(con, minionId);
        if (system == null) {
            system = SystemRecord.create(con, minionId, profile);
        }
        system.setKernelOptions(kernelParams);
        List<Network> networks = hwAddresses.stream().map(hw -> {
            Network k = new Network(con, hw);
            k.setMacAddress(hw);
            return k;
        }).collect(Collectors.toList());
        system.setNetworkInterfaces(networks);
        system.save();
    }

    public void deleteSaltbootSystem() {

    }
}
