/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 */
package com.suse.manager.webui.utils.salt.custom;

import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.StateApplyResult;

import com.google.gson.annotations.SerializedName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Map;

public class VmInfoSlsResult {

    private static final Logger LOG = LogManager.getLogger(VmInfoSlsResult.class);

    @SerializedName(value = "mgrcompat_|-mgr_virt_profile_|-virt.vm_info_|-module_run")
    private StateApplyResult<Ret<Map<String, Map<String, Object>>>> vminfo;

    /**
     * @return return virtual machine information
     */
    public Map<String, Map<String, Object>> getVmInfos() {
        if (vminfo == null) {
            LOG.info("No virtual machines found");
            return Collections.emptyMap();
        }
        return vminfo.getChanges().getRet();
    }
}
