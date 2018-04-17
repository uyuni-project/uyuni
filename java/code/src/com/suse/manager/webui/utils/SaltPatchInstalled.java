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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * YAML generator for the Salt Patch State.
 */
public class SaltPatchInstalled extends AbstractSaltRequisites implements SaltState {
    /**
     * Patch data
     */
    static class Patch {
        private final String name;

        Patch(String nameIn) {
            this.name = nameIn;
        }

        public String getName() {
            return name;
        }
    }

    private final Map<String, SaltPatchInstalled.Patch> patches;

    /**
     * Constructor.
     */
    public SaltPatchInstalled() {
        this.patches = new LinkedHashMap<>();
    }

    /**
     * Add package.
     *
     * @param name package name
     * @return this
     */
    public SaltPatchInstalled addPatch(String name) {
        this.patches.put(name, new SaltPatchInstalled.Patch(name));
        return this;
    }

    /**
     * Get the data structure to be serialized as YAML.
     *
     * @return data structure to be serialized as YAML
     */
    @Override
    public Map<String, Object> getData() {
        // Set general settings
        List<Map<String, Object>> ptchs = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("refresh", true);
        ptchs.add(map);

        // The list of patches
        List<Object> advisoryList = new ArrayList<>();
        for (SaltPatchInstalled.Patch patch : patches.values()) {
            advisoryList.add(patch.getName());
        }
        map = new HashMap<>();
        map.put("advisory_ids", advisoryList);
        ptchs.add(map);

        addRequisites(map);

        // Put the policy
        Map<String, Object> policy = new LinkedHashMap<>();
        policy.put("pkg.patch_installed", ptchs);

        // State is on the top level
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("patch_installed", policy);
        return state;
    }
}
