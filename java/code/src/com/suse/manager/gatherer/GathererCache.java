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

package com.suse.manager.gatherer;

import com.suse.manager.model.gatherer.GathererModule;
import java.util.Map;
import java.util.Set;

/**
 * Gatherer Cache
 */
public enum GathererCache {

    /** Singleton instance. */
    INSTANCE;

    private Map<String, GathererModule> gathererModules =
            new GathererRunner().listModules();
    private Long cacheCreated = System.currentTimeMillis();
    private long CACHE_LIFETIME = 5 * 60 * 1000;

    /**
     * List the currently available modules of the gatherer
     *
     * @return available gatherer modules
     */
    public Set<String> listAvailableModules() {
        updateCacheIfNeeded(false);
        return gathererModules.keySet();
    }

    /**
     * Get the deatils of a specific gatherer module
     *
     * @param moduleName name of the module
     * @return the details of the requested module
     */
    public GathererModule getDetails(String moduleName) {
        updateCacheIfNeeded(false);
        return gathererModules.get(moduleName);
    }

    private void updateCacheIfNeeded(boolean force) {
        if (force || System.currentTimeMillis() - cacheCreated > CACHE_LIFETIME) {
            // cache is more than 5 minutes old => refresh
            gathererModules = new GathererRunner().listModules();
            cacheCreated = System.currentTimeMillis();
        }
    }
}
