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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;

/**
 * Base class that provides helpers for working with Salt state requisites.
 */
public abstract class AbstractSaltRequisites implements SaltRequisistes {

    private List<Map<String, String>> requireEntries = new LinkedList<>();

    @Override
    public void addRequire(String state, String id) {
        requireEntries.add(singletonMap(state, id));
    }

    /**
     * @return the list of required requisites.
     */
    public List<Map<String, String>> getRequireEntries() {
        return requireEntries;
    }

    protected void addRequisites(Map<String, Object> stateMap) {
        // add requires (if any)
        if (!requireEntries.isEmpty()) {
            stateMap.putAll(singletonMap("require", requireEntries));
        }
    }

    protected void addRequisites(List<Map<String, ?>> stateMap) {
        // add requires (if any)
        if (!requireEntries.isEmpty()) {
            stateMap.add(singletonMap("require", requireEntries));
        }
    }
}
