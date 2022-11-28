package com.suse.manager.metrics;

import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.system.SystemManager;

import java.util.ArrayList;
import java.util.List;

import io.prometheus.client.Collector;

public class SystemsCollector extends Collector {

    private static final String PRODUCT_NAME = "uyuni";

    private List<Server> servers;

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> out = new ArrayList<>();
        servers = ServerFactory.lookupByIds(SystemManager.listSystemIds());

        out.add(CustomCollectorUtils.gaugeFor("managed_systems", "Number of managed systems",
                getNumberOfSystems(), PRODUCT_NAME));
        out.add(CustomCollectorUtils.gaugeFor("physical_systems", "Number of managed physical systems",
                getNumberOfPhysicalSystems(), PRODUCT_NAME));
        out.add(CustomCollectorUtils.gaugeFor("inactive_systems", "Number of inactive managed systems",
                getNumberOfInactiveSystems(), PRODUCT_NAME));
        out.add(CustomCollectorUtils.gaugeFor("outdated_systems", "Number of systems with outdated packages",
                getNumberOfOutdatedSystems(), PRODUCT_NAME));

        return out;
    }

    private long getNumberOfSystems() {
        return servers.size();
    }

    private long getNumberOfPhysicalSystems() {
        return servers.stream().filter(Server::isVirtualGuest).count();
    }

    private long getNumberOfInactiveSystems() {
        return servers.stream().filter(Server::isInactive).count();
    }

    private long getNumberOfOutdatedSystems() {
        return SystemManager.countOutdatedSystems();
    }
}
