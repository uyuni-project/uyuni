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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * YAML generator for the Salt Includes.
 */
public class SaltInclude implements SaltState {

    private List<String> states;

    /**
     * The constructor.
     * @param statesIn the states to include
     */
    public SaltInclude(String... statesIn) {
        this.states = Arrays.asList(statesIn);
    }

    @Override
    public Map<String, Object> getData() {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("include", states);
        return state;
    }

}
