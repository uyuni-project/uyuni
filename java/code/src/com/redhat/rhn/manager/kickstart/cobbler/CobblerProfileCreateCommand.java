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
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.satellite.CobblerSyncCommand;

import org.cobbler.CobblerConnection;
import org.cobbler.Distro;
import org.cobbler.Profile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * KickstartCobblerCommand - class to contain logic to communicate with cobbler
 */
public class CobblerProfileCreateCommand extends CobblerProfileCommand {

    private boolean callCobblerSync;

    /**
     * Constructor
     * @param ksDataIn to sync
     * @param userIn - user wanting to sync with cobbler
     * @param cobblerSync - should store() execute a cobbbler sync
     */
    public CobblerProfileCreateCommand(KickstartData ksDataIn, User userIn,
            boolean cobblerSync) {
        super(ksDataIn, userIn);
        callCobblerSync = cobblerSync;
    }

    /**
     * Constructor
     * @param ksDataIn to sync
     * @param userIn - user wanting to sync with cobbler
     */
    public CobblerProfileCreateCommand(KickstartData ksDataIn, User userIn) {
        this(ksDataIn, userIn, true);
    }

    /**
     * Constructor
     * @param ksDataIn to sync
     * @param cobblerSync - should store() execute a cobbbler sync
     */
    public CobblerProfileCreateCommand(KickstartData ksDataIn, boolean cobblerSync) {
        super(ksDataIn);
        callCobblerSync = cobblerSync;
    }

    /**
     * Call this if you want to use the taskomatic_user.
     *
     * Useful for automated non-user initiated syncs
     * @param ksDataIn to sync
     */
    public CobblerProfileCreateCommand(KickstartData ksDataIn) {
        this(ksDataIn, true);
    }

     /**
     * Save the Cobbler profile to cobbler.
     * @return ValidatorError if there was a problem
     */
    @Override
    public ValidatorError store() {
        CobblerConnection con = getCobblerConnection();
        Distro distro =  getDistroForKickstart();

        if (distro == null) {
            return new ValidatorError("kickstart.cobbler.profile.invalidvirt");
        }

        Profile prof = Profile.create(con, CobblerCommand.makeCobblerName(this.ksData),
                distro);

        Map<String, Object> meta = new HashMap<>();
        meta.put("org", ksData.getOrg().getId().toString());
        prof.setKsMeta(Optional.of(meta));
        KickstartFactory.saveKickstartData(this.ksData);
        prof.setVirtBridge(Optional.of(this.ksData.getDefaultVirtBridge()));
        prof.setVirtCpus(Optional.of(ConfigDefaults.get().getDefaultVirtCpus()));
        prof.setVirtRam(Optional.of(ConfigDefaults.get().getDefaultVirtMemorySize(this.ksData)));
        prof.setVirtFileSize(Optional.of(ConfigDefaults.get().getDefaultVirtDiskSize()));
        prof.setKickstart(this.ksData.buildCobblerFileName());
        prof.save();

        updateCobblerFields(prof);

        ksData.setCobblerId(prof.getUid());
        if (callCobblerSync) {
            return new CobblerSyncCommand(user).store();
        }
        return null;
    }

}
