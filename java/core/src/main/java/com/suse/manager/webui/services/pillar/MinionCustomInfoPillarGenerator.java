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


import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.CustomDataValue;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Pillar;

import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class for generating minion pillar data containing CustomInfo information of minions
 */
public class MinionCustomInfoPillarGenerator extends MinionPillarGeneratorBase {

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
            minion.getPillarByCategory(CATEGORY).ifPresent(pillar -> {
                minion.getPillars().remove(pillar);
                HibernateFactory.getSession().remove(pillar);
            });
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
    public String getCategory() {
        return CATEGORY;
    }
}
