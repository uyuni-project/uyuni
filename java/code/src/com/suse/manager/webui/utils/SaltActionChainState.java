/**
 * Copyright (c) 2018 SUSE LLC
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

import java.util.Map;
import java.util.TreeMap;

/**
 * YAML generator for a Salt Action Chain.
 */
public class SaltActionChainState implements SaltState {

    private Map<String, Object> data = new TreeMap<>();

    /**
     * @param dataIn the data to be included
     */
    public SaltActionChainState(Map<String, Object> dataIn) {
        this.data = dataIn;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Add an entry to the Action Chain state
     * @param name the name of the entry
     * @param value the value
     */
    public void add(String name, Object value) {
        data.put(name, value);
    }
}
