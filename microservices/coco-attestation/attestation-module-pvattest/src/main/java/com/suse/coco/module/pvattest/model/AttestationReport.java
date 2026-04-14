/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.coco.module.pvattest.model;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Represent the data required to verify an attestation report with Pvattest.
 */

public class AttestationReport {

    private long id;

    private int envType;

    private String hostKeyDocument;

    private byte[] secureExecutionHeader;

    private byte[] attestationProtectionKey;

    private byte[] attestationResponse;


    public long getId() {
        return id;
    }

    public void setId(long idIn) {
        this.id = idIn;
    }

    public int getEnvType() {
        return envType;
    }

    public void setEnvType(int envTypeIn) {
        this.envType = envTypeIn;
    }

    public String getHostKeyDocument() {
        return hostKeyDocument;
    }

    public void setHostKeyDocument(String hostKeyDocumentIn) {
        this.hostKeyDocument = hostKeyDocumentIn;
    }

    public byte[] getSecureExecutionHeader() {
        return secureExecutionHeader;
    }

    public void setSecureExecutionHeader(byte[] secureExecutionHeaderIn) {
        this.secureExecutionHeader = secureExecutionHeaderIn;
    }

    public byte[] getAttestationProtectionKey() {
        return attestationProtectionKey;
    }

    public void setAttestationProtectionKey(byte[] attestationProtectionKeyIn) {
        this.attestationProtectionKey = attestationProtectionKeyIn;
    }

    public byte[] getAttestationResponse() {
        return attestationResponse;
    }

    public void setAttestationResponse(byte[] attestationResponseIn) {
        this.attestationResponse = attestationResponseIn;
    }

    public IbmZGeneration getCpuGeneration() {
        return IbmZGeneration.fromValue(getEnvType());
    }

    /**
     * Sets cpu generation as an environment type
     * @param generationIn the IBM Z series cpu generation
     */
    public void setCpuGeneration(IbmZGeneration generationIn) {
        setEnvType(generationIn.getValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AttestationReport attestationReport)) {
            return false;
        }
        return id == attestationReport.id &&
                envType == attestationReport.envType &&
                hostKeyDocument.equals(attestationReport.hostKeyDocument) &&
                Arrays.equals(secureExecutionHeader, attestationReport.secureExecutionHeader);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, envType, hostKeyDocument, Arrays.hashCode(secureExecutionHeader));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AttestationReport.class.getSimpleName() + "[", "]")
            .add("id=" + id)
            .add("envType=" + envType)
            .toString();
    }
}
