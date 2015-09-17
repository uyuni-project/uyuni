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
            new GathererCommand().listModules();
    private Long cacheCreated = System.currentTimeMillis();
    private long CACHE_LIFETIME = 5*60*1000;

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
            gathererModules = new GathererCommand().listModules();
            cacheCreated = System.currentTimeMillis();
        }
    }
}
