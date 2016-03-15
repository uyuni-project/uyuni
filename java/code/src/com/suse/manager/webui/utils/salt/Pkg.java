/**
 * CHECKSTYLE:OFF
 */
package com.suse.manager.webui.utils.salt;

import com.suse.salt.netapi.calls.LocalCall;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * salt.modules.pkg
 */
public class Pkg {

    /**
     * @param refresh set true to perform a refresh before the installation
     * @param pkgs a map of package names to versions
     * @return the LocalCall object
     */
    public static LocalCall<Map<String, Object>> install(boolean refresh,
            Map<String, String> pkgs) {
        LinkedHashMap<String, Object> kwargs = new LinkedHashMap<>();
        kwargs.put("refresh", refresh);

        // Convert map into a list of maps with a single entry each as expected by Salt
        List<Map<String, String>> pkgsList = new ArrayList<>();
        for (String pkg : pkgs.keySet()) {
            Map<String, String> map = new HashMap<>();
            map.put(pkg, pkgs.get(pkg));
            pkgsList.add(map);
        }
        kwargs.put("pkgs", pkgsList);

        return new LocalCall<>("pkg.install", Optional.empty(), Optional.of(kwargs),
                new TypeToken<Map<String, Object>>() { });
    }
}
