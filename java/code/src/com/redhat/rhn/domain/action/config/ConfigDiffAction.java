/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.action.config;

import com.redhat.rhn.domain.server.MinionSummary;

import com.suse.manager.webui.services.ConfigChannelSaltManager;
import com.suse.manager.webui.services.SaltParameters;
import com.suse.salt.netapi.calls.LocalCall;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ConfigDiffAction - Class representing TYPE_CONFIGFILES_DIFF
 */
public class ConfigDiffAction extends ConfigAction {

    /**
     * Deploy files(files, directory, symlink) through state.apply
     *
     * @param minionSummaries a list of minion summaries of the minions involved in the given Action
     * @return minion summaries grouped by local call
     */
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        List<Map<String, Object>> fileStates = getConfigRevisionActions().stream()
                .map(ConfigRevisionAction::getConfigRevision)
                .filter(revision -> revision.isFile() ||
                        revision.isDirectory() ||
                        revision.isSymlink())
                .map(revision -> ConfigChannelSaltManager.getInstance().getStateParameters(revision))
                .toList();
        ret.put(com.suse.salt.netapi.calls.modules.State.apply(
                List.of(SaltParameters.CONFIG_DIFF_FILES),
                Optional.of(Collections.singletonMap(SaltParameters.PARAM_FILES, fileStates)),
                Optional.of(true), Optional.of(true)), minionSummaries);
        return ret;
    }

}
