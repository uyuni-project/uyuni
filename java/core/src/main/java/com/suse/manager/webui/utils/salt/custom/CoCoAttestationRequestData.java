/*
 * Copyright (c) 2024--2025 SUSE LLC
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

package com.suse.manager.webui.utils.salt.custom;

import com.suse.salt.netapi.results.CmdResult;
import com.suse.salt.netapi.results.StateApplyResult;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Confidential Compute Attestation Result Data from cocoattest.requestdata
 */
public class CoCoAttestationRequestData {

    @SerializedName("cmd_|-mgr_snpguest_report_|-/usr/bin/cat /tmp/cocoattest/report.bin | /usr/bin/base64_|-run")
    private StateApplyResult<CmdResult> snpguestResult;

    @SerializedName("cmd_|-mgr_vlek_certificate_|-/usr/bin/cat /tmp/cocoattest/vlek.pem_|-run")
    private StateApplyResult<CmdResult> vlekCertificateResult;

    @SerializedName("cmd_|-mgr_secureboot_enabled_|-/usr/bin/mokutil --sb-state_|-run")
    private StateApplyResult<CmdResult> securebootResult;

    /**
     * Gets the attestation report
     * @return the attestation report if available
     */
    public Optional<StateApplyResult<CmdResult>> getSnpguestReport() {
        return Optional.ofNullable(snpguestResult);
    }

    /**
     * Gets the certificate of Versioned Loaded Endorsement Key (VLEK)
     * @return the certificate if available
     */
    public Optional<StateApplyResult<CmdResult>> getVlekCertificate() {
        return Optional.ofNullable(vlekCertificateResult);
    }

    /**
     * Gets the info if the system has secure boot
     * @return returns if the system has secure boot
     */
    public Optional<StateApplyResult<CmdResult>> getSecureBoot() {
        return Optional.ofNullable(securebootResult);
    }

    /**
     * Gets all info returned as Map for inserting into the report entry
     * @return returns data as Map.
     */
    public Map<String, Object> asMap() {
        Map<String, Object> out = new HashMap<>();
        getSnpguestReport()
                .map(StateApplyResult::getChanges)
                .ifPresent(c -> {
                    if (c.getRetcode() == 0) {
                        out.put("mgr_snpguest_report", c.getStdout());
                    }
                });

        getVlekCertificate()
                .map(StateApplyResult::getChanges)
                .ifPresent(c -> {
                    if (c.getRetcode() == 0) {
                        out.put("mgr_vlek_certificate", c.getStdout());
                    }
                });

        getSecureBoot()
                .map(StateApplyResult::getChanges)
                .ifPresent(c -> {
                    if (!c.getStdout().isEmpty()) {
                        out.put("mgr_secureboot_enabled", c.getStdout());
                    }
                    else {
                        out.put("mgr_secureboot_enabled", c.getStderr());
                    }
                });
        return out;
    }
}
