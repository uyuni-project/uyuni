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
package com.redhat.rhn.domain.product;


import static java.util.Optional.ofNullable;

import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.server.InstalledProduct;

import com.suse.utils.Opt;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Fetches {@link SUSEProduct} objects caching them for speed.
 */
public class CachingSUSEProductFactory {

    /* Cache for findSUSEProduct() results */
    private Map<String, SUSEProduct> suseProductCache = new HashMap<>();

    /**
     * Returns the SUSE product corresponding to an InstalledProduct, if available. Caches results for faster lookups.
     * @param ip an installed product
     */
    private SUSEProduct lookupCachedSUSEProduct(InstalledProduct ip) {
        String name = ip.getName();
        String version = ip.getVersion();
        String release = ip.getRelease();
        String arch = Opt.fold(ofNullable(ip.getArch()), () -> null, PackageArch::getLabel);

        String key = name + "-" + version + "-" + release + "-" + arch;
        SUSEProduct cached = suseProductCache.get(key);
        if (cached != null) {
            return cached;
        }
        else {
            SUSEProduct result = SUSEProductFactory.findSUSEProduct(name, version, release, arch, true);
            suseProductCache.put(key, result);
            return result;
        }
    }

    /**
     * Maps many InstalledProducts to many SUSEProducts
     * @param installedProducts the installed products
     * @return the SUSE products stream
     */
    public Stream<SUSEProduct> map(Collection<InstalledProduct> installedProducts) {
        return installedProducts.stream()
                .map(this::lookupCachedSUSEProduct)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(SUSEProduct::isBase).thenComparing(SUSEProduct::getId));
    }
}
