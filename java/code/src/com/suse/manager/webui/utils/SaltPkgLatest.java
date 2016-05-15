/**
 * Copyright (c) 2015 SUSE LLC
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
 * YAML generator for the Salt Package State.
 */
public class SaltPkgLatest extends AbstractSaltRequisites implements SaltState {
    /**
     * Package data
     */
    static class Package {
        private final String name;

        Package(String nameIn) {
            this.name = nameIn;
        }

        public String getName() {
            return name;
        }

    }

    private final Map<String, SaltPkgLatest.Package> packages;

    /**
     * Constructor.
     */
    public SaltPkgLatest() {
        this.packages = new LinkedHashMap<>();
    }

    /**
     * Add package.
     *
     * @param name package name
     * @return this
     */
    public SaltPkgLatest addPackage(String name) {
        this.packages.put(name, new SaltPkgLatest.Package(name));
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
        List<Map<String, Object>> pkgs = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("refresh", true);
        pkgs.add(map);

        // The list of packages
        List<Object> pkgsList = new ArrayList<>();
        for (SaltPkgLatest.Package pkg : packages.values()) {
            pkgsList.add(pkg.getName());
        }
        map = new HashMap<>();
        map.put("pkgs", pkgsList);
        pkgs.add(map);

        // require the Suma channel
        addRequisites(map);

        // Put the policy
        Map<String, Object> policy = new LinkedHashMap<>();
        policy.put("pkg.latest", pkgs);

        // State is on the top level
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("pkg_latest", policy);
        return state;
    }
}
