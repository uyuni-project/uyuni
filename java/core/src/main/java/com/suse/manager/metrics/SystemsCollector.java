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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.common.RhnConfiguration;
import com.redhat.rhn.domain.common.RhnConfigurationFactory;
import com.redhat.rhn.domain.server.ServerInfo;

import java.util.ArrayList;
import java.util.List;

import io.prometheus.client.Collector;
import jakarta.persistence.Tuple;

public class SystemsCollector extends Collector {

    public static final String PRODUCT_NAME = "uyuni";

    private static long getCountFromNativeQuery(String selectCountQuery) {
        return HibernateFactory.getSession()
                .createNativeQuery(selectCountQuery, Tuple.class)
                .getSingleResult()
                .get("count", Number.class)
                .longValue();
    }

    /**
     * Returns the number of systems with outdated packages
     *
     * @return number of systems with outdated packages
     */
    public static long getNumberOfOutdatedSystems() {
        String selectCountQuery = "SELECT COUNT(DISTINCT(id)) FROM susesystemoverview WHERE outdated_packages > 0";
        return getCountFromNativeQuery(selectCountQuery);
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> out = new ArrayList<>();
        long start = System.nanoTime();
        long numberOfSystems = getNumberOfSystems();

        if (numberOfSystems > 0) {
            out.add(CustomCollectorUtils.gaugeFor("all_systems", "Number of all systems",
                    numberOfSystems, PRODUCT_NAME));
            out.add(CustomCollectorUtils.gaugeFor("virtual_systems", "Number of virtual systems",
                    getNumberOfVirtualSystems(), PRODUCT_NAME));
            out.add(CustomCollectorUtils.gaugeFor("inactive_systems", "Number of inactive systems",
                    getNumberOfInactiveSystems(), PRODUCT_NAME));
            out.add(CustomCollectorUtils.gaugeFor("outdated_systems", "Number of systems with outdated packages",
                    getNumberOfOutdatedSystems(), PRODUCT_NAME));
            out.add(CustomCollectorUtils.gaugeFor("systems_scrape_duration_seconds", "Duration of Uyuni systems " +
                    "statistics scrape", (System.nanoTime() - start) / 1.0E9, PRODUCT_NAME));
        }

        return out;
    }

    private long getNumberOfSystems() {
        String selectCountQuery = "SELECT COUNT(DISTINCT(id)) FROM rhnServer";
        return getCountFromNativeQuery(selectCountQuery);
    }

    private long getNumberOfVirtualSystems() {
        String selectCountQuery = "SELECT COUNT(DISTINCT(virtual_system_id)) FROM rhnvirtualinstance";
        return getCountFromNativeQuery(selectCountQuery);
    }

    protected long getNumberOfInactiveSystems() {
        String selectCountQuery = "SELECT COUNT(DISTINCT(server_id)) " +
                "FROM rhnServerInfo " +
                "WHERE checkin < CURRENT_TIMESTAMP - NUMTODSINTERVAL(:checkin_threshold, 'second')";
        RhnConfigurationFactory factory = RhnConfigurationFactory.getSingleton();
        long threshold = factory.getLongConfiguration(RhnConfiguration.KEYS.SYSTEM_CHECKIN_THRESHOLD).getValue();
        long secondsInDay = 60L * 60 * 24;
        return HibernateFactory.getSession()
                .createNativeQuery(selectCountQuery, Tuple.class)
                .addSynchronizedEntityClass(ServerInfo.class)
                .setParameter("checkin_threshold", threshold * secondsInDay)
                .getSingleResult()
                .get("count", Number.class)
                .longValue();
    }
}
