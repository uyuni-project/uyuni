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
package com.redhat.rhn.manager.content.ubuntu;

import com.redhat.rhn.domain.product.Tuple3;

import java.time.Instant;
import java.util.List;

/**
 * Entry containing all ubuntu errata information needed to create an errata for susemanager.
 */
public class Entry {

    private final String id;
    private final List<String> cves;
    private final String summary;
    private final String isummary;
    private final Instant date;
    private final String description;
    private final boolean reboot;
    private final List<Tuple3<String, String, List<String>>> packages;

    /**
     * Default constructor.
     * @param idIn errata id
     * @param cvesIn list of CVEs
     * @param summaryIn errata summary
     * @param isummaryIn
     * @param dateIn issue date
     * @param descriptionIn errata description
     * @param rebootIn reboot required flag
     * @param packagesIn list of errata package information
     */
    public Entry(String idIn, List<String> cvesIn, String summaryIn, String isummaryIn,
          Instant dateIn, String descriptionIn, boolean rebootIn,
          List<Tuple3<String, String, List<String>>> packagesIn) {
        this.id = idIn;
        this.cves = cvesIn;
        this.summary = summaryIn;
        this.date = dateIn;
        this.description = descriptionIn;
        this.reboot = rebootIn;
        this.packages = packagesIn;
        this.isummary = isummaryIn;
    }

    /**
     * @return summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return CVEs
     */
    public List<String> getCves() {
        return cves;
    }

    /**
     * @return issue date
     */
    public Instant getDate() {
        return date;
    }

    /**
     * @return package information
     */
    public List<Tuple3<String, String, List<String>>> getPackages() {
        return packages;
    }

    /**
     * @return isummary
     */
    public String getIsummary() {
        return isummary;
    }

    /**
     * @return reboot flag
     */
    public boolean isReboot() {
        return reboot;
    }
}
