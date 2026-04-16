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

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class CoCoAbstractAttestationResponseData {

    /**
     * @return a map of tags to response items
     */
    public abstract Map<String, Optional<StateApplyResult<CmdResult>>> getResults();

    /**
     * @return a map of tags to data items
     */
    public Map<String, Object> asMap() {
        Map<String, Object> outMap = new HashMap<>();

        getResults().entrySet().stream()
                .filter(e -> e.getValue().isPresent())
                .forEach(e -> insertItemInMap(e.getKey(), e.getValue().get(), outMap));

        return outMap;
    }

    protected boolean hasBase64Stdout(StateApplyResult<CmdResult> responseItem) {
        return responseItem.getName()
                .map(x -> x.fold(Arrays::asList, List::of))
                .orElseGet(ArrayList::new)
                .stream()
                .anyMatch(s -> s.contains("/usr/bin/base64"));
    }

    protected void insertItemInMap(String tagKey, StateApplyResult<CmdResult> responseItem,
                                   Map<String, Object> outMap) {
        if (null == responseItem) {
            return;
        }

        Optional.ofNullable(responseItem.getChanges())
                .ifPresent(cmdResult -> {
                    if (hasBase64Stdout(responseItem)) {
                        if (StringUtils.isNotEmpty(cmdResult.getStdout())) {
                            outMap.put(tagKey, cmdResult.getStdout().replace("\n", ""));
                        }
                        else {
                            outMap.put(tagKey, "");
                        }
                    }
                    else if (StringUtils.isNotEmpty(cmdResult.getStdout())) {
                        outMap.put(tagKey, cmdResult.getStdout());
                    }
                    else if (StringUtils.isNotEmpty(cmdResult.getStderr())) {
                        outMap.put(tagKey, cmdResult.getStderr());
                    }
                    else if (StringUtils.isNotEmpty(responseItem.getComment())) {
                        outMap.put(tagKey, responseItem.getComment());
                    }
                });
    }
}
