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
public class SaltPkgRemoved extends AbstractSaltRequisites implements SaltState {
    /**
     * Package data
     */
    static class Package {
        private final String name;
        private final String version;
        private final String operator;

        Package(String nameIn, String versionIn, String operatorIn) {
            this.name = nameIn;
            this.version = versionIn;
            this.operator = operatorIn;
        }

        public String getName() {
            return name;
        }

        public String getOperator() {
            return operator;
        }

        public String getVersion() {
            return version;
        }
    }

    private final Map<String, SaltPkgRemoved.Package> packages;

    /**
     * Constructor.
     */
    public SaltPkgRemoved() {
        this.packages = new LinkedHashMap<>();
    }

    /**
     * Add package.
     *
     * @param name package name
     * @return this
     */
    public SaltPkgRemoved addPackage(String name) {
        return this.addPackage(name, null, null);
    }

    /**
     * Add package.
     *
     * @param name package name
     * @param version package version
     * @return this
     */
    public SaltPkgRemoved addPackage(String name, String version) {
        return this.addPackage(name, version, null);
    }

    /**
     * Add package.
     *
     * @param name package name
     * @param version package version
     * @param operator operator
     * @return this
     */
    public SaltPkgRemoved addPackage(String name, String version, String operator) {
        this.packages.put(name, new SaltPkgRemoved.Package(name, version, operator));
        return this;
    }

    /**
     * Get the data structure to be serialized as YAML.
     *
     * @return data structure to be serialized as YAML
     */
    @Override
    public Map<String, Object> getData() {
        // The list of packages
        List<Object> pkgsList = new ArrayList<>();
        for (SaltPkgRemoved.Package pkg : packages.values()) {
            if (pkg.getVersion() != null) {
                StringBuilder version = new StringBuilder();
                if (pkg.getOperator() != null) {
                    version.append(pkg.getOperator());
                }
                version.append(pkg.getVersion());
                pkgsList.add(new HashMap<String, Object>() {
                    {
                        put(pkg.getName(), version.toString());
                    }
                });
            }
            else {
                pkgsList.add(pkg.getName());
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("pkgs", pkgsList);
        List<Map<String, Object>> pkgs = new ArrayList<>();
        pkgs.add(map);

        addRequisites(map);

        // Put the policy
        Map<String, Object> policy = new LinkedHashMap<>();
        policy.put("pkg.removed", pkgs);

        // State is on the top level
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("pkg_removed", policy);
        return state;
    }
}
