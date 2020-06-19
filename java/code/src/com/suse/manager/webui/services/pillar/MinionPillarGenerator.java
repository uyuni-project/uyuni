/**
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

import com.suse.manager.webui.utils.SaltPillar;

import java.util.Optional;

/**
 * Common interface for generating specific minion pillar data
 */
public interface MinionPillarGenerator {

    /**
     * Generates specific pillar data for a the passed minion
     * @param minion the minion server
     * @return the SaltPillar containing the pillar data
     */
    Optional<SaltPillar> generatePillarData(MinionServer minion);

    /**
     * Generates the filename of the file disk where the pillar data should be save
     * @param minionId the minion Id
     * @return the filename
     */
    String getFilename(String minionId);

}
