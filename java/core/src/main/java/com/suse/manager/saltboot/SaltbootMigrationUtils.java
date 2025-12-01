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

import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cobbler.CobblerConnection;
import org.cobbler.Distro;
import org.cobbler.Profile;
import org.cobbler.SystemRecord;
import org.cobbler.XmlRpcException;

import java.util.List;

public class SaltbootMigrationUtils {
    private static final Logger LOG = LogManager.getLogger(SaltbootMigrationUtils.class);
    private SaltbootMigrationUtils() { }

    private static String getOldNamingScheme(ImageInfo imageInfo) {
        return imageInfo.getOrg().getId() + "-" +
                imageInfo.getName() + "-" +
                imageInfo.getVersion() + "-" +
                imageInfo.getRevisionNumber();
    }

    private static void migrateSaltbootDistros(CobblerConnection con) throws SaltbootMigrationException {
        List<Distro> distros = Distro.list(con);
        boolean allOk = true;
        con.transactionBegin();
        // Image list is sorted from the oldest to the newest.
        // createSaltbootDistro automatically sets default distro to currently created, so it depends on this order
        try {
            for (ImageInfo imageInfo : ImageInfoFactory.list()) {
                if (!imageInfo.getImageType().equals(ImageProfile.TYPE_KIWI) || !imageInfo.isBuilt()) {
                    continue;
                }
                try {
                    SaltbootUtils.createSaltbootDistro(imageInfo, distros, con);

                    String newName = SaltbootUtils.makeCobblerNameVR(imageInfo);
                    Profile newDistroProfile = Profile.lookupByName(con, newName);
                    if (newDistroProfile == null) {
                        throw new SaltbootMigrationException("Could not find new distribution profile " + newName);
                    }
                    String oldName = getOldNamingScheme(imageInfo);
                    Profile oldDistroProfile = Profile.lookupByName(con, oldName);
                    if (oldDistroProfile != null) {
                        migrateSaltbootSystems(con, oldDistroProfile, newDistroProfile);
                        oldDistroProfile.remove();
                    }
                    Distro oldDistro = Distro.lookupByName(con, oldName);
                    if (oldDistro != null) {
                        oldDistro.remove();
                    }
                }
                catch (XmlRpcException | SaltbootMigrationException e) {
                    LOG.error("Error migrating {}-{}-{}", imageInfo.getName(),
                            imageInfo.getVersion(), imageInfo.getRevisionNumber(), e);
                    allOk = false;
                }
            }
        }
        catch (RuntimeException e) {
            LOG.error("Unknown error detected", e);
            allOk = false;
        }
        finally {
            con.transactionCommit();
        }
        if (!allOk) {
            throw new SaltbootMigrationException(
                    "Errors encountered when creating new saltboot distributions, see log files");
        }
    }

    private static void migrateSaltbootProfiles(CobblerConnection con) {
        boolean allOk = true;
        con.transactionBegin();
        try {
            for (ServerGroup saltbootGroup : Pillar.getGroupsForCategory(FormulaFactory.SALTBOOT_PILLAR)) {
                try {
                    String groupImageName = SaltbootUtils.getGroupImageName(saltbootGroup).orElseThrow(
                            () -> new SaltbootException(String.format(
                                    "Cannot get an image for a saltboot group %s under organization %s",
                                    saltbootGroup.getOrg().getId(), saltbootGroup.getName())));

                    SaltbootUtils.updateGroupProfile(con, saltbootGroup, groupImageName, false);
                    Profile oldProfile = Profile.lookupByName(con,
                            saltbootGroup.getOrg().getId() + "-" + saltbootGroup.getName());
                    if (oldProfile != null) {
                        oldProfile.remove();
                    }
                }
                catch (XmlRpcException e) {
                    LOG.error("Error migrating {}", saltbootGroup.getName(), e);
                    allOk = false;
                }
            }
        }
        catch (RuntimeException e) {
            LOG.error("Unknown error detected", e);
            allOk = false;
        }
        finally {
            con.transactionCommit();
        }
        if (!allOk) {
            throw new SaltbootMigrationException(
                    "Errors encountered when migrating to new saltboot profiles, see log files");
        }
    }

    private static void migrateSaltbootSystems(CobblerConnection con, Profile oldProfile, Profile newProfile) {
        List<SystemRecord> systems = SystemRecord.listByAssociatedProfile(con, oldProfile.getName());
        for (SystemRecord system : systems) {
            system.setProfile(newProfile);
            system.save();
        }
    }

    /**
     * Migrate saltboot cobbler entries to the new naming scheme.
     * Workflow:
     * 1) create new distro and profile entries based on new naming scheme
     * 2) migrate branch profiles and existing system entries to new profile names
     * 3) remove old distro and distro profiles entries
     */
    public static void migrateSaltboot() {
        CobblerConnection con = CobblerXMLRPCHelper.getUncachedAutomatedConnection();
        migrateSaltboot(con);
    }

    /**
     * Migrate saltboot cobbler entries to the new naming scheme.
     * @param con Cobbler connection
     */
    public static void migrateSaltboot(CobblerConnection con) {
        migrateSaltbootDistros(con);
        migrateSaltbootProfiles(con);
    }
}
