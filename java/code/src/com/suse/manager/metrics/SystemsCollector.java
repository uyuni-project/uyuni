/*
 * Copyright (c) 2022 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.metrics;

import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.system.SystemManager;

import java.util.ArrayList;
import java.util.List;

import io.prometheus.client.Collector;

public class SystemsCollector extends Collector {

    public static final String PRODUCT_NAME = "uyuni";

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> out = new ArrayList<>();
        long start = System.nanoTime();
        List<Server> servers = ServerFactory.lookupByIds(SystemManager.listSystemIds());

        out.add(CustomCollectorUtils.gaugeFor("all_systems", "Number of all systems",
                getNumberOfSystems(servers), PRODUCT_NAME));
        out.add(CustomCollectorUtils.gaugeFor("virtual_systems", "Number of virtual systems",
                getNumberOfVirtualSystems(servers), PRODUCT_NAME));
        out.add(CustomCollectorUtils.gaugeFor("inactive_systems", "Number of inactive systems",
                getNumberOfInactiveSystems(servers), PRODUCT_NAME));
        out.add(CustomCollectorUtils.gaugeFor("outdated_systems", "Number of systems with outdated packages",
                getNumberOfOutdatedSystems(), PRODUCT_NAME));
        out.add(CustomCollectorUtils.gaugeFor("systems_scrape_duration_seconds", "Duration of Uyuni systems " +
                "statistics scrape", (System.nanoTime() - start) / 1.0E9, PRODUCT_NAME));

        return out;
    }

    private long getNumberOfSystems(List<Server> servers) {
        return servers.size();
    }

    private long getNumberOfVirtualSystems(List<Server> servers) {
        return servers.stream().filter(Server::isVirtualGuest).count();
    }

    private long getNumberOfInactiveSystems(List<Server> servers) {
        return servers.stream().filter(Server::isInactive).count();
    }

    private long getNumberOfOutdatedSystems() {
        return SystemManager.countOutdatedSystems();
    }
}
