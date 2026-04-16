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

package com.suse.manager.webui.utils.salt.custom.coco;

import com.suse.salt.netapi.results.CmdResult;
import com.suse.salt.netapi.results.StateApplyResult;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CoCoSecureBootAttestationResponseData extends CoCoAbstractAttestationResponseData {

    @SerializedName("cmd_|-mgr_secureboot_enabled_|-/usr/bin/mokutil --sb-state_|-run")
    private StateApplyResult<CmdResult> securebootResult;

    public static final String SECURE_BOOT_ENABLED_TAG = "mgr_secureboot_enabled";

    @Override
    public Map<String, Optional<StateApplyResult<CmdResult>>> getResults() {
        Map<String, Optional<StateApplyResult<CmdResult>>> out = new HashMap<>();
        out.put(SECURE_BOOT_ENABLED_TAG, Optional.ofNullable(securebootResult));
        return out;
    }

    /**
     * @return secure boot enabled response item
     */
    public Optional<StateApplyResult<CmdResult>> getSecureBoot() {
        return Optional.ofNullable(securebootResult);
    }

}
