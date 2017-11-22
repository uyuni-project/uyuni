/**
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * YAML generator for the Salt Custom State.
 */
public class SaltCustomState implements SaltState {

    private Set<String> includedStates = new HashSet<>();

    /**
     * @param includedStatesIn the states to be included
     */
    public SaltCustomState(Set<String> includedStatesIn) {
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
        List<String> included = new ArrayList<>(includedStates);
        state.put("include", included);
        return state;
    }

    /**
     * @return the included states
     */
    public Set<String> getIncludedStates() {
        return includedStates;
    }
}
