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

    @SerializedName("cmd_|-mgr_snpguest_report_|-cat /tmp/cocoattest/report.bin | base64_|-run")
    private StateApplyResult<CmdResult> snpguestResult;

    @SerializedName("cmd_|-mgr_secureboot_enabled_|-mokutil --sb-state_|-run")
    private StateApplyResult<CmdResult> securebootResult;

    /**
     * Gets the attestation report
     * @return the attestation report if available
     */
    public Optional<StateApplyResult<CmdResult>> getSnpguestReport() {
        return Optional.ofNullable(snpguestResult);
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
