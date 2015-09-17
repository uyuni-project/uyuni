package com.suse.manager.gatherer;

import com.suse.manager.model.gatherer.GathererModule;

import java.util.Map;
import java.util.Set;


public enum GathererFactory {

    /** Singleton instance. */
    INSTANCE;

    private final Map<String, GathererModule> gathererModules =
            new GathererCommand().listModules();
    private static Long created = System.currentTimeMillis();

    public Set<String> listModules() {
        updateCacheIfNeeded(false);
        return gathererModules.keySet();
    }

    public GathererModule getDetails(String moduleName) {
        updateCacheIfNeeded(false);
        return gathererModules.get(moduleName);
    }

    private void updateCacheIfNeeded(boolean force) {
        if(force || System.currentTimeMillis() - created > 5*60*60*1000) {
            // cache is more than 5 minutes old => refresh
            gathererModules.clear();
            gathererModules.putAll(new GathererCommand().listModules());
            created = System.currentTimeMillis();
        }
    }
}
