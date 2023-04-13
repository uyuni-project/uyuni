/*
 * Copyright (c) 2009--2015 Red Hat, Inc.
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
package com.redhat.rhn.manager.report.dto;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.frontend.dto.BaseTupleDto;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Tuple;

/**
 * Simple DTO for transfering data from the DB to the UI through datasource.
 *
 */
public class SystemInventoryOverview extends BaseTupleDto implements Serializable {


    private Long mgmId;
    private Long systemId;

    private String minionId;
    private String machineId;

    private String profileName;
    private String hostname;

    private String lastCheckinTime;
    private String syncedDate;

    private String kernelVersion;
    private Long packagesOutOfDate;
    private Long errataOutOfDate;

    private String organization;
    private String architecture;


    /**
     * Default constructor
     */
    public SystemInventoryOverview() {
    }

    /**
     * Constructor used to populate using JQPL DTO projection from the data of suseSystemOverview
     *
     * @param tuple JPA tuple
     */
    public SystemInventoryOverview(Tuple tuple) {

        mgmId = getTupleValue(tuple, "mgm_id", Number.class).map(Number::longValue).orElse(null);
        systemId = getTupleValue(tuple, "system_id", Number.class).map(Number::longValue).orElse(null);

        minionId = getTupleValue(tuple, "minion_id", String.class).orElse(null);
        machineId = getTupleValue(tuple, "machine_id", String.class).orElse(null);

        profileName = getTupleValue(tuple, "profile_name", String.class).orElse(null);
        hostname = getTupleValue(tuple, "hostname", String.class).orElse(null);

        lastCheckinTime = getTupleValue(tuple, "last_checkin_time", Date.class)
                .map(data-> LocalizationService.getInstance().formatCustomDate(data)).orElse(null);

        syncedDate = getTupleValue(tuple, "synced_date", Date.class)
                .map(data-> LocalizationService.getInstance().formatCustomDate(data)).orElse(null);

        kernelVersion = getTupleValue(tuple, "kernel_version", String.class).orElse(null);
        packagesOutOfDate = getTupleValue(tuple, "packages_out_of_date", Number.class).map(Number::longValue).orElse(0L);
        errataOutOfDate = getTupleValue(tuple, "errata_out_of_date", Number.class).map(Number::longValue).orElse(0L);

        organization = getTupleValue(tuple, "organization", String.class).orElse(null);
        architecture = getTupleValue(tuple, "architecture", String.class).orElse(null);
    }


    @Override
    public Long getId() {
        return systemId;
    }

    public Long getMgmId() {
        return mgmId;
    }

    public void setMgmId(Long mgmIdIn) {
        mgmId = mgmIdIn;
    }

    public Long getSystemId() {
        return systemId;
    }

    public void setSystemId(Long systemIdIn) {
        systemId = systemIdIn;
    }

    public String getMinionId() {
        return minionId;
    }

    public void setMinionId(String minionIdIn) {
        minionId = minionIdIn;
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineIdIn) {
        machineId = machineIdIn;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileNameIn) {
        profileName = profileNameIn;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostnameIn) {
        hostname = hostnameIn;
    }

    public String getLastCheckinTime() {
        return lastCheckinTime;
    }

    public void setLastCheckinTime(String lastCheckinTimeIn) {
        lastCheckinTime = lastCheckinTimeIn;
    }

    public String getSyncedDate() {
        return syncedDate;
    }

    public void setSyncedDate(String syncedDateIn) {
        syncedDate = syncedDateIn;
    }

    public String getKernelVersion() {
        return kernelVersion;
    }

    public void setKernelVersion(String kernelVersionIn) {
        kernelVersion = kernelVersionIn;
    }

    public Long getPackagesOutOfDate() {
        return packagesOutOfDate;
    }

    public void setPackagesOutOfDate(Long packagesOutOfDateIn) {
        packagesOutOfDate = packagesOutOfDateIn;
    }

    public Long getErrataOutOfDate() {
        return errataOutOfDate;
    }

    public void setErrataOutOfDate(Long errataOutOfDateIn) {
        errataOutOfDate = errataOutOfDateIn;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organizationIn) {
        organization = organizationIn;
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architectureIn) {
        architecture = architectureIn;
    }
}
