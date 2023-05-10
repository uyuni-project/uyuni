/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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

package com.redhat.rhn.manager.kickstart.cobbler;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.manager.satellite.CobblerSyncCommand;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cobbler.Distro;
import org.cobbler.Profile;

import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This command finds profiles that have been changed on the cobbler server and syncs
 * those changes to the satellite
 */
public class CobblerProfileSyncCommand extends CobblerCommand {

    private final Logger log;

    /**
     * Command to sync unsynced Kickstart profiles to cobbler.
     */
    public CobblerProfileSyncCommand() {
        super();
        log = LogManager.getLogger(this.getClass());
    }

    /**
     * Get a map of CobblerID -> profileMap from cobbler
     *
     * @return a map of cobbler profile uid with the profile object as a value
     */
    private Map<String, Profile> getModifiedProfiles() {
        Map<String, Profile> toReturn = new HashMap<>();
        List<Profile> profiles = Profile.list(cobblerConnection);
        for (Profile profile : profiles) {
                toReturn.put(profile.getUid(), profile);
        }
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidatorError store() {
        //First are there any profiles within spacewalk that aren't within cobbler
        List<KickstartData> ksDataList = KickstartFactory.listAllKickstartData();
        Map<String, Profile> cobblerProfilesMap = getModifiedProfiles();
        for (KickstartData kickstartData : ksDataList) {
            // workaround for bad data left in the DB (bz 525561)
            // https://bugzilla.redhat.com/show_bug.cgi?id=525561
            if (kickstartData.getKickstartDefaults() == null) {
                continue;
            }

            if (!cobblerProfilesMap.containsKey(kickstartData.getCobblerId())) {
                  if (kickstartData.getKickstartDefaults().getKstree().getCobblerId() == null) {
                      log.warn("Kickstart profile {} could not be synced to cobbler, due to it's tree " +
                              "being unsynced. Please edit the tree url to correct this.", kickstartData.getLabel());
                  }
                  else {
                      createProfile(kickstartData);
                      kickstartData.setModified(new Date());
                  }
            }
        }

        log.debug(ksDataList);
        log.debug(cobblerProfilesMap);
        //Are there any profiles on cobbler that have changed
        for (KickstartData profile : ksDataList) {
            if (cobblerProfilesMap.containsKey(profile.getCobblerId())) {
                Profile cobProfile = cobblerProfilesMap.get(profile.getCobblerId());
                log.debug("{}: {} - {}", profile.getLabel(), cobProfile.getModified(), profile.getModified().getTime());
                if (cobProfile.getModified().getTime() > profile.getModified().getTime()) {
                    syncProfileToUyuni(cobProfile, profile);
                }
            }
        }

        // This is triggering a FULL "cobbler sync"!
        return new CobblerSyncCommand(user).store();
    }

    private void createProfile(KickstartData profile) {
        CobblerProfileCreateCommand creator =
                new CobblerProfileCreateCommand(profile, false);
        creator.store();
    }


    /**
     * Sync's a Distro if applicable. Then overwrites the 'autoinstall' attribute within the Cobbler profile
     * (in case they changed it to something spacewalk doesn't know about).
     *
     * @param cobblerProfile The profile that should be synced to Uyuni
     * @param kickstartData The kickstart data that Uyuni is aware of
     */
    private void syncProfileToUyuni(Profile cobblerProfile, KickstartData kickstartData) {
        log.debug("Syncing profile: {} known in cobbler as: {}", kickstartData.getLabel(), cobblerProfile.getName());
        //Do we need to sync the distro?
        Distro distro = cobblerProfile.getDistro();
        if (!distro.getUid().equals(kickstartData.getTree().getCobblerId()) &&
               !distro.getUid().equals(kickstartData.getTree().getCobblerXenId())) {
            //lookup the distro locally:
            KickstartableTree tree = KickstartFactory.
                    lookupKickstartTreeByCobblerIdOrXenId(distro.getUid());
            if (tree == null) {
                log.error("Kickstartable tree was not found for Cobbler id:{}", distro.getUid());
            }
            else {
                kickstartData.setTree(tree);
            }
        }

        //Now re-set the filename in case someone set it incorrectly
        Path kickstartPath = Path.of(
                ConfigDefaults.get().getKickstartConfigDir(),
                cobblerProfile.getKickstart()
        );
        String cobblerKickstartFileName = kickstartData.buildCobblerFileName();
        if (!Path.of(cobblerKickstartFileName).equals(kickstartPath)) {
            log.info("Updating cobbler profile, setting 'autoinstall' to: {}", cobblerKickstartFileName);
            cobblerProfile.setKickstart(cobblerKickstartFileName);
            cobblerProfile.save();
            cobblerProfile.reload();
            // Let's update the modified date just to make sure
            kickstartData.setModified(cobblerProfile.getModified());
        }
    }
}
