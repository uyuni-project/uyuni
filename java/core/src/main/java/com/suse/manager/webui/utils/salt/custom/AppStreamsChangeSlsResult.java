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

import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.StateApplyResult;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AppStreamsChangeSlsResult {

    @SerializedName("mgrcompat_|-enabled_appstreams_|-appstreams.get_enabled_modules_|-module_run")
    private Optional<StateApplyResult<Ret<Set<Map<String, String>>>>> currentlyEnabled = Optional.empty();

    public Optional<StateApplyResult<Ret<Set<Map<String, String>>>>> getChanges() {
        return currentlyEnabled;
    }

    public Set<Map<String, String>> getCurrentlyEnabled() {
        return currentlyEnabled.isPresent() ? currentlyEnabled.get().getChanges().getRet() : Collections.emptySet();
    }
}
