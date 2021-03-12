/**
 * Copyright (c) 2021 SUSE LLC
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

import static com.suse.manager.webui.services.SaltConstants.PILLAR_DATA_FILE_EXT;
import static com.suse.manager.webui.services.SaltConstants.PILLAR_DATA_FILE_PREFIX;

import com.redhat.rhn.domain.server.MinionServer;

import com.suse.manager.webui.utils.SaltPillar;

import org.apache.log4j.Logger;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class for generating minion pillar data containing CustomInfo information of minions
 */
public class MinionCustomInfoPillarGenerator implements MinionPillarGenerator {

    /** Logger */
    private static final Logger LOG = Logger.getLogger(MinionCustomInfoPillarGenerator.class);

    public static final MinionCustomInfoPillarGenerator INSTANCE = new MinionCustomInfoPillarGenerator();

    /**
     * Generates pillar data containing CustomInfo information of the passed minion
     * @param minion the minion server
     * @return the SaltPillar containing the pillar data
     */
    @Override
    public Optional<SaltPillar> generatePillarData(MinionServer minion) {
        if (minion.getCustomDataValues().isEmpty()) {
            return Optional.empty();
        }
        SaltPillar pillar = new SaltPillar();
        pillar.add("custom_info", minion.getCustomDataValues().stream()
                .collect(Collectors.toMap(a -> a.getKey().getLabel(), a -> a.getValue())));
        return Optional.of(pillar);
    }

    @Override
    public String getFilename(String minionId) {
        return PILLAR_DATA_FILE_PREFIX + "_" + minionId + "_custom_info." + PILLAR_DATA_FILE_EXT;
    }

}
