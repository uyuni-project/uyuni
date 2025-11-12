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
 * SPDX-License-Identifier: GPL-2.0-only
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

    private String vlekCertificate;

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

    public String getVlekCertificate() {
        return vlekCertificate;
    }

    public void setVlekCertificate(String vlekCertificateIn) {
        this.vlekCertificate = vlekCertificateIn;
    }

    public boolean isUsingVlekAttestation() {
        //The Versioned Loaded Endorsement Key (VLEK) is a versioned signing key that is certified by AMD
        //and used by the AMD CPU to sign the AMD SEV-SNP attestation reports

        //if VLEK fails, we assume VCEK.
        return (null != vlekCertificate) && (!vlekCertificate.isEmpty());
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
