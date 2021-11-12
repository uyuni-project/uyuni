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
public class SaltPkgInstalled extends AbstractSaltRequisites implements SaltState {
    /**
     * Package data
     */
    static class Package {
        private final String name;
        private final String arch;
        private final String version;
        private final String operator;

        Package(String nameIn, String archIn, String versionIn, String operatorIn) {
            this.name = nameIn;
            this.version = versionIn;
            this.arch = archIn;
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

        /**
         * @return arch to get
         */
        public String getArch() {
            return arch;
        }
    }

    private final Map<String, SaltPkgInstalled.Package> packages;

    /**
     * Constructor.
     */
    public SaltPkgInstalled() {
        this.packages = new LinkedHashMap<>();
    }

    /**
     * Add package.
     *
     * @param name package name
     * @return this
     */
    public SaltPkgInstalled addPackage(String name) {
        return this.addPackageNameArchVersionOp(name, null, null, null);
    }

    /**
     * Add package.
     *
     * @param name package name
     * @param version package version
     * @param arch package arch
     * @return this
     */
    public SaltPkgInstalled addPackageNameArchVersion(String name, String arch, String version) {
        return this.addPackageNameArchVersionOp(name, arch, version, null);
    }

    /**
     * Add package.
     *
     * @param name package name
     * @param arch package arch
     * @param version package version
     * @param operator package operator
     * @return this
     */
    public SaltPkgInstalled addPackageNameArchVersionOp(String name, String arch, String version, String operator) {
        this.packages.put(name, new SaltPkgInstalled.Package(name, arch, version, operator));
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
        for (SaltPkgInstalled.Package pkg : packages.values()) {
            if (pkg.getVersion() != null) {
                StringBuilder version = new StringBuilder();
                if (pkg.getOperator() != null) {
                    version.append(pkg.getOperator());
                }
                version.append(pkg.getVersion());
                pkgsList.add(new HashMap<String, Object>() {
                    {
                        put(pkg.getName() + "." + pkg.getArch(), version.toString());
                    }
                });
            }
            else {
                pkgsList.add(pkg.getName());
            }
        }
        map = new HashMap<>();
        map.put("pkgs", pkgsList);
        pkgs.add(map);

        addRequisites(map);

        // Put the policy
        Map<String, Object> policy = new LinkedHashMap<>();
        policy.put("pkg.installed", pkgs);

        // State is on the top level
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("pkg_installed", policy);
        return state;
    }
}
