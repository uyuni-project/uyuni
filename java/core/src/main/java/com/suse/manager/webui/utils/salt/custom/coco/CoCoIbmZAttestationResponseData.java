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

public class CoCoIbmZAttestationResponseData extends CoCoAbstractAttestationResponseData {

    @SuppressWarnings("checkstyle:lineLength")
    @SerializedName("cmd_|-mgr_ibmpvattest_pvattest_response_|-/usr/bin/cat /tmp/cocoattest_ibmpvattest/attestation_response.bin | /usr/bin/base64_|-run")
    private StateApplyResult<CmdResult> pvattestResponse;

    //@SerializedName("cmd_|-mgr_pvattest_response_|-/usr/bin/cat /tmp/cocoattest/response.bin | /usr/bin/base64_|-run")

    public static final String PVATTEST_RESPONSE_TAG = "attestation_response";

    @Override
    public Map<String, Optional<StateApplyResult<CmdResult>>> getResults() {
        Map<String, Optional<StateApplyResult<CmdResult>>> out = new HashMap<>();
        out.put(PVATTEST_RESPONSE_TAG, Optional.ofNullable(pvattestResponse));
        return out;
    }

    /**
     * @return result response item
     */
    public Optional<StateApplyResult<CmdResult>> getPvattestResponse() {
        return Optional.ofNullable(pvattestResponse);
    }

}
