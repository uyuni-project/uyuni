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

public class CoCoAmdEpycAttestationResponseData extends CoCoAbstractAttestationResponseData {

    @SuppressWarnings("checkstyle:lineLength")
    @SerializedName("cmd_|-mgr_sevsnp_snpguest_response_|-/usr/bin/cat /tmp/cocoattest_sevsnp/response.bin | /usr/bin/base64_|-run")
    private StateApplyResult<CmdResult> snpguestResponse;

    @SerializedName("cmd_|-mgr_sevsnp_vlek_certificate_|-/usr/bin/cat /tmp/cocoattest_sevsnp/vlek.pem_|-run")
    private StateApplyResult<CmdResult> vlekCertificate;

    public static final String SNP_GUEST_RESPONSE_TAG = "mgr_snpguest_response";
    public static final String VLEK_CERTIFICATE_TAG = "mgr_vlek_certificate";

    @Override
    public Map<String, Optional<StateApplyResult<CmdResult>>> getResults() {
        Map<String, Optional<StateApplyResult<CmdResult>>> out = new HashMap<>();
        out.put(SNP_GUEST_RESPONSE_TAG, Optional.ofNullable(snpguestResponse));
        out.put(VLEK_CERTIFICATE_TAG, Optional.ofNullable(vlekCertificate));
        return out;
    }

    /**
     * @return snpguest result response item
     */
    public Optional<StateApplyResult<CmdResult>> getSnpguestResponse() {
        return Optional.ofNullable(snpguestResponse);
    }

    /**
     * @return vlek certificate response item
     */
    public Optional<StateApplyResult<CmdResult>> getVlekCertificate() {
        return Optional.ofNullable(vlekCertificate);
    }
}
