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
 * Common interface for generating specific minion pillar data
 */
public interface MinionPillarGenerator {

    /**
     * Generates specific pillar data for a the passed minion
     * @param minion the minion server
     * @return the Pillar containing the pillar data
     */
    Optional<Pillar> generatePillarData(MinionServer minion);

    /**
     * @return the pillar category for the generator
     */
    String getCategory();

    /**
     * Remove the pillar data from the given minion
     * @param minion the minion server
     */
    void removePillar(MinionServer minion);

}
