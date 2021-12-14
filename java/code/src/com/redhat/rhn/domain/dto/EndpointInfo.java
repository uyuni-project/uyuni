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

package com.redhat.rhn.domain.dto;

import java.util.Optional;

/**
 * Class for representing the endpoint information of applications or Prometheus exporters of a minion system
 */
public class EndpointInfo {

    private final Long systemID;
    private final String endpointName;
    private final Integer port;
    private final String path;
    private final String module;
    private final String exporterName;
    private final Boolean tlsEnabled;

    /**
     * Instantiates a new endpoint information
     * @param systemIDIn server ID
     * @param endpointNameIn endpoint name
     * @param exporterNameIn
     * @param portIn
     * @param moduleIn
     * @param pathIn
     * @param tlsEnabledIn
     */
    public EndpointInfo(Long systemIDIn,
                        String endpointNameIn,
                        String exporterNameIn,
                        Integer portIn,
                        String moduleIn,
                        String pathIn,
                        Boolean tlsEnabledIn) {
        this.systemID = systemIDIn;
        this.endpointName = endpointNameIn;
        this.exporterName = exporterNameIn;
        this.port = portIn;
        this.module = moduleIn;
        this.path = pathIn;
        this.tlsEnabled = tlsEnabledIn;
    }

    public Long getSystemID() {
        return systemID;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public Integer getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public String getModule() {
        return module;
    }

    public Optional<String> getExporterName() {
        return Optional.ofNullable(exporterName);
    }

    public Boolean isTlsEnabled() {
        return tlsEnabled;
    }

    @Override
    public String toString() {
        return "EndpointInfo{" +
                "systemID=" + systemID +
                ", endpointName='" + endpointName + '\'' +
                ", port=" + port +
                ", path='" + path + '\'' +
                ", module='" + module + '\'' +
                ", exporterName='" + exporterName + '\'' +
                ", tlsEnabled='" + tlsEnabled + '\'' +
                '}';
    }
}
