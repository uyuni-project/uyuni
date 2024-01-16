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
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class for generating pillar data containing information of the server groups memberships of minions
 */
public class MinionGroupMembershipPillarGenerator extends MinionPillarGeneratorBase {

    /** Logger */
    private static final Logger LOG = LogManager.getLogger(MinionGroupMembershipPillarGenerator.class);

    public static final MinionGroupMembershipPillarGenerator INSTANCE = new MinionGroupMembershipPillarGenerator();

    public static final String CATEGORY = "group_memberships";

    /**
     * Generates pillar containing the information of the server groups the passed minion is member of
     * @param minion the minion server
     * @return the Pillar containing the pillar data
     */
    @Override
    public Optional<Pillar> generatePillarData(MinionServer minion) {
        LOG.debug("Generating group memberships pillar file for minion: {}", minion.getMinionId());
        Pillar pillar = minion.getPillarByCategory(CATEGORY).orElseGet(() -> {
            Pillar newPillar = new Pillar(CATEGORY, new HashMap<>(), minion);
            minion.getPillars().add(newPillar);
            return newPillar;
        });
        pillar.getPillar().clear();

        List<String> addonGroupTypes = minion.getEntitledGroupTypes().stream()
                .map(ServerGroupType::getLabel).collect(Collectors.toList());

        List<Long> groupIds = minion.getManagedGroups().stream().map(ServerGroup::getId).collect(Collectors.toList());

        pillar.add("group_ids", groupIds.toArray(new Long[groupIds.size()]));
        pillar.add("addon_group_types", addonGroupTypes.toArray(new String[addonGroupTypes.size()]));

        return Optional.of(pillar);
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }
}
