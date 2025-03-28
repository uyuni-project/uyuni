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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Pillar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Class for generating pillar data for the virtual hosts
 */
public class MinionVirtualizationPillarGenerator extends MinionPillarGeneratorBase {

    private static final Logger LOG = LogManager.getLogger(MinionVirtualizationPillarGenerator.class);
    public static final MinionVirtualizationPillarGenerator INSTANCE = new MinionVirtualizationPillarGenerator();
    private static final Map<String, Object> VIRTPOLLER_BEACON_PROPS = new HashMap<>();
    public static final String CATEGORY = "virtualization";

    static {
        VIRTPOLLER_BEACON_PROPS.put("cache_file", Config.get().getString(ConfigDefaults.VIRTPOLLER_CACHE_FILE));
        VIRTPOLLER_BEACON_PROPS.put("expire_time", Config.get().getInt(ConfigDefaults.VIRTPOLLER_CACHE_EXPIRATION));
        VIRTPOLLER_BEACON_PROPS.put("interval", Config.get().getInt(ConfigDefaults.VIRTPOLLER_INTERVAL));
    }

    /**
     * Generates pillar activating the virtpoller on a virtualization host
     * @param minion the minion server
     * @return the SaltPillar containing the pillar data
     */
    @Override
    public Optional<Pillar> generatePillarData(MinionServer minion) {
        LOG.debug("Generating virtualization pillar file for minion: {}", minion.getMinionId());

        Pillar pillar = null;
        if (minion.hasVirtualizationEntitlement()) {

            pillar = minion.getPillarByCategory(CATEGORY).orElseGet(() -> {
                Pillar newPillar = new Pillar(CATEGORY, new HashMap<>(), minion);
                minion.getPillars().add(newPillar);
                return newPillar;
            });
            pillar.getPillar().clear();

            // this add the configuration for the beacon that tell us about
            // virtual guests running on that minion
            // The virtpoller is still usefull with the libvirt events: it will help
            // synchronizing the DB with the actual guest lists in case we had a temporary shutdown.
            Map<String, Object> beaconConfig = new HashMap<>();
            beaconConfig.put("virtpoller", VIRTPOLLER_BEACON_PROPS);
            pillar.add("beacons", beaconConfig);
        }

        return Optional.ofNullable(pillar);
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }
}
