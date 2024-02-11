/*
 * Copyright (c) 2022 SUSE LLC
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
package com.redhat.rhn.domain.server;

import com.redhat.rhn.domain.credentials.ReportDBCredentials;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;

/**
 * MgrServerInfo
 */
public class MgrServerInfo {

    private Server server;
    private PackageEvr version;
    private String reportDbName;
    private String reportDbHost;
    private Integer reportDbPort;
    private ReportDBCredentials reportDbCredentials;
    private Date reportDbLastSynced;
    private Long id;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param sid the server_id to set
     */
    public void setId(Long sid) {
        this.id = sid;
    }

    /**
     * Constructs a SatelliteServer instance.
     */
    public MgrServerInfo() {
        super();
    }

    /**
     * @return Returns the version.
     */
    public PackageEvr getVersion() {
        return version;
    }


    /**
     * @param aVersion The version to set.
     */
    public void setVersion(PackageEvr aVersion) {
        version = aVersion;
    }

    /**
     * @return Returns the reportDbName.
     */
    public String getReportDbName() {
        return reportDbName;
    }


    /**
     * @param reportDbNameIn The reportDbName to set.
     */
    public void setReportDbName(String reportDbNameIn) {
        reportDbName = reportDbNameIn;
    }


    /**
     * @return Returns the reportDbHost.
     */
    public String getReportDbHost() {
        return reportDbHost;
    }


    /**
     * @param reportDbHostIn The reportDbHost to set.
     */
    public void setReportDbHost(String reportDbHostIn) {
        reportDbHost = reportDbHostIn;
    }


    /**
     * @return Returns the reportDbPort.
     */
    public Integer getReportDbPort() {
        return reportDbPort;
    }


    /**
     * @param reportDbPortIn The reportDbPort to set.
     */
    public void setReportDbPort(Integer reportDbPortIn) {
        reportDbPort = reportDbPortIn;
    }

    /**
     * @return Returns the reportDbCredentials.
     */
    public ReportDBCredentials getReportDbCredentials() {
        return reportDbCredentials;
    }


    /**
     * @param reportDbCredentialsIn The reportDbCredentials to set.
     */
    public void setReportDbCredentials(ReportDBCredentials reportDbCredentialsIn) {
        reportDbCredentials = reportDbCredentialsIn;
    }


    /**
     * @return Returns the when the rport db was last synced.
     */
    public Date getReportDbLastSynced() {
        return reportDbLastSynced;
    }


    /**
     * @param lastSyncedIn The lastSynced to set.
     */
    public void setReportDbLastSynced(Date lastSyncedIn) {
        reportDbLastSynced = lastSyncedIn;
    }


    /**
     * @return the server
     */
    public Server getServer() {
        return server;
    }

    /**
     * @param s the server to set
     */
    public void setServer(Server s) {
        this.server = s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MgrServerInfo mgrServerInfo = (MgrServerInfo) o;

        return new EqualsBuilder()
                .append(server, mgrServerInfo.server)
                .append(version, mgrServerInfo.version)
                .append(reportDbName, mgrServerInfo.reportDbName)
                .append(reportDbHost, mgrServerInfo.reportDbHost)
                .append(reportDbPort, mgrServerInfo.reportDbPort)
                .append(reportDbCredentials, mgrServerInfo.reportDbCredentials)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(server)
                .append(version)
                .append(reportDbName)
                .append(reportDbHost)
                .append(reportDbPort)
                .append(reportDbCredentials)
                .toHashCode();
    }
}
