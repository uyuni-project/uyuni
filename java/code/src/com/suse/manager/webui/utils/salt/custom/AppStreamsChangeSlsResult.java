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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AppStreamsChangeSlsResult {

    private class ChangesResult {
        private List<String> enabled;
        private List<String> disabled;
        @SerializedName("currently_enabled")
        private Set<Map<String, String>> currentlyEnabled;
    }

    @SerializedName("appstreams_|-change_appstreams_|-change_appstreams_|-change")
    private Optional<StateApplyResult<Ret<ChangesResult>>> changes = Optional.empty();

    public Optional<StateApplyResult<Ret<ChangesResult>>> getChanges() {
        return changes;
    }

    public Set<Map<String, String>> getCurrentlyEnabled() {
        return changes.get().getChanges().getRet().currentlyEnabled;
    }
}
