/*
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

import com.redhat.rhn.domain.server.CustomDataValue;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Pillar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class for generating minion pillar data containing CustomInfo information of minions
 */
public class MinionCustomInfoPillarGenerator implements MinionPillarGenerator {

    /** Logger */
    private static final Logger LOG = LogManager.getLogger(MinionCustomInfoPillarGenerator.class);

    public static final MinionCustomInfoPillarGenerator INSTANCE = new MinionCustomInfoPillarGenerator();

    public static final String CATEGORY = "custom_info";

    /**
     * Generates pillar data containing CustomInfo information of the passed minion
     * @param minion the minion server
     * @return the SaltPillar containing the pillar data
     */
    @Override
    public Optional<Pillar> generatePillarData(MinionServer minion) {
        if (minion.getCustomDataValues().isEmpty()) {
            minion.getPillarByCategory(CATEGORY).ifPresent(pillar -> minion.getPillars().remove(pillar));
            return Optional.empty();
        }
        Pillar pillar = minion.getPillarByCategory(CATEGORY).orElseGet(() -> {
            Pillar newPillar = new Pillar(CATEGORY, new HashMap<>(), minion);
            minion.getPillars().add(newPillar);
            return newPillar;
        });
        pillar.getPillar().clear();
        pillar.add("custom_info", minion.getCustomDataValues().stream()
                .collect(Collectors.toMap(a -> a.getKey().getLabel(), CustomDataValue::getValue)));
        return Optional.of(pillar);
    }

    @Override
    public String getFilename(String minionId) {
        return PILLAR_DATA_FILE_PREFIX + "_" + minionId + "_" + CATEGORY + "." + PILLAR_DATA_FILE_EXT;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }
}
