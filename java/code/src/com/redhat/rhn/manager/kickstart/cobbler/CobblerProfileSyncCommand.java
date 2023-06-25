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

import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redstone.xmlrpc.XmlRpcFault;


/**
 * This command finds profiles that have been changed on the cobbler server and syncs
 *  those changes to the satellite
 */
public class CobblerProfileSyncCommand extends CobblerCommand {

    private Logger log;

    /**
     * Command to sync unsynced Kickstart profiles to cobbler.
     */
    public CobblerProfileSyncCommand() {
        super();
        log = LogManager.getLogger(this.getClass());
    }





    /**
     *  Get a map of CobblerID -> profileMap from cobbler
     * @return a list of cobbler profile names
     */
    private Map<String, Map> getModifiedProfileNames() {
        Map<String, Map> toReturn = new HashMap<>();
        List<Map> profiles = (List<Map>)invokeXMLRPC("get_profiles", xmlRpcToken);
        for (Map profile : profiles) {
                toReturn.put((String)profile.get("uid"), profile);
        }
        return toReturn;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ValidatorError store() {
        //First are there any profiles within spacewalk that aren't within cobbler
        List<KickstartData> profiles = KickstartFactory.listAllKickstartData();
        Map<String, Map> profileNames = getModifiedProfileNames();
        for (KickstartData profile : profiles) {
            /**
             * workaround for bad data left in the DB (bz 525561)
             */
            if (profile.getKickstartDefaults() == null) {
                continue;
            }

            if (!profileNames.containsKey(profile.getCobblerId())) {
                  if (profile.getKickstartDefaults().getKstree().getCobblerId() == null) {
                      log.warn("Kickstart profile {} could not be synced to cobbler, due to it's tree " +
                              "being unsynced. Please edit the tree url to correct this.", profile.getLabel());
                  }
                  else {
                      createProfile(profile);
                      profile.setModified(new Date());
                  }
            }
        }


        log.debug(profiles);
        log.debug(profileNames);
        //Are there any profiles on cobbler that have changed
        for (KickstartData profile : profiles) {
            if (profileNames.containsKey(profile.getCobblerId())) {
                Map cobProfile = profileNames.get(profile.getCobblerId());
                log.debug("{}: {} - {}", profile.getLabel(), cobProfile.get("mtime"), profile.getModified().getTime());
                if (((Double)cobProfile.get("mtime")).longValue() >
                      profile.getModified().getTime() / 1000) {
                    syncProfileToSpacewalk(cobProfile, profile);
                }
            }
        }


        return new CobblerSyncCommand(user).store();
    }

    private void createProfile(KickstartData profile) {
        CobblerProfileCreateCommand creator =
                new CobblerProfileCreateCommand(profile, false);
        creator.store();
    }


    /**
     * Sync s the following things:
     *  Distro (if applicable)
     *
     * then overwrites the 'autoinstall' attribute within the cobbler profile
     *      (in case they changed it to something spacewalk doesn't know about)
     * @param cobblerProfile
     * @param profile
     */
    private void syncProfileToSpacewalk(Map cobblerProfile, KickstartData profile) {
        log.debug("Syncing profile: {} known in cobbler as: {}", profile.getLabel(), cobblerProfile.get("name"));
        //Do we need to sync the distro?
        Map distro = (Map) invokeXMLRPC("get_distro", cobblerProfile.get("distro"));
        if (!distro.get("uid").equals(profile.getTree().getCobblerId()) &&
               !distro.get("uid").equals(profile.getTree().getCobblerXenId())) {
            //lookup the distro locally:
            KickstartableTree tree = KickstartFactory.
                    lookupKickstartTreeByCobblerIdOrXenId((String)distro.get("uid"));
            if (tree == null) {
                log.error("Kickstartable tree was not found for Cobbler id:{}", (String) distro.get("uid"));
            }
            else {
                profile.setTree(tree);
            }
        }

        //Now re-set the filename in case someone set it incorrectly
        Path kickstartPath = Path.of(
                ConfigDefaults.get().getKickstartConfigDir(),
                cobblerProfile.get("autoinstall").toString()
        );
        String cobblerKickstartFileName = profile.buildCobblerFileName();
        if (!Path.of(cobblerKickstartFileName).equals(kickstartPath)) {
            try {
                log.info("Updating cobbler profile, setting 'autoinstall' to: {}", cobblerKickstartFileName);
                String handle = (String) invokeXMLRPC("get_profile_handle",
                        cobblerProfile.get("name"), xmlRpcToken);
                invokeXMLRPC("modify_profile", handle, "autoinstall", cobblerKickstartFileName,
                        xmlRpcToken);

                invokeXMLRPC("save_profile", handle, xmlRpcToken);

                //Lets update the modified date just to make sure
                profile.setModified(new Date());
            }
            catch (RuntimeException re) {
                if (re.getCause() instanceof XmlRpcFault) {
                    XmlRpcFault xrf = (XmlRpcFault)re.getCause();
                    if (xrf.getMessage().contains("unknown profile name")) {
                        log.error("Cobbler doesn't know about this profile any more!");
                    }
                }
                else {
                    throw re;
                }
            }
        }
    }

}
