/*
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.manager.webui.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * YAML generator for the Salt config channels.
 */
public class SaltConfigChannelState implements SaltState {

    private List<String> includedStates = new ArrayList<>();

    /**
     * @param includedStatesIn the states to be included in order of priority (high to low)
     */
    public SaltConfigChannelState(List<String> includedStatesIn) {
        this.includedStates = includedStatesIn;
    }

    /**
     * Get the data structure to be serialized as YAML.
     *
     * @return data structure to be serialized as YAML
     */
    @Override
    public Map<String, Object> getData() {
        Map<String, Object> state = new LinkedHashMap<>();
        List<String> include = new ArrayList<>(includedStates);
        // since states are ordered highest to lowest priority and salt will execute them in the order of the list,
        // we need to reverse the list so the highest priority state will run last. This way higher priority states can
        // override the effects of lower priority states i.e deploying to the same file path
        Collections.reverse(include);
        state.put("include", include);
        return state;
    }

    /**
     * @return the included states
     */
    public List<String> getIncludedStates() {
        return includedStates;
    }
}
