/*
 * Copyright (c) 2024 SUSE LLC
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

package com.suse.coco.module.snpguest.model;

import java.util.Objects;
import java.util.StringJoiner;

public class AttestationReport {

    private Long id;

    private Long serverId;

    private Integer envType;

    private String inData;

    private String outData;

    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        this.id = idIn;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverIdIn) {
        this.serverId = serverIdIn;
    }

    public Integer getEnvType() {
        return envType;
    }

    public void setEnvType(Integer envTypeIn) {
        this.envType = envTypeIn;
    }

    public String getInData() {
        return inData;
    }

    public void setInData(String inDataIn) {
        this.inData = inDataIn;
    }

    public String getOutData() {
        return outData;
    }

    public void setOutData(String outDataIn) {
        this.outData = outDataIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AttestationReport)) {
            return false;
        }
        AttestationReport that = (AttestationReport) o;
        return Objects.equals(serverId, that.serverId) && Objects.equals(envType, that.envType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverId, envType);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AttestationReport.class.getSimpleName() + "[", "]")
            .add("id=" + id)
            .add("serverId=" + serverId)
            .add("envType=" + envType)
            .add("inData='" + inData + "'")
            .add("outData='" + outData + "'")
            .toString();
    }
}
