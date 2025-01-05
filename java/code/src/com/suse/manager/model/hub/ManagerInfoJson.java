/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.model.hub;

import java.util.Objects;
import java.util.StringJoiner;

public class ManagerInfoJson {

    private final String version;

    private final boolean reportDb;

    private final String reportDbName;

    private final String reportDbHost;

    private final int reportDbPort;


    /**
     * Default constructor
     */
    public ManagerInfoJson() {
        version = "0";
        reportDb = false;
        reportDbName = "";
        reportDbHost = "";
        reportDbPort = 5432;
    }

    /**
     * Constructor
     * @param versionIn the version
     * @param reportDbIn has a report DB
     * @param reportDbNameIn the report DB name
     * @param reportDbHostIn the report DB hostname
     * @param reportDbPortIn the report DB port number
     */
    public ManagerInfoJson(String versionIn, boolean reportDbIn, String reportDbNameIn,
                           String reportDbHostIn, int reportDbPortIn) {
        version = versionIn;
        reportDb = reportDbIn;
        reportDbName = reportDbNameIn;
        reportDbHost = reportDbHostIn;
        reportDbPort = reportDbPortIn;
    }

    /**
     * @return return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return return true when a report DB is configured
     */
    public boolean hasReportDb() {
        return reportDb;
    }

    /**
     * @return return the report DB name
     */
    public String getReportDbName() {
        return reportDbName;
    }

    /**
     * @return return the report DB hostname
     */
    public String getReportDbHost() {
        return reportDbHost;
    }

    /**
     * @return return the report DB port number
     */
    public int getReportDbPort() {
        return reportDbPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ManagerInfoJson that)) {
            return false;
        }
        return Objects.equals(version, that.version) &&
                Objects.equals(reportDb, that.reportDb) &&
                Objects.equals(reportDbName, that.reportDbName) &&
                Objects.equals(reportDbHost, that.reportDbHost) &&
                Objects.equals(reportDbPort, that.reportDbPort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, reportDb, reportDbName, reportDbHost, reportDbPort);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ManagerInfoJson.class.getSimpleName() + "[", "]")
                .add("version='" + getVersion() + "'")
                .add("reportDb='" + hasReportDb() + "'")
                .add("reportDbHost='" + getReportDbHost() + "'")
                .toString();
    }
}
