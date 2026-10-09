/*
 * Copyright (c) 2021--2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.manager.content.ubuntu;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

public class Release {
    private Map<String, Urls> archs;
    private Map<String, PackageInfo> binaries;
    private Map<String, PackageInfo> sources;
    private Map<String, Binary> allbinaries;

    public Map<String, PackageInfo> getBinaries() {
        return Objects.requireNonNullElseGet(binaries, () -> Map.of());
    }

    public Map<String, Binary> getAllbinaries() {
        return Objects.requireNonNullElseGet(allbinaries, () -> Map.of());
    }

    public Map<String, PackageInfo> getSources() {
        return Objects.requireNonNullElseGet(sources, () -> Map.of());
    }

    public Map<String, Urls> getArchs() {
        return Objects.requireNonNullElseGet(archs, () -> Map.of());
    }

    /**
     * Retrieves a map of packages associated with this release. The key is the package name meanwhile
     * the value is the corresponding version.
     *
     * @return the package name -> version map for all the packages associated with this release.
     */
    public Map<String, String> getPackageMap() {
        Map<String, ? extends BasePackageInfo> packageMap = getAllbinaries();
        if (packageMap.isEmpty()) {
            packageMap = getBinaries();
        }

        return packageMap.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue().getVersion()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
}
