/*
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.webui.services.pillar;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Pillar;

import java.util.Optional;

/**
 * Class for generating pillar data for the virtual hosts
 */
public class MinionVirtualizationPillarGenerator extends MinionPillarGeneratorBase {

    public static final MinionVirtualizationPillarGenerator INSTANCE = new MinionVirtualizationPillarGenerator();


    /**
     * Generates pillar activating the virtpoller on a virtualization host
     * @param minion the minion server
     * @return the SaltPillar containing the pillar data
     */
    @Override
    public Optional<Pillar> generatePillarData(MinionServer minion) {
        // The virtpoller pillar data used to be created on minions in the past.
        // We want to ensure those are not lingering around.
        removePillar(minion);
        return Optional.empty();
    }

    @Override
    public String getCategory() {
        return "virtualization";
    }
}
