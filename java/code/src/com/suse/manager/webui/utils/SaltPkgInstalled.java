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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * YAML generator for the Salt Package State.
 */
public class SaltPkgInstalled implements SaltState {
    /**
     * Package data
     */
    static class Package {
        private final String name;
        private final String version;
        private final String operator;

        public Package(String name, String version, String operator) {
            this.name = name;
            this.version = version;
            this.operator = operator != null ? (operator.isEmpty() ? null : operator) : null;
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

    private final String machineId;
    private final Set<String> repos;
    private final Map<String, SaltPkgInstalled.Package> packages ;
    private File destination;
    private boolean packageVerify = true;

    /**
     * Constructor.
     * @param machineId
     */
    public SaltPkgInstalled(String machineId) {
        this.machineId = machineId;
        this.repos = new HashSet<>();
        this.packages = new LinkedHashMap<>();
    }

    /**
     * Set package verification flag.
     *
     * @param packageVerify
     * @return
     */
    public SaltPkgInstalled setPackageVerify(boolean packageVerify) {
        this.packageVerify = packageVerify;
        return this;
    }

    /**
     * Add a repository.
     *
     * @param repo
     * @return
     */
    public SaltPkgInstalled addRepo(String repo) {
        this.repos.add(repo);

        return this;
    }

    /**
     * Add package.
     *
     * @param name
     * @return
     */
    public SaltPkgInstalled addPackage(String name) {
        return this.addPackage(name, null, null);
    }

    /**
     * Add package.
     *
     * @param name
     * @param version
     * @return
     */
    public SaltPkgInstalled addPackage(String name, String version) {
        return this.addPackage(name, version, null);
    }

    /**
     * Add package.
     *
     * @param name
     * @param version
     * @param operator
     * @return
     */
    public SaltPkgInstalled addPackage(String name, String version, String operator) {
        this.packages.put(name, new SaltPkgInstalled.Package(name, version, operator));
        return this;
    }

    /**
     * Get a data for the YAML.
     *
     * @return
     */
    @Override
    public Map<String, Object> getData() {
        Map<String, Object> policy = new LinkedHashMap<>();
        Map<String, Object> pkgs = new LinkedHashMap<>();
        Map<String, Object> state = new LinkedHashMap<>();
        List<Object> content = new ArrayList<>();

        // Set general settings
        pkgs.put("pkg_verify", this.packageVerify);
        pkgs.put("refresh", false);

        // Set packages
        for (SaltPkgInstalled.Package pkg : this.packages.values()) {
            if (pkg.getVersion() != null) {
                StringBuilder version = new StringBuilder();
                if (pkg.getOperator() != null) {
                    version.append(pkg.getOperator());
                }
                version.append(pkg.getVersion());
                content.add(new HashMap<String, Object>() {
                    {
                        put(pkg.getName(), version.toString());
                    }
                });
            }
            else {
                content.add(pkg.getName());
            }
        }

        state.put(String.format("%s_pkg_installed", this.machineId), policy);
        policy.put("pkg.installed", pkgs);
        pkgs.put("pkgs", content);

        return state;
    }
}
