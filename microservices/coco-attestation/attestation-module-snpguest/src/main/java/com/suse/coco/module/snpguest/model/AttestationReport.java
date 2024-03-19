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

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Represent the data required to verify an attestation report with SNPGuest.
 */
public class AttestationReport {

    private long id;

    private EpycGeneration cpuGeneration;

    private byte[] randomNonce;

    private byte[] report;

    public long getId() {
        return id;
    }

    public void setId(long idIn) {
        this.id = idIn;
    }

    public EpycGeneration getCpuGeneration() {
        return cpuGeneration;
    }

    public void setCpuGeneration(EpycGeneration generationIn) {
        this.cpuGeneration = generationIn;
    }

    public byte[] getRandomNonce() {
        return randomNonce;
    }

    public void setRandomNonce(byte[] randomNonceIn) {
        this.randomNonce = randomNonceIn;
    }

    public byte[] getReport() {
        return report;
    }

    public void setReport(byte[] reportIn) {
        this.report = reportIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AttestationReport)) {
            return false;
        }
        AttestationReport attestationReport = (AttestationReport) o;
        return id == attestationReport.id &&
            cpuGeneration == attestationReport.cpuGeneration &&
            Arrays.equals(randomNonce, attestationReport.randomNonce);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cpuGeneration, Arrays.hashCode(randomNonce));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AttestationReport.class.getSimpleName() + "[", "]")
            .add("id=" + id)
            .add("cpuGeneration=" + cpuGeneration)
            .toString();
    }
}
